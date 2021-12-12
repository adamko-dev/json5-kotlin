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
 * A JSONArray is an array structure capable of holding multiple values,
 * including other JSONArrays and [JSONObjects][JSONObject]
 *
 * @author SyntaxError404
 */
class JSONArray() : Iterable<Any?> {
  private val values: MutableList<Any?> = ArrayList()

  /**
   * Constructs a new JSONArray from a string
   */
  constructor(source: String) : this(JSONParser(source))

  /**
   * Constructs a new JSONArray from a JSONParser
   */
  constructor(parser: JSONParser) : this() {
    var c: Char
    if (parser.nextClean() != '[') throw JSONSyntaxError("A JSONArray must begin with '['")
    while (true) {
      c = parser.nextClean()
      when (c) {
        Char.MIN_VALUE -> throw JSONSyntaxError("A JSONArray must end with ']'")
        ']'            -> return
        else           -> parser.back()
      }
      val value = parser.nextValue()
      values.add(value as Any)
      c = parser.nextClean()
      if (c == ']') return
      if (c != ',') throw JSONSyntaxError("Expected ',' or ']' after value, got '$c' instead")
    }
  }

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
  /**
   * Returns a collection of values of the JSONArray.
   * Modifying the collection will modify the JSONArray
   *
   * Use with caution.
   *
   * @return a set of entries
   */
  fun entrySet(): Collection<Any?> {
    return values
  }

  override fun iterator(): Iterator<Any?> {
    return values.iterator()
  }
  /**
   * Returns the number of values in the JSONArray
   *
   * @return the number of values
   */
  fun length(): Int {
    return values.size
  }
  /**
   * Removes all values from this JSONArray
   *
   * @since 1.2.0
   */
  fun clear() {
    values.clear()
  }
  /**
   * Removes the value at an index from a JSONArray
   * to be removed
   * @since 1.2.0
   *
   * @throws JSONException if the index does not exist
   */
  fun remove(index: Int) {
    checkIndex(index)
    values.removeAt(index)
  }
  // -- CHECK --
  /**
   * Checks if the value with the specified index is `null`
   *
   * @return whether or not the value is `null`
   *
   * @throws JSONException if the index does not exist
   */
  fun isNull(index: Int): Boolean {
    return checkIndex(index) == null
  }
  /**
   * Checks if the value with the specified index is a boolean
   *
   * @return whether or not the value is a boolean
   *
   * @throws JSONException if the index does not exist
   */
  fun isBoolean(index: Int): Boolean {
    return checkIndex(index) is Boolean
  }
  /**
   * Checks if the value with the specified index is a string
   *
   * @return whether or not the value is a string
   *
   * @throws JSONException if the index does not exist
   */
  fun isString(index: Int): Boolean {
    val value = checkIndex(index)
    return value is String || value is Instant
  }
  /**
   * Checks if the value with the specified index is a number
   *
   * @return whether or not the value is a number
   *
   * @throws JSONException if the index does not exist
   */
  fun isNumber(index: Int): Boolean {
    val value = checkIndex(index)
    return value is Number || value is Instant
  }
  /**
   * Checks if the value with the specified index is a JSONObject
   *
   * @return whether or not the value is a JSONObject
   *
   * @throws JSONException if the index does not exist
   */
  fun isObject(index: Int): Boolean {
    return checkIndex(index) is JSONObject
  }
  /**
   * Checks if the value with the specified index is a JSONArray
   *
   * @return whether or not the value is a JSONArray
   *
   * @throws JSONException if the index does not exist
   */
  fun isArray(index: Int): Boolean {
    return checkIndex(index) is JSONArray
  }
  /**
   * Checks if the value with the specified index is an Instant
   *
   * @since 1.1.0
   *
   * @throws JSONException if the index does not exist
   */
  fun isInstant(index: Int): Boolean {
    return checkIndex(index) is Instant
  }
  // -- GET --
  /**
   * Returns the value for a given index
   *
   * @return the value
   *
   * @throws JSONException if the index does not exist
   */
  operator fun get(index: Int): Any? {
    checkIndex(index)
    return values[index]
  }
  /**
   * Returns the value as a boolean for a given index
   *
   * @return the boolean
   *
   * @throws JSONException if the index does not exist, or if the value is not a boolean
   */
  fun getBoolean(index: Int): Boolean {
    return checkType(::isBoolean, index, "boolean")
  }
  /**
   * Returns the value as a string for a given index
   *
   * @return the string
   *
   * @throws JSONException if the index does not exist, or if the value is not a string
   */
  fun getString(index: Int): String {
    return checkType(::isString, index, "string")
  }
  /**
   * Returns the value as a number for a given index
   *
   * @return the number
   *
   * @throws JSONException if the index does not exist, or if the value is not a number
   */
  fun getNumber(index: Int): Number {
    return checkType(::isNumber, index, "number")
  }
  /**
   * Returns the value as a byte for a given index
   *
   * @return the byte
   *
   * @throws JSONException if the index does not exist, or if the value is not a byte
   */
  fun getByte(index: Int): Byte {
    return getNumber(index).toByte()
  }
  /**
   * Returns the value as a short for a given index
   *
   * @return the short
   *
   * @throws JSONException if the index does not exist, or if the value is not a short
   */
  fun getShort(index: Int): Short {
    return getNumber(index).toShort()
  }
  /**
   * Returns the value as an int for a given index
   *
   * @return the int
   *
   * @throws JSONException if the index does not exist, or if the value is not an int
   */
  fun getInt(index: Int): Int {
    return getNumber(index).toInt()
  }
  /**
   * Returns the value as a long for a given index
   *
   * @return the long
   *
   * @throws JSONException if the index does not exist, or if the value is not a long
   */
  fun getLong(index: Int): Long {
    return getNumber(index).toLong()
  }
  /**
   * Returns the value as a float for a given index
   *
   * @return the float
   *
   * @throws JSONException if the index does not exist, or if the value is not a float
   */
  fun getFloat(index: Int): Float {
    return getNumber(index).toFloat()
  }
  /**
   * Returns the value as a double for a given index
   *
   * @return the double
   *
   * @throws JSONException if the index does not exist, or if the value is not a double
   */
  fun getDouble(index: Int): Double {
    return getNumber(index).toDouble()
  }

