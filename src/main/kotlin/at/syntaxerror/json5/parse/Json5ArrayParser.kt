package at.syntaxerror.json5.parse

import at.syntaxerror.json5.error.JSONException
import at.syntaxerror.json5.config.Json5Options
import java.io.Reader


class Json5ArrayParser(
  reader: Reader,
  private val options: Json5Options = Json5Options(),
) : JSONParser(reader, options) {

  /**
   * Constructs a new [JSONArray] from a [JSONParser]
   */
  fun parse(): MutableList<*> {
    val internalArray: MutableList<Any?> = mutableListOf()

    var c: Char
    if (nextClean() != '[') throw JSONException.JSONSyntaxError("A JSONArray must begin with '['")
    while (true) {
      c = nextClean()
      when (c) {
        Char.MIN_VALUE -> throw JSONException.JSONSyntaxError("A JSONArray must end with ']'")
        ']'            -> break
        else           -> back()
      }
      val value = nextValue()
      internalArray.add(value)
      c = nextClean()
      if (c == ']') break
      if (c != ',') throw JSONException.JSONSyntaxError("Expected ',' or ']' after value, got '$c'")
    }
    return internalArray
  }

}
