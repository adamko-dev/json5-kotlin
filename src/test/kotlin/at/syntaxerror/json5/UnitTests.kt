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

import at.syntaxerror.json5.config.Json5Options.StringifyOptions.Format
import at.syntaxerror.json5.structure.JSONArray
import at.syntaxerror.json5.structure.JSONObject
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class UnitTests {
  @Test
  fun testDoubleQuoted() {
    Assertions.assertEquals(
      "Test \" 123", parse("{ a: \"Test \\\" 123\" }")["a"]
    )
  }

  @Test
  fun testSingleQuoted() {
    Assertions.assertEquals(
      "Test ' 123", parse("{ a: 'Test \\' 123' }")["a"]
    )
  }

  @Test
  fun testMixedQuoted() {
    Assertions.assertEquals(
      "Test ' 123", parse("{ a: \"Test \\' 123\" }")["a"]
    )
  }

  @Test
  fun testStringify() {
    val jsonMapper = Json5Module {
      stringifyOptions.stringifyUnixInstants = true
      stringifyOptions.formatting = Format.Pretty()
    }

    val json = JSONObject()
    json["a"] = null as Any?
    json["b"] = false
    json["c"] = true
    json["d"] = JSONObject()
    json["e"] = JSONArray()
    json["f"] = Double.NaN
    json["g"] = 123e+45
    json["h"] = (-123e45).toFloat()
    json["i"] = 123L
    json["j"] = "Lorem Ipsum"
    json["k"] = Instant.now(clock)

    val actual = jsonMapper.serializeToString(json)

    @Language("JSON5")
    val expected = """
      |{
      |  'a': null,
      |  'b': false,
      |  'c': true,
      |  'd': {
      |  },
      |  'e': [
      |  ],
      |  'f': NaN,
      |  'g': 1.23E+47,
      |  'h': -Infinity,
      |  'i': 123,
      |  'j': 'Lorem Ipsum',
      |  'k': 1639351848
      |  }
    """.trimMargin( )
    Assertions.assertEquals(expected, actual)
    // TODO fix indentation of last bracket
  }

  @Test
  fun testEscapes() {
    Assertions.assertEquals(
      "\n\r\u000c\b\t\u000B\u0000\u12Fa\u007F",
      parse("{ a: \"\\n\\r\\u000c\\b\\t\\v\\0\\u12Fa\\x7F\" }")["a"] as? String
    )
  }

  @Test
  fun testMemberName() {

    Assertions.assertTrue(
      parse("{ \$Lorem\\u0041_Ipsum123指事字: 0 }").containsKey("\$LoremA_Ipsum123指事字")
    )
  }

  @Test
  fun testMultiComments() {
    Assertions.assertTrue(parse("/**/{/**/a/**/:/**/'b'/**/}/**/").containsKey("a"))
  }

  @Test
  fun testSingleComments() {
    Assertions.assertTrue(
      parse("// test\n{ // lorem ipsum\n a: 'b'\n// test\n}// test").containsKey(
        "a"
      )
    )
  }

//  @Test
//  fun testInstant() {
//    Assertions.assertInstanceOf(Instant::class.java, parse("{a:1338150759534}")["a"])
//    Assertions.assertEquals(parse("{a:1338150759534}").getLong("a"), 1338150759534L)
//    Assertions.assertEquals(
//      parse("{a:'2001-09-09T01:46:40Z'}").getString("a"),
//      "2001-09-09T01:46:40Z"
//    )
//  }

//  @Test
//  fun testHex() {
//    Assertions.assertEquals(0xCAFEBABEL, parse("{a: 0xCAFEBABE}").getLong("a"))
//  }

//  @Test
//  fun testSpecial() {
//    Assertions.assertTrue(java.lang.Double.isNaN(parse("{a: +NaN}").getDouble("a")))
//    Assertions.assertTrue(java.lang.Double.isInfinite(parse("{a: -Infinity}").getDouble("a")))
//  }

  fun parse(str: String): JSONObject {
    return Json5Module().parseString(str)

  }

  companion object {
    private val clock = Clock.fixed(Instant.ofEpochSecond(1639351848L), ZoneId.of("UTC"))
  }
//
//  companion object {
//    @BeforeAll
//    fun setUpBeforeClass() {
//      // compile regex patterns
//      JSONParser::class.java.toString()
//    }
//  }
}