  private fun <T> getNumberExact(
    index: Int,
    type: String,
    bigint: Function<BigInteger, T>,
    bigdec: Function<BigDecimal, T>
  ): T {
    val number = getNumber(index)
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
    val num = getNumber(index)
    if (num is Double) // NaN and Infinity
      return num.toFloat()
    val f = num.toFloat()
    if (!java.lang.Float.isFinite(f)) throw mismatch(index, "float")
    return f
  }
  /**
   * Returns the exact value as a double for a given index.
   * This fails if the value does not fit into a double
   *
   * @throws JSONException if the index does not exist, the value is not a double, or if the value does not fit into a double
   */
  fun getDoubleExact(index: Int): Double {
    val num = getNumber(index)
    if (num is Double) // NaN and Infinity
      return num
    val d = num.toDouble()
    if (!java.lang.Double.isFinite(d)) throw mismatch(index, "double")
    return d
  }
  /**
   * Returns the value as a JSONObject for a given index
   *
   * @return the JSONObject
   *
   * @throws JSONException if the index does not exist, or if the value is not a JSONObject
   */
  fun getObject(index: Int): JSONObject {
    return checkType(::isObject, index, "object")
  }
  /**
   * Returns the value as a JSONArray for a given index
   *
   * @return the JSONArray
   *
   * @throws JSONException if the index does not exist, or if the value is not a JSONArray
   */
  fun getArray(index: Int): JSONArray {
    return checkType(::isArray, index, "array")
  }
  /**
   * Returns the value as an Instant for a given index
   *
   * @return the Instant
   * @since 1.1.0
   *
   * @throws JSONException if the index does not exist, or if the value is not an Instant
   */
  fun getInstant(index: Int): Instant {
    return checkType(::isInstant, index, "instant")
  }

  private fun <T> getOpt(index: Int, supplier: Function<Int, T>, defaults: T): T {
    return try {
      supplier.apply(index)
    } catch (e: Exception) {
      defaults
    }
  }

