package at.syntaxerror.json5

import at.syntaxerror.json5.config.Json5Options
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader

data class Json5Module(
  val configure: Json5Options.() -> Unit = {}
) {
  private val options: Json5Options = Json5Options().apply { configure() }
  private val stringify: JSONStringify = JSONStringify(options)

  /**
   * Constructs a new JSONParser from an InputStream. The stream is not [closed][InputStream.close].
   *
   * @since 1.1.0
   */
  fun parseInputStream(stream: InputStream) {
    val parser: JSONParser = JSONParser(InputStreamReader(stream), options)
  }

  /**
   * Constructs a new JSONParser from a string
   */
  fun parseString(source: String): JSONObject {
    val parser: Json5ObjectParser = Json5ObjectParser(StringReader(source), options)
    return parser.parseObject()
  }

  fun serializeToString(jsonObject: JSONObject): String {
    return stringify.anyToString(jsonObject)
  }
}
