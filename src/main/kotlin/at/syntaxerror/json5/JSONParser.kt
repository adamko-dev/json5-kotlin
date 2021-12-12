/*
 * MIT License
 * 
 * Copyright (c) 2021 SyntaxError404
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package at.syntaxerror.json5

import at.syntaxerror.json5.JSONException.JSONParseError
import at.syntaxerror.json5.JSONException.JSONSyntaxError
import at.syntaxerror.json5.config.Json5Options
import at.syntaxerror.json5.config.Json5Options.ParserOptions.UnicodeSurrogate.LENIENT
import java.io.BufferedReader
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.regex.Pattern

/**
 * A JSONParser is used to convert a source string into tokens, which then are used to
 * construct [JSONObjects][JSONObject] and [JSONArrays][JSONArray]
 *
 * The reader is not [closed][Reader.close]
 *
 * @author SyntaxError404
 */
open class JSONParser(
  reader: Reader,
  private val options: Json5Options = Json5Options(),
) {
  private val reader: Reader = if (reader.markSupported()) reader else BufferedReader(reader)

  /** whether the end of the file has been reached  */
  private var eof: Boolean = false
  /** whether the current character should be re-read  */
  private var back: Boolean = false
  /** the absolute position in the string  */
  private var index: Long = -1
  /** the relative position in the line  */
  private var character: Long = 0
  /** the line number  */
  private var line: Long = 1
  /** the previous character  */
  private var previous: Char = Char.MIN_VALUE
  /** the current character  */
  private var current: Char = Char.MIN_VALUE

  private fun more(): Boolean {
    return if (back || eof) back && !eof else peek().code > 0
  }
  /**
   * Forces the parser to re-read the last character
   */
  fun back() {
    back = true
  }

  private fun peek(): Char {
    if (eof) return Char.MIN_VALUE
    val c: Int
    try {
      reader.mark(1)
      c = reader.read()
      reader.reset()
    } catch (e: Exception) {
      throw JSONException("Could not peek from source", e)
    }
    return if (c == -1) Char.MIN_VALUE else c.toChar()
  }

  private operator fun next(): Char {
    if (back) {
      back = false
      return current
    }
    val c: Int = try {
      reader.read()
    } catch (e: Exception) {
      throw JSONParseError("Could not read from source", e)
    }
    if (c < 0) {
      eof = true
      return Char.MIN_VALUE
    }
    previous = current
    current = c.toChar()
    index++
    if (isLineTerminator(current) && (current != '\n' || previous != '\r')) {
      line++
      character = 0
    } else character++
    return current
  }
  // https://262.ecma-international.org/5.1/#sec-7.3
  private fun isLineTerminator(c: Char): Boolean {
    return when (c) {
      '\n', '\r', '\u2028', '\u2029' -> true
      else                           -> false
    }
  }

  @Deprecated("asd", ReplaceWith("c.isWhitespace()"))
  private fun isWhitespace(c: Char): Boolean = c.isWhitespace()


  @Deprecated("asd", ReplaceWith("c.isDigit()"))
  private fun isDecimalDigit(c: Char): Boolean {
    c.isDigit()
    return c in '0'..'9'
  }

  private fun nextMultiLineComment() {
    while (true) {
      val n = next()
      if (n == '*' && peek() == '/') {
        next()
        return
      }
    }
  }

  private fun nextSingleLineComment() {
    while (true) {
      val n = next()
      if (isLineTerminator(n) || n.code == 0) return
    }
  }
  /**
   * Reads until encountering a character that is not a whitespace according to the
   * [JSON5 Specification](https://spec.json5.org/#white-space)
   *
   * @return a non-whitespace character, or `0` if the end of the stream has been reached
   */
  fun nextClean(): Char {
    while (true) {
      if (!more()) throw JSONParseError("Unexpected end of data")
      val n = next()
      if (n == '/') {
        val p = peek()
        when (p) {
          '*'  -> {
            next()
            nextMultiLineComment()
          }
          '/'  -> {
            next()
            nextSingleLineComment()
          }
          else -> return n
        }
      } else if (!n.isWhitespace()) return n
    }
  }

  private fun nextCleanTo(delimiters: String = CLOSING_DELIMITERS): String {
    val result = StringBuilder()
    while (true) {
      if (!more()) throw JSONParseError("Unexpected end of data")
      val n = nextClean()
      if (delimiters.indexOf(n) > -1 || n.isWhitespace()) {
        back()
        break
      }
      result.append(n)
    }
    return result.toString()
  }

  private fun dehex(c: Char): Int {
    if (c in '0'..'9') return c - '0'
    if (c in 'a'..'f') return c - 'a' + 0xA
    return if (c in 'A'..'F') c - 'A' + 0xA else -1
  }

  private fun unicodeEscape(member: Boolean, part: Boolean): Char {
    val where = if (member) "key" else "string"
    var value = ""
    var codepoint = 0
    for (i in 0..3) {
      val n = next()
      value += n
      val hex = dehex(n)
      if (hex == -1) throw JSONSyntaxError(
        "Illegal unicode escape sequence '\\u$value' in $where",
      )
      codepoint = codepoint or (hex shl (3 - i shl 2))
    }
    if (member && !isMemberNameChar(
        codepoint.toChar(),
        part
      )
    ) throw JSONSyntaxError(
      "Illegal unicode escape sequence '\\u$value' in key",
    )
    return codepoint.toChar()
  }

  private fun checkSurrogate(hi: Char, lo: Char) {
    if (options.parserOptions.allowInvalidSurrogates == LENIENT) return
    if (!Character.isHighSurrogate(hi) || !Character.isLowSurrogate(lo)) return
    if (!Character.isSurrogatePair(hi, lo))
      throw JSONSyntaxError(
        String.format("Invalid surrogate pair: U+%04X and U+%04X", hi, lo),
      )
  }
  // https://spec.json5.org/#prod-JSON5String
  private fun nextString(quote: Char): String {
    val result = StringBuilder()
    var value: String
    var codepoint: Int
    var n = 0.toChar()
    var prev: Char
    while (true) {
      if (!more()) throw JSONParseError("Unexpected end of data", null)
      prev = n
      n = next()
      if (n == quote) break
      if (isLineTerminator(n) && n.code != 0x2028 && n.code != 0x2029) throw JSONSyntaxError(
        "Unescaped line terminator in string",
      )
      if (n == '\\') {
        n = next()
        if (isLineTerminator(n)) {
          if (n == '\r' && peek() == '\n') next()

          // escaped line terminator/ line continuation
          continue
        } else when (n) {
          '\'', '"', '\\' -> {
            result.append(n)
            continue
          }
          'b'             -> {
            result.append('\b')
            continue
          }
          'f'             -> {
            result.append('\u000c')
            continue
          }
          'n'             -> {
            result.append('\n')
            continue
          }
          'r'             -> {
            result.append('\r')
            continue
          }
          't'             -> {
            result.append('\t')
            continue
          }
          'v'             -> {
            result.append(0x0B.toChar())
            continue
          }
          '0'             -> {
            val p = peek()
            if (p.isDigit()) throw JSONSyntaxError(
              "Illegal escape sequence '\\0$p'",
            )
            result.append(0.toChar())
            continue
          }
          'x'             -> {
            value = ""
            codepoint = 0
            var i = 0
            while (i < 2) {
              n = next()
              value += n
              val hex = dehex(n)
              if (hex == -1) throw JSONSyntaxError(
                "Illegal hex escape sequence '\\x$value' in string",
              )
              codepoint = codepoint or (hex shl (1 - i shl 2))
              ++i
            }
            n = codepoint.toChar()
          }
          'u'             -> n = unicodeEscape(false, false)
          else            -> if (n.isDigit()) throw JSONSyntaxError(
            "Illegal escape sequence '\\$n'",
          )
        }
      }
      checkSurrogate(prev, n)
      result.append(n)
    }
    return result.toString()
  }

  private fun isMemberNameChar(n: Char, part: Boolean): Boolean {
    if (n == '$' || n == '_' || n.code == 0x200C || n.code == 0x200D) {
      return true
    }

    return when (n.category) {
      CharCategory.UPPERCASE_LETTER,
      CharCategory.LOWERCASE_LETTER,
      CharCategory.TITLECASE_LETTER,
      CharCategory.MODIFIER_LETTER,
      CharCategory.OTHER_LETTER,
      CharCategory.LETTER_NUMBER
           -> return true
      CharCategory.NON_SPACING_MARK,
      CharCategory.COMBINING_SPACING_MARK,
      CharCategory.DECIMAL_DIGIT_NUMBER,
      CharCategory.CONNECTOR_PUNCTUATION
           -> part
      else -> false
    }
  }
  /**
   * Reads a member name from the source according to the
   * [JSON5 Specification](https://spec.json5.org/#prod-JSON5MemberName)
   *
   * @return an member name
   */
  fun nextMemberName(): String {
    val result = StringBuilder()
    var prev: Char
    var n = next()
    if (n == '"' || n == '\'') return nextString(n)
    back()
    n = 0.toChar()
    while (true) {
      if (!more()) throw JSONSyntaxError("Unexpected end of data")
      val part = result.isNotEmpty()
      prev = n
      n = next()
      if (n == '\\') { // unicode escape sequence
        n = next()
        if (n != 'u') throw JSONSyntaxError(
          "Illegal escape sequence '\\$n' in key",
        )
        n = unicodeEscape(true, part)
      } else if (!isMemberNameChar(n, part)) {
        back()
        break
      }
      checkSurrogate(prev, n)
      result.append(n)
    }
    if (result.isEmpty()) throw JSONSyntaxError("Empty key")
    return result.toString()
  }
  /**
   * Reads a value from the source according to the
   * [JSON5 Specification](https://spec.json5.org/#prod-JSON5Value)
   */
  fun nextValue(): Any? {
    when (val n = nextClean()) {
      '"', '\'' -> {
        val string = nextString(n)
        if (options.parserOptions.parseInstants && options.parserOptions.parseStringInstants) try {
          return Instant.parse(string)
        } catch (ignored: Exception) {
        }
        return string
      }
      '{'       -> {
        back()
        return Json5ObjectParser(reader, options).parseObject()
      }
      '['       -> {
        back()
        return Json5ArrayParser(reader, options).parse()
      }
    }
    back()
    val string = nextCleanTo()
    if (string == "null") return null
    if (PATTERN_BOOLEAN.matcher(string).matches()) return string == "true"
    if (PATTERN_NUMBER_INTEGER.matcher(string).matches()) {
      val bigint = BigInteger(string)
      if (options.parserOptions.parseInstants && options.parserOptions.parseUnixInstants) try {
        val unix = bigint.longValueExact()
        return Instant.ofEpochSecond(unix)
      } catch (ignored: Exception) {
      }
      return bigint
    }
    if (PATTERN_NUMBER_FLOAT.matcher(string).matches()) return BigDecimal(string)
    if (PATTERN_NUMBER_SPECIAL.matcher(string).matches()) {
      val special: String
      val factor: Int
      var d = 0.0
      when (string[0]) {
        '+'  -> {
          special = string.substring(1) // +
          factor = 1
        }
        '-'  -> {
          special = string.substring(1) // -
          factor = -1
        }
        else -> {
          special = string
          factor = 1
        }
      }
      when (special) {
        "NaN"      -> {
          if (!options.allowNaN) throw JSONSyntaxError("NaN is not allowed")
          d = Double.NaN
        }
        "Infinity" -> {
          if (!options.allowInfinity) throw JSONSyntaxError("Infinity is not allowed")
          d = Double.POSITIVE_INFINITY
        }
      }
      return factor * d
    }
    if (PATTERN_NUMBER_HEX.matcher(string).matches()) {
      val hex: String
      val factor: Int
      when (string[0]) {
        '+'  -> {
          hex = string.substring(3) // +0x
          factor = 1
        }
        '-'  -> {
          hex = string.substring(3) // -0x
          factor = -1
        }
        else -> {
          hex = string.substring(2) // 0x
          factor = 1
        }
      }
      val bigint = BigInteger(hex, 16)
      return if (factor == -1) bigint.negate() else bigint
    }
    throw JSONException("Illegal value '$string'")
  }

  override fun toString(): String {
    return " at index $index [character $character in line $line]"
  }

  companion object {
    const val CLOSING_DELIMITERS = ",]}"

    private val PATTERN_BOOLEAN = Pattern.compile("true|false")
    private val PATTERN_NUMBER_FLOAT =
      Pattern.compile("[+-]?((0|[1-9]\\d*)(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?")
    private val PATTERN_NUMBER_INTEGER = Pattern.compile("[+-]?(0|[1-9]\\d*)")
    private val PATTERN_NUMBER_HEX = Pattern.compile("[+-]?0[xX][0-9a-fA-F]+")
    private val PATTERN_NUMBER_SPECIAL = Pattern.compile("[+-]?(Infinity|NaN)")
  }
}