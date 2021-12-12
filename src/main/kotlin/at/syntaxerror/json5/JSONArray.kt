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

import java.math.BigDecimal
import java.math.BigInteger
import java.util.function.Function

/**
 * A JSONArray is an array structure capable of holding multiple values,
 * including other JSONArrays and [JSONObjects][JSONObject]
 *
 * @author SyntaxError404
 */
class JSONArray(
  private val internalArray: MutableList<Any?> = mutableListOf()
) : MutableList<Any?> by internalArray {

  /**
   * Converts the JSONArray into a list. All JSONObjects and JSONArrays
   * contained within this JSONArray will be converted into their
   * Map or List form as well
   *
   * @return a list of the values of this array
   */
  fun toList(): List<Any> {
    val list: MutableList<Any> = ArrayList()
    for (value in this) {
      if (value is JSONObject) {
        list.add(value.toMap())
      } else if (value is JSONArray) {
        list.add(value.toList())
      }
    }
    return list
  }

  private fun <T> getNumberExact(
    index: Int,
    type: String,
    bigint: Function<BigInteger, T>,
    bigdec: Function<BigDecimal, T>
  ): T {
    val number = internalArray[index] as? Number
    try {
      if (number is BigInteger) return bigint.apply(number)
      if (number is BigDecimal) return bigdec.apply(number)
    } catch (e: Exception) {
    }
    throw mismatch(index, type)
  }
  /**
   * Returns the exact value as a byte for a given index.
   * This fails if the value does not fit into a byte
   *
   * @return the byte
   *
   * @throws JSONException if the index does not exist, the value is not a byte, or if the value does not fit into a byte
   */
  fun getByteExact(index: Int): Byte {
    return getNumberExact(
      index,
      "byte",
      { obj: BigInteger -> obj.byteValueExact() },
      BigDecimal::byteValueExact
    )
  }
  /**
   * Returns the exact value as a short for a given index.
   * This fails if the value does not fit into a short
   *
   * @return the short
   *
   * @throws JSONException if the index does not exist, the value is not a short, or if the value does not fit into a short
   */
  fun getShortExact(index: Int): Short {
    return getNumberExact(
      index,
      "short",
      { obj: BigInteger -> obj.shortValueExact() },
      BigDecimal::shortValueExact
    )
  }
  /**
   * Returns the exact value as an int for a given index.
   * This fails if the value does not fit into an int
   *
   * @throws JSONException if the index does not exist, the value is not an int, or if the value does not fit into an int
   */
  fun getIntExact(index: Int): Int {
    return getNumberExact(
      index,
      "int",
      { obj: BigInteger -> obj.intValueExact() },
      BigDecimal::intValueExact
    )
  }
  /**
   * Returns the exact value as a long for a given index.
   * This fails if the value does not fit into a long
   *
   * @throws JSONException if the index does not exist, the value is not a long, or if the value does not fit into a long
   */
  fun getLongExact(index: Int): Long {
    return getNumberExact(
      index,
      "long",
      { obj: BigInteger -> obj.longValueExact() },
      BigDecimal::longValueExact
    )
  }
  /**
   * Returns the exact value as a float for a given index.
   * This fails if the value does not fit into a float
   *
   * @throws JSONException if the index does not exist, the value is not a float, or if the value does not fit into a float
   */
  fun getFloatExact(index: Int): Float {
    val num = internalArray[index] as? Number
    if (num is Double) // NaN and Infinity
      return num.toFloat()
    val f = num?.toFloat()
    if (f?.isFinite() != true) throw mismatch(index, "float")
    return f
  }
  /**
   * Returns the exact value as a double for a given index.
   * This fails if the value does not fit into a double
   */
  fun getDoubleExact(index: Int): Double? {
    val num = internalArray[index] as? Number
    if (num is Double) // NaN and Infinity
      return num
    val d = num?.toDouble()
    if (d?.isFinite() == true) throw mismatch(index, "double")
    return d
  }


//  /**
//   * Converts the JSONArray into its compact string representation.
//   *
//   * @return the compact string representation
//   */
//  override fun toString(): String {
//    return prettyPrint(0, JSONOptions())
//  }


  companion object {
    private fun mismatch(index: Int, type: String): JSONException {
      return JSONException("JSONArray[$index] is not of type $type")
    }
  }
}
