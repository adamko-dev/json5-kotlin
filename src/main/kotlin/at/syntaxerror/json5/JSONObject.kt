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

import at.syntaxerror.json5.JSONException.JSONSyntaxError
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.function.Function
import java.util.function.Predicate

/**
 * A JSONObject is a map (key-value) structure capable of holding multiple values,
 * including other [JSONArrays][JSONArray] and JSONObjects
 *
 * @author SyntaxError404
 */
class JSONObject() : Iterable<Map.Entry<String?, Any?>?> {
  private val values: MutableMap<String, Any?> = HashMap()

  /**
   * Constructs a new JSONObject from a string
   */
  constructor(source: String) : this(JSONParser(source))
  /**
   * Constructs a new JSONObject from a JSONParser
   *
   * @param parser a JSONParser
   */
  constructor(parser: JSONParser) : this() {
    var c: Char
    var key: String
    if (parser.nextClean() != '{') throw JSONSyntaxError("A JSONObject must begin with '{'")
    while (true) {
      c = parser.nextClean()
      key = when (c) {
        Char.MIN_VALUE -> throw JSONSyntaxError("A JSONObject must end with '}'")
        '}'            -> return
        else           -> {
          parser.back()
          parser.nextMemberName()
        }
      }
      if (has(key)) throw JSONException("Duplicate key " + JSONStringify.quote(key, JSONOptions()))
      c = parser.nextClean()
      if (c != ':') throw JSONSyntaxError("Expected ':' after a key, got '$c' instead")
      val value = parser.nextValue()
      values[key] = value
      c = parser.nextClean()
      if (c == '}') return
      if (c != ',') throw JSONSyntaxError("Expected ',' or '}' after value, got '$c' instead")
    }
  }

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
  /**
   * Returns a set of keys of the JSONObject
   *
   * @return a set of keys
   *
   * @see Map.keySet
   */
  fun keySet(): Set<String> {
    return values.keys
  }
  /**
   * Returns a set of entries of the JSONObject. Modifying the set
   * or an entry will modify the JSONObject
   *
   * Use with caution.
   *
   * @return a set of entries
   *
   * @see Map.entrySet
   */
  fun entrySet(): Set<Map.Entry<String, Any?>> {
    return values.entries
  }

  override fun iterator(): Iterator<Map.Entry<String, Any?>> {
    return values.entries.iterator()
  }
  /**
   * Returns the number of entries in the JSONObject
   *

   */
  fun length(): Int {
    return values.size
  }
  /**
   * Removes all values from this JSONObject
   *
   * @since 1.2.0
   */
  fun clear() {
    values.clear()
  }
  /**
   * Removes a key from a JSONObject
   *
   * @since 1.2.0
   *
   * @throws JSONException if the key does not exist
   */
  fun remove(key: String) {
    checkKey(key)
    values.remove(key)
  }
  // -- CHECK --
  /**
   * Checks if a key exists within the JSONObject
   *
   * @return whether or not the key exists
   */
  fun has(key: String): Boolean {
    return values.containsKey(key)
  }
  /**
   * Checks if the value with the specified key is `null`
   *
   * @throws JSONException if the key does not exist
   */
  fun isNull(key: String): Boolean {
    return checkKey(key) == null
  }
  /**
   * Checks if the value with the specified key is a boolean
   *
   * @throws JSONException if the key does not exist
   */
  fun isBoolean(key: String): Boolean {
    return checkKey(key) is Boolean
  }
  /**
   * Checks if the value with the specified key is a string
   *
   * @throws JSONException if the key does not exist
   */
  fun isString(key: String): Boolean {
    val value = checkKey(key)
    return value is String || value is Instant
  }
  /**
   * Checks if the value with the specified key is a number
   *
   * @throws JSONException if the key does not exist
   */
  fun isNumber(key: String): Boolean {
    val value = checkKey(key)
    return value is Number || value is Instant
  }
  /**
   * Checks if the value with the specified key is a JSONObject
   *
   * @throws JSONException if the key does not exist
   */
  fun isObject(key: String): Boolean {
    return checkKey(key) is JSONObject
  }
  /**
   * Checks if the value with the specified key is a JSONArray
   *
   * @throws JSONException if the key does not exist
   */
  fun isArray(key: String): Boolean {
    return checkKey(key) is JSONArray
  }
  /**
   * Checks if the value with the specified key is an Instant
   * @since 1.1.0
   *
   * @throws JSONException if the key does not exist
   */
  fun isInstant(key: String): Boolean {
    return checkKey(key) is Instant
  }

