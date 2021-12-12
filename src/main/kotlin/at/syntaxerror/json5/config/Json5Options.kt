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
package at.syntaxerror.json5.config


/**
 * This class used is used to customize the behaviour of Json5-Kotlin
 *
 * @author SyntaxError404
 * @since 1.1.0
 */
data class Json5Options constructor(

  /** Whether `NaN` should be allowed as a number */
  var allowNaN: Boolean = true,
  /**
   * Whether `Infinity` should be allowed as a number.
   * This applies to both `+Infinity` and `-Infinity`
   */
  var allowInfinity: Boolean = true,

  /** Customize the behaviour of [parsing][JSONParser]. */
  val parserOptions: ParserOptions = ParserOptions(),
  /** Customize the behaviour of [parsing][JSONStringify]. */
  val stringifyOptions: StringifyOptions = StringifyOptions(),
) {


  data class ParserOptions constructor(
    /**
     * Whether instants should be parsed as such.
     *
     * If this is `false`, [parseStringInstants] and [parseUnixInstants] are ignored
     */
    var parseInstants: Boolean = true,
    /**
     * Whether string instants (according to
     * [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6))
     * should be parsed as such.
     *
     * Ignored if [parseInstants] is `false`
     */
    var parseStringInstants: Boolean = true,
    /**
     * Whether unix instants (integers) should be parsed as such.
     * Ignored if [parseInstants] is `false`
     */
    var parseUnixInstants: Boolean = true,
    /** Whether invalid unicode surrogate pairs should be allowed. */
    var allowInvalidSurrogates: UnicodeSurrogate = UnicodeSurrogate.LENIENT,
  ) {

    enum class UnicodeSurrogate {
      STRICT, LENIENT
    }

  }

  data class StringifyOptions(

    /**
     * Whether instants should be stringifyed as unix timestamps.
     * If this is `false`, instants will be stringifyed as strings
     * (according to [RFC 3339, Section 5.6](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)).
     */
    var stringifyUnixInstants: Boolean = false,

    /**
     * Whether string should be single-quoted (`'`) instead of double-quoted (`"`).
     * This also includes a [JSONObject's][JSONObject] member names
     */
    var quoteSingle: Char = '\'',

    var formatting: Format = Format.Pretty(),
  ) {
    sealed interface Format {
      /** No indentation, no new-lines */
      object Compact : Format
      /** New lines and indentation (if [indentation] is not-empty) */
      @JvmInline
      value class Pretty(val indentation: String = "  ") : Format
    }
  }

}
