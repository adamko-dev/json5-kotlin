# ⚠️ Status - recommended replacement: https://github.com/xn32/json5k

[json5-kotlin](https://github.com/adamko-dev/json5-kotlin) was made as a proof-of-concept, and while it works, I have not made any further development efforts. 

You are welcome to use this library, however I recommend you use https://github.com/xn32/json5k instead, because it better supports Kotlinx Serialization.

----
----
----

[![](https://jitpack.io/v/adamko-dev/json5-kotlin.svg?style=flat-square)](https://jitpack.io/#adamko-dev/json5-kotlin)

# [json5 Kotlin](https://github.com/adamko-dev/json5-kotlin)

A JSON5 Library for Kotlin.

## Overview

The [JSON5 Standard](https://json5.org/) tries to make JSON more human-readable

This is a reference implementation, capable of parsing JSON5 data according to
the [specification](https://spec.json5.org/).

## Getting started

Gradle (Kotlin):

```kotlin
repositories {
  maven("https://jitpack.io")
}

dependencies {
  implementation("com.github.adamko-dev:json5-kotlin:$json5KotlinVersion")
}
```

Maven:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

```xml
<dependencies>
  <dependency>
	  <groupId>com.github.adamko-dev</groupId>
    <artifactId>json5-kotlin</artifactId>
    <version>${json5-kotlin.version}</version>
  </dependency>
</dependencies>
```

### Usage

```kotlin
import at.syntaxerror.json5.Json5Module
import kotlinx.serialization.json.JsonObject

// create and configure the Json5Module
val j5 = Json5Module {
  allowInfinity = true
  indentFactor = 4u
}

val json5 = """
    {
      // comments
      unquoted: 'and you can quote me on that',
      singleQuotes: 'I can use "double quotes" here',
      lineBreaks: "Look, Mom! \
    No \\n's!",
      hexadecimal: 0xdecaf,
      leadingDecimalPoint: .8675309,
      andTrailing: 8675309.,
      positiveSign: +1,
      trailingComma: 'in objects',
      andIn: [
        'arrays',
      ],
      "backwardsCompatible": "with JSON",
    }
  """.trimIndent()

// Parse a JSON5 String to a Kotlinx Serialization JsonObject
val jsonObject: JsonObject = j5.decodeObject(json5)

// encode the JsonObject to a Json5 String
val jsonString = j5.encodeToString(jsonObject)

println(jsonString)
/* 
{
  "unquoted": "and you can quote me on that",
  "singleQuotes": "I can use \"double quotes\" here",
  "lineBreaks": "Look, Mom! No \\n's!",
  "hexadecimal": 912559,
  "leadingDecimalPoint": 0.8675309,
  "andTrailing": 8675309.0,
  "positiveSign": 1,
  "trailingComma": "in objects",
  "andIn": [
  "arrays"
  ],
  "backwardsCompatible": "with JSON"
}
*/
```

## Credits

This project is entirely based on [Synt4xErr0r4/json5](https://github.com/Synt4xErr0r4/json5/),
which was partly based on stleary's [JSON-Java](https://github.com/stleary/JSON-java) library.

## License

This project is licensed under
the [MIT License](https://github.com/Synt4xErr0r4/json5/blob/main/LICENSE)