  /**
   * Returns the value for a given key
   *
   * @throws JSONException if the key does not exist
   */
  operator fun get(key: String): Any? {
    checkKey(key)
    return values[key]
  }
  /**
   * Returns the value as a boolean for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a boolean
   */
  fun getBoolean(key: String): Boolean {
    return checkType<Boolean>(::isBoolean, key, "boolean")!!
  }
  /**
   * Returns the value as a string for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a string
   */
  fun getString(key: String): String {
    return if (isInstant(key)) {
      getInstant(key).toString()
    } else {
      checkType<String>(::isString, key, "string")!!
    }
  }
  /**
   * Returns the value as a number for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a number
   */
  fun getNumber(key: String): Number {
    return if (isInstant(key)) {
      getInstant(key).epochSecond
    } else {
      checkType<Number>(::isNumber, key, "number")!!
    }
  }
  /**
   * Returns the value as a byte for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a byte
   */
  fun getByte(key: String): Byte {
    return getNumber(key).toByte()
  }
  /**
   * Returns the value as a short for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not a short
   */
  fun getShort(key: String): Short {
    return getNumber(key).toShort()
  }
  /**
   * Returns the value as an int for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not an int
   */
  fun getInt(key: String): Int {
    return getNumber(key).toInt()
  }
  /**
   * Returns the value as a long for a given key
   *
   * @throws JSONException if the key does not exist, or if the value is not a long
   */
  fun getLong(key: String): Long {
    return getNumber(key).toLong()
  }
  /**
   * Returns the value as a float for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not a float
   */
  fun getFloat(key: String): Float {
    return getNumber(key).toFloat()
  }
  /**
   * Returns the value as a double for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not a double
   */
  fun getDouble(key: String): Double {
    return getNumber(key).toDouble()
  }

