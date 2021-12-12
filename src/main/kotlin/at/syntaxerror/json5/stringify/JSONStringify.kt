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
package at.syntaxerror.json5.stringify

import at.syntaxerror.json5.config.Json5Options
import at.syntaxerror.json5.config.Json5Options.StringifyOptions.Format
import at.syntaxerror.json5.constants.CharUnicode
import at.syntaxerror.json5.error.JSONException
import at.syntaxerror.json5.structure.JSONArray
import at.syntaxerror.json5.structure.JSONObject
import java.time.Instant

/**
 * A utility class for serializing [JSONObjects][JSONObject] and
 * [JSONArrays][JSONArray] into their string representations
 *
 * @author SyntaxError404
 */
class JSONStringify(
  private val options: Json5Options,
) {
  private val indent = when (val format = options.stringifyOptions.formatting) {
    is Format.Compact -> null
    is Format.Pretty  -> format.indentation
  }
  private val quotationToken = options.stringifyOptions.quoteSingle
  private val quotationTokenDouble = "$quotationToken$quotationToken"


  fun jsonObjectToString(obj: JSONObject): String {

    val sb = StringBuilder()
    sb.append('{')
    for ((key: String, value: Any?) in obj) {
      if (sb.length != 1) sb.append(',')
      if (indent != null) sb.append('\n').append(indent)
      sb.append(quote(key)).append(':')
      if (indent != null) sb.append(' ')
      sb.append(anyToString(value))
    }
    if (indent != null) sb.append('\n').append(indent)
    sb.append('}')

    return sb.toString()
  }

  fun jsonArrayToString(array: JSONArray): String {

    val sb = StringBuilder()

    sb.append('[')
    for (value in array) {
      if (sb.length != 1) sb.append(',')
      if (indent != null) sb.append('\n').append(indent)
      sb.append(anyToString(value))
    }
    if (indent != null) sb.append('\n').append(indent)
    sb.append(']')
    return sb.toString()
  }

    fun anyToString(
    value: Any?,
  ): String {
    if (value == null) return "null"
    if (value is JSONObject) return jsonObjectToString(value)
    if (value is JSONArray) return jsonArrayToString(value)
    if (value is String) return quote(value as String?)
    if (value is Instant) {
      return if (options.stringifyOptions.stringifyUnixInstants) {
        value.epochSecond.toString()
      } else {
        quote(value.toString())
      }
    }
    if (value is Double) {
      if (!options.allowNaN && value.isNaN()) throw JSONException("Illegal NaN in JSON")
      if (!options.allowInfinity && value.isInfinite()) throw JSONException("Illegal Infinity in JSON")
    }
    return value.toString()
  }

  fun quote(string: String?): String {

    if (string.isNullOrEmpty())
      return quotationTokenDouble

    val quoted = StringBuilder(string.length + 2)
    quoted.append(quotationToken)
    for (c in string.toCharArray()) {
      if (c == quotationToken) {
        quoted.append('\\')
        quoted.append(c)
      } else {
        quoted.append(escapeChar(c) ?: c)
      }
    }
    quoted.append(quotationToken)
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
