package at.syntaxerror.json5


import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.intellij.lang.annotations.Language

class Json5Module(
  configure: JSONOptions.() -> Unit = {}
) {
  internal val options: JSONOptions = JSONOptions().apply(configure)
  internal val stringify: JSONStringify = JSONStringify(options)

  internal val arrayDecoder: DecodeJson5Array = DecodeJson5Array()
  internal val objectDecoder: DecodeJson5Object = DecodeJson5Object(this)

  fun decodeObject(@Language("JSON5") string: String): JsonObject = decodeObject(string.reader())
  fun decodeObject(stream: InputStream): JsonObject = decodeObject(InputStreamReader(stream))

  fun decodeObject(reader: Reader): JsonObject {
    return reader.use { r ->
      val parser = JSONParser(r, this)
      objectDecoder.decode(parser)
    }
  }

  fun decodeArray(@Language("JSON5") string: String): JsonArray = decodeArray(string.reader())
  fun decodeArray(stream: InputStream): JsonArray = decodeArray(InputStreamReader(stream))

  fun decodeArray(reader: Reader): JsonArray {
    return reader.use { r ->
      val parser = JSONParser(r, this)
      arrayDecoder.decode(parser)
    }
  }

  fun encodeToString(array: JsonArray) = stringify.encodeArray(array)
  fun encodeToString(jsonObject: JsonObject) = stringify.encodeObject(jsonObject)

}
