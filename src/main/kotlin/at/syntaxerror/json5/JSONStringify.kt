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

import java.time.Instant

/**
 * A utility class for serializing [JSONObjects][JSONObject] and
 * [JSONArrays][JSONArray] into their string representations
 *
 * @author SyntaxError404
 */
object JSONStringify {

  fun jsonObjectToString(
    obj: JSONObject,
    options: JSONOptions,
    indent: String = "",
    indentFactor: Int = 0,
  ): String {
    val indentFactor = indentFactor.coerceAtLeast(0)

    val sb = StringBuilder()
    val childIndent = indent + " ".repeat(indentFactor)
    sb.append('{')

    for ((key: String, value: Any?) in obj) {
      if (sb.length != 1) sb.append(',')
      if (indentFactor > 0) sb.append('\n').append(childIndent)
      sb.append(quote(key, options))
        .append(':')
      if (indentFactor > 0) sb.append(' ')
      sb.append(toString(value, childIndent, indentFactor, options))
    }
    if (indentFactor > 0) sb.append('\n').append(indent)
    sb.append('}')
    return sb.toString()
  }

  fun jsonArrayToString(
    array: JSONArray,
    options: JSONOptions,
    indent: String = "",
    indentFactor: Int = 0,
  ): String {
    val indentFactor = indentFactor.coerceAtLeast(0)

    val sb = StringBuilder()
    val childIndent = indent + " ".repeat(indentFactor)
    sb.append('[')
    for (value in array) {
      if (sb.length != 1) sb.append(',')
      if (indentFactor > 0) sb.append('\n').append(childIndent)
      sb.append(toString(value, childIndent, indentFactor, options))
    }
    if (indentFactor > 0) sb.append('\n').append(indent)
    sb.append(']')
    return sb.toString()
  }

  private fun toString(
    value: Any?,
    indent: String,
    indentFactor: Int,
    options: JSONOptions,
  ): String {
    if (value == null) return "null"
    if (value is JSONObject) return jsonObjectToString(value, options, indent, indentFactor)
    if (value is JSONArray) return jsonArrayToString(value, options, indent, indentFactor)
    if (value is String) return quote(value as String?, options)
    if (value is Instant) {
      return if (options.stringifyUnixInstants) value.epochSecond.toString() else quote(
        value.toString(),
        options
      )
    }
    if (value is Double) {
      if (!options.allowNaN && java.lang.Double.isNaN(value)) throw JSONException("Illegal NaN in JSON")
      if (!options.allowInfinity && java.lang.Double.isInfinite(value)) throw JSONException("Illegal Infinity in JSON")
    }
    return value.toString()
  }

  fun quote(string: String?, options: JSONOptions): String {

    if (string == null || string.isEmpty()) return if (options.quoteSingle) "''" else "\"\""
    val qt = if (options.quoteSingle) '\'' else '"'
    val quoted = StringBuilder(string.length + 2)
    quoted.append(qt)
    for (c in string.toCharArray()) {
      if (c == qt) {
        quoted.append('\\')
        quoted.append(c)
      } else {
        quoted.append(escapeChar(c) ?: c)
      }
    }
    quoted.append(qt)
    return quoted.toString()
  }

  private fun escapeChar(char: Char): String? {

    return when (char) {
      '\\'                  -> "\\\\"
      '\b'                  -> "\\b"
      CharUnicode.FORM_FEED -> "\\f"
      '\n'                  -> "\\n"
      '\r'                  -> "\\r"
      '\t'                  -> "\\t"
      else                  ->
        if (char in CharUnicode.VERTICAL) {
          "\\v"
        } else {
          when (char.category) {
            CharCategory.FORMAT,
            CharCategory.LINE_SEPARATOR,
            CharCategory.PARAGRAPH_SEPARATOR,
            CharCategory.CONTROL,
            CharCategory.PRIVATE_USE,
            CharCategory.SURROGATE,
            CharCategory.UNASSIGNED -> String.format("\\u%04X", char)
            else                    -> null
          }
        }
    }
  }
}
