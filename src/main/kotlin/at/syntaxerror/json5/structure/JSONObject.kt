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
package at.syntaxerror.json5.structure

import at.syntaxerror.json5.error.JSONException
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.function.Function

/**
 * A JSONObject is a map (key-value) structure capable of holding multiple values,
 * including other [JSONArrays][JSONArray] and JSONObjects
 *
 * @author SyntaxError404
 */
class JSONObject(
  private val internalMap: MutableMap<String, Any?> = mutableMapOf()
) : MutableMap<String, Any?> by internalMap {

  /**
   * Converts the JSONObject into a map. All JSONObjects and JSONArrays
   * contained within this JSONObject will be converted into their
   * Map or List form as well
   *
   * @return a map of the entries of this object
   */
  fun toMap(): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    for (entry in this) {
      val value = entry.value
      if (value is JSONObject) {
        map[entry.key] = value.toMap()
      } else if (value is JSONArray) {
        map[entry.key] = value.toList()
      }
    }
    return map
  }

  private fun <T> getNumberExact(
    key: String,
    type: String,
    bigint: Function<BigInteger, T>,
    bigdec: Function<BigDecimal, T>
  ): T {
    val number = internalMap[key] as? Number
    try {
      if (number is BigInteger) return bigint.apply(number)
      if (number is BigDecimal) return bigdec.apply(number)
    } catch (_: Exception) {
    }
    throw mismatch(key, type)
  }
  /**
   * Returns the exact value as a byte for a given key.
   * This fails if the value does not fit into a byte
   *
   * @throws JSONException if the key does not exist, the value is not a byte, or if the value does not fit into a byte
   */
  fun getByteExact(key: String): Byte {
    return getNumberExact(
      key,
      "byte",
      { obj: BigInteger -> obj.byteValueExact() },
      BigDecimal::byteValueExact
    )
  }
  /**
   * Returns the exact value as a short for a given key.
   * This fails if the value does not fit into a short
   *

   *
   * @throws JSONException if the key does not exist, the value is not a short, or if the value does not fit into a short
   */
  fun getShortExact(key: String): Short {
    return getNumberExact(
      key,
      "short",
      { obj: BigInteger -> obj.shortValueExact() },
      BigDecimal::shortValueExact
    )
  }
  /**
   * Returns the exact value as an int for a given key.
   * This fails if the value does not fit into an int
   *

   *
   * @throws JSONException if the key does not exist, the value is not an int, or if the value does not fit into an int
   */
  fun getIntExact(key: String): Int {
    return getNumberExact(
      key,
      "int",
      { obj: BigInteger -> obj.intValueExact() },
      BigDecimal::intValueExact
    )
  }
  /**
   * Returns the exact value as a long for a given key.
   * This fails if the value does not fit into a long
   *
   * @throws JSONException if the key does not exist, the value is not a long, or if the value does not fit into a long
   */
  fun getLongExact(key: String): Long {
    return getNumberExact(
      key,
      "long",
      { obj: BigInteger -> obj.longValueExact() },
      BigDecimal::longValueExact
    )
  }
  /**
   * Returns the exact value as a float for a given key.
   * This fails if the value does not fit into a float
   *
   * @throws JSONException if the key does not exist, the value is not a float, or if the value does not fit into a float
   */
  fun getFloatExact(key: String): Float? {
    val num = internalMap[key] as? Number
    if (num is Double) // NaN and Infinity
      return num.toFloat()
    val f = num?.toFloat()
    if (f?.isFinite() == false) throw mismatch(key, "float")
    return f
  }
  /**
   * Returns the exact value as a double for a given key.
   * This fails if the value does not fit into a double
   *

   *
   * @throws JSONException if the key does not exist, the value is not a double, or if the value does not fit into a double
   */
  fun getDoubleExact(key: String): Double? {
    val num = internalMap[key] as? Number
    if (num is Double) // NaN and Infinity
      return num
    val d = num?.toDouble()
    if (d?.isFinite() == false) throw mismatch(key, "double")
    return d
  }

  override fun put(key: String, value: Any?): Any? {
    return internalMap.put(key, sanitize(value))
  }

//  /**
//   * Converts the JSONObject into its compact string representation.
//   */
//  override fun toString(): String {
//    return prettyPrint(0, JSONOptions())
//  }

  companion object {

    private fun mismatch(key: String, type: String): JSONException {
      return JSONException("JSONObject[$key] is not of type $type")
    }

    fun sanitize(value: Any?): Any? {
      if (value == null) {
        return null
      }
      return when (value) {
        is Boolean, is String, is JSONObject, is JSONArray, is Instant -> {
          value
        }
        is Number                                                      -> {
          when {
            value is Double                                                  -> {
              if (java.lang.Double.isFinite(value)) {
                return BigDecimal.valueOf(value)
              }
            }
            value is Float                                                   -> {
              return if (java.lang.Float.isFinite(value)) {
                BigDecimal.valueOf(value.toDouble())
              } else {
                // NaN and Infinity
                value.toDouble()
              }

            }
            value is Byte || value is Short || value is Int || value is Long -> {
              return BigInteger.valueOf(value.toLong())
            }
            !(value is BigDecimal || value is BigInteger)                    -> {
              return BigDecimal.valueOf(value.toDouble())
            }
          }
          value
        }
        else                                                           -> {
          throw JSONException("Illegal type '${value.javaClass}'")
        }
      }
    }
  }
}