  /**
   * Returns the value for a given index, or [defaults] if the operation is not possible
   */
  operator fun get(index: Int, defaults: Any): Any? {
    return getOpt(index, ::get, defaults)
  }
  /**
   * Returns the value as a boolean for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getBoolean(index: Int, defaults: Boolean): Boolean {
    return getOpt(index, this::getBoolean, defaults)
  }
  /**
   * Returns the value as a string for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getString(index: Int, defaults: String): String {
    return getOpt(index, this::getString, defaults)
  }
  /**
   * Returns the value as a number for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getNumber(index: Int, defaults: Number): Number {
    return getOpt(index, this::getNumber, defaults)
  }
  /**
   * Returns the value as a byte for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getByte(index: Int, defaults: Byte): Byte {
    return getOpt(index, this::getByte, defaults)
  }
  /**
   * Returns the value as a short for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getShort(index: Int, defaults: Short): Short {
    return getOpt(index, this::getShort, defaults)
  }
  /**
   * Returns the value as an int for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getInt(index: Int, defaults: Int): Int {
    return getOpt(index, this::getInt, defaults)
  }
  /**
   * Returns the value as a long for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getLong(index: Int, defaults: Long): Long {
    return getOpt(index, this::getLong, defaults)
  }
  /**
   * Returns the value as a float for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getFloat(index: Int, defaults: Float): Float {
    return getOpt(index, this::getFloat, defaults)
  }
  /**
   * Returns the value as a double for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getDouble(index: Int, defaults: Double): Double {
    return getOpt(index, this::getDouble, defaults)
  }
  /**
   * Returns the exact value as a byte for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getByteExact(index: Int, defaults: Byte): Byte {
    return getOpt(index, this::getByteExact, defaults)
  }
  /**
   * Returns the exact value as a short for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getShortExact(index: Int, defaults: Short): Short {
    return getOpt(index, this::getShortExact, defaults)
  }
  /**
   * Returns the exact value as an int for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getIntExact(index: Int, defaults: Int): Int {
    return getOpt(index, this::getIntExact, defaults)
  }
  /**
   * Returns the exact value as a long for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getLongExact(index: Int, defaults: Long): Long {
    return getOpt(index, this::getLongExact, defaults)
  }
  /**
   * Returns the exact value as a float for a given index, or [defaults] if the operation is not possible
   */
  fun getFloatExact(index: Int, defaults: Float): Float {
    return getOpt(index, this::getFloatExact, defaults)
  }
  /**
   * Returns the exact value as a double for a given index, or [defaults] if the operation is not possible
   */
  fun getDoubleExact(index: Int, defaults: Double): Double {
    return getOpt(index, this::getDoubleExact, defaults)
  }
  /**
   * Returns the value as a JSONObject for a given index, or [defaults] if the operation is not possible
   */
  fun getObject(index: Int, defaults: JSONObject): JSONObject {
    return getOpt(index, this::getObject, defaults)
  }

  /**
   * Returns the value as a JSONArray for a given index, or [defaults] if the operation is not possible
   *
   */
  fun getArray(index: Int, defaults: JSONArray): JSONArray {
    return getOpt(index, this::getArray, defaults)
  }

  /**
   * Returns the value as an Instant for a given index, or [defaults] if the operation is not possible
   * @since 1.1.0
   */
  fun getInstant(index: Int, defaults: Instant): Instant {
    return getOpt(index, this::getInstant, defaults)
  }

  /**
   * Adds a value to the JSONArray
   *
   * @return this JSONArray
   */
  fun add(value: Any?): JSONArray {
    values.add(JSONObject.sanitize(value))
    return this
  }

  /**
   * Inserts a value to the JSONArray at a given index
   *
   * @since 1.1.0
   */
  fun insert(index: Int, value: Any?): JSONArray {
    if (index < 0 || index > length()) throw JSONException("JSONArray[$index] is out of bounds")
    values.add(index, JSONObject.sanitize(value))
    return this
  }

  /**
   * Sets the value at a given index
   *
   * @return this JSONArray
   */
  operator fun set(index: Int, value: Any?): JSONArray {
    checkIndex(index)
    values[index] = JSONObject.sanitize(value)
    return this
  }

  /**
   * Converts the JSONArray into its string representation.
   * The indentation factor enables pretty-printing and defines
   * how many spaces (' ') should be placed before each value.
   * A factor of `< 1` disables pretty-printing and discards
   * any optional whitespace characters.
   *
   * ```
   *
   * ```
   * `indentFactor = 2`:
   * <pre>
   * [
   * "value",
   * {
   * "nested": 123
   * },
   * false
   * ]
  </pre> *
   *
   *
   * `indentFactor = 0`:
   * <pre>
   * ["value",{"nested":123},false]
  </pre> *
   *
   * @param indentFactor the indentation factor
   * @return the string representation
   *
   * @see JSONStringify.toString
   */
  fun prettyPrint(indentFactor: Int, options: JSONOptions): String {
    return JSONStringify.jsonArrayToString(this, indentFactor = indentFactor, options = options)
  }
  /**
   * Converts the JSONArray into its compact string representation.
   *
   * @return the compact string representation
   */
  override fun toString(): String {
    return prettyPrint(0, JSONOptions())
  }

  private fun checkIndex(index: Int): Any? {
    if (index < 0 || index >= length()) throw JSONException("JSONArray[$index] does not exist")
    return values[index]
  }

  private fun <T> checkType(predicate: Predicate<Int>, index: Int, type: String): T {
    if (!predicate.test(index)) throw mismatch(index, type)
    return values[index] as T
  }

  companion object {
    private fun mismatch(index: Int, type: String): JSONException {
      return JSONException("JSONArray[$index] is not of type $type")
    }
  }
}