  private fun <T> getNumberExact(
    key: String,
    type: String,
    bigint: Function<BigInteger, T>,
    bigdec: Function<BigDecimal, T>
  ): T {
    val number = getNumber(key)
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
  fun getFloatExact(key: String): Float {
    val num = getNumber(key)
    if (num is Double) // NaN and Infinity
      return num.toFloat()
    val f = num.toFloat()
    if (!java.lang.Float.isFinite(f)) throw mismatch(key, "float")
    return f
  }
  /**
   * Returns the exact value as a double for a given key.
   * This fails if the value does not fit into a double
   *

   *
   * @throws JSONException if the key does not exist, the value is not a double, or if the value does not fit into a double
   */
  fun getDoubleExact(key: String): Double {
    val num = getNumber(key)
    if (num is Double) // NaN and Infinity
      return num
    val d = num.toDouble()
    if (!java.lang.Double.isFinite(d)) throw mismatch(key, "double")
    return d
  }
  /**
   * Returns the value as a JSONObject for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not a JSONObject
   */
  fun getObject(key: String): JSONObject {
    return checkType(::isObject, key, "object")!!
  }
  /**
   * Returns the value as a JSONArray for a given key
   *

   *
   * @throws JSONException if the key does not exist, or if the value is not a JSONArray
   */
  fun getArray(key: String): JSONArray {
    return checkType<JSONArray>(::isArray, key, "array")!!
  }
  /**
   * Returns the value as an Instant for a given key
   *

   * @since 1.1.0
   *
   * @throws JSONException if the key does not exist, or if the value is not an Instant
   */
  fun getInstant(key: String): Instant {
    return checkType<Instant>(::isInstant, key, "instant")!!
  }

  private fun <T> getOpt(key: String, supplier: Function<String, T>, defaults: T): T {
    return try {
      supplier.apply(key)
    } catch (e: Exception) {
      defaults
    }
  }
  /**
   * Returns the value for a given key, or [defaults] if the operation is not possible
   *
   */
  operator fun get(key: String, defaults: Any?): Any? {
    return getOpt(key, ::get, defaults)
  }
  /**
   * Returns the value as a boolean for a given key, or [defaults] if the operation is not possible
   */
  fun getBoolean(key: String, defaults: Boolean): Boolean {
    return getOpt(key, this::getBoolean, defaults)
  }
  /**
   * Returns the value as a string for a given key, or [defaults] if the operation is not possible
   */
  fun getString(key: String, defaults: String): String {
    return getOpt(key, this::getString, defaults)
  }
  /**
   * Returns the value as a number for a given key, or [defaults] if the operation is not possible
   */
  fun getNumber(key: String, defaults: Number): Number {
    return getOpt(key, this::getNumber, defaults)
  }
  /**
   * Returns the value as a byte for a given key, or [defaults] if the operation is not possible
   */
  fun getByte(key: String, defaults: Byte): Byte {
    return getOpt(key, this::getByte, defaults)
  }
  /**
   * Returns the value as a short for a given key, or [defaults] if the operation is not possible
   */
  fun getShort(key: String, defaults: Short): Short {
    return getOpt(
      key, this::getShort, defaults
    )
  }
  /**
   * Returns the value as an int for a given key, or [defaults] if the operation is not possible
   */
  fun getInt(key: String, defaults: Int): Int {
    return getOpt(key, this::getInt, defaults)
  }
  /**
   * Returns the value as a long for a given key, or [defaults] if the operation is not possible
   */
  fun getLong(key: String, defaults: Long): Long {
    return getOpt(key, this::getLong, defaults)
  }
  /**
   * Returns the value as a float for a given key, or [defaults] if the operation is not possible
   */
  fun getFloat(key: String, defaults: Float): Float {
    return getOpt(key, this::getFloat, defaults)
  }
  /**
   * Returns the value as a double for a given key, or [defaults] if the operation is not possible
   */
  fun getDouble(key: String, defaults: Double): Double {
    return getOpt(
      key, this::getDouble, defaults
    )
  }
  /**
   * Returns the exact value as a byte for a given key, or [defaults] if the operation is not possible
   */
  fun getByteExact(key: String, defaults: Byte): Byte {
    return getOpt(
      key, this::getByteExact, defaults
    )
  }
  /**
   * Returns the exact value as a short for a given key, or [defaults] if the operation is not possible
   */
  fun getShortExact(key: String, defaults: Short): Short {
    return getOpt(
      key, this::getShortExact, defaults
    )
  }
  /**
   * Returns the exact value as an int for a given key, or [defaults] if the operation is not possible
   */
  fun getIntExact(key: String, defaults: Int): Int {
    return getOpt(
      key, this::getIntExact, defaults
    )
  }
  /**
   * Returns the exact value as a long for a given key, or [defaults] if the operation is not possible
   */
  fun getLongExact(key: String, defaults: Long): Long {
    return getOpt(
      key, this::getLongExact, defaults
    )
  }
  /**
   * Returns the exact value as a float for a given key, or [defaults] if the operation is not possible
   */
  fun getFloatExact(key: String, defaults: Float): Float {
    return getOpt(
      key, this::getFloatExact, defaults
    )
  }
  /**
   * Returns the exact value as a double for a given key, or [defaults] if the operation is not possible
   */
  fun getDoubleExact(key: String, defaults: Double): Double {
    return getOpt(
      key, this::getDoubleExact, defaults
    )
  }
  /**
   * Returns the value as a JSONObject for a given key, or [defaults] if the operation is not possible
   */
  fun getObject(key: String, defaults: JSONObject): JSONObject {
    return getOpt(
      key, this::getObject, defaults
    )
  }
  /**
   * Returns the value as a JSONArray for a given key, or [defaults] if the operation is not possible
   */
  fun getArray(key: String, defaults: JSONArray): JSONArray {
    return getOpt(key, this::getArray, defaults)
  }
  /**
   * Returns the value as an Instant for a given key, or [defaults] if the operation is not possible
   *
   * @since 1.1.0
   */
  fun getInstant(key: String, defaults: Instant): Instant {
    return getOpt(
      key, this::getInstant, defaults
    )
  }

  /**
   * Sets the value at a given key
   *
   * @return this JSONObject
   */
  operator fun set(key: String, value: Any?): JSONObject {
    values[key] = sanitize(value)
    return this
  }

  /**
   * Converts the JSONObject into its string representation.
   * The indentation factor enables pretty-printing and defines
   * how many spaces (' ') should be placed before each key/value pair.
   * A factor of `< 1` disables pretty-printing and discards
   * any optional whitespace characters.
   *
   *
   * `indentFactor = 2`:
   * <pre>
   * {
   * "key0": "value0",
   * "key1": {
   * "nested": 123
   * },
   * "key2": false
   * }
  </pre> *
   *
   *
   * `indentFactor = 0`:
   * <pre>
   * {"key0":"value0","key1":{"nested":123},"key2":false}
  </pre> *
   *
   * @param indentFactor the indentation factor

   *
   * @see JSONStringify.toString
   */
  fun prettyPrint(indentFactor: Int, options: JSONOptions): String {
    return JSONStringify.jsonObjectToString(this, indentFactor = indentFactor, options = options)
  }
  /**
   * Converts the JSONObject into its compact string representation.
   */
  override fun toString(): String {
    return prettyPrint(0, JSONOptions())
  }

  private fun checkKey(key: String): Any? {
    if (!values.containsKey(key)) throw JSONException(
      "JSONObject[" + JSONStringify.quote(
        key,
        JSONOptions()
      ) + "] does not exist"
    )
    return values[key]
  }

  private fun <T> checkType(predicate: Predicate<String>, key: String, type: String): T? {
    if (!predicate.test(key)) throw mismatch(key, type)
    return values[key] as T?
  }

  companion object {

    private fun mismatch(key: String, type: String): JSONException {
      return JSONException(
        "JSONObject[" + JSONStringify.quote(
          key,
          JSONOptions()
        ) + "] is not of type " + type
      )
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
