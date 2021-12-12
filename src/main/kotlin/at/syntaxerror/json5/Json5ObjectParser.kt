package at.syntaxerror.json5

import at.syntaxerror.json5.config.Json5Options
import java.io.Reader

class Json5ObjectParser(
  reader: Reader,
      options: Json5Options = Json5Options(),
) : JSONParser(reader, options) {

  /**
   * Constructs a new JSONObject from a JSONParser
   */
  fun parseObject(): JSONObject {
    val internalMap: MutableMap<String, Any?> = mutableMapOf()

    var c: Char
    var key: String
    if (nextClean() != '{') throw JSONException.JSONSyntaxError("A JSONObject must begin with '{'")
    while (true) {
      c = nextClean()
      key = when (c) {
        Char.MIN_VALUE -> throw JSONException.JSONSyntaxError("A JSONObject must end with '}'")
        '}'            -> break
        else           -> {
          back()
          nextMemberName()
        }
      }
      if (internalMap.containsKey(key))
//        throw JSONException("Duplicate key " + JSONStringify.quote(key)))
        throw JSONException("Duplicate key $key")
      c = nextClean()
      if (c != ':') throw JSONException.JSONSyntaxError("Expected ':' after a key, got '$c' instead")
      val value = nextValue()
      internalMap[key] = value
      c = nextClean()
      if (c == '}') break
      if (c != ',') throw JSONException.JSONSyntaxError("Expected ',' or '}' after value, got '$c' instead")
    }

    return JSONObject(internalMap)
  }
}
