package at.syntaxerror.json5.constants

object CharUnicode {

  const val LINE_FEED = '\u000A'
  const val LINE_TABULATION = '\u000B'
  const val FORM_FEED = '\u000C'
  const val CARRIAGE_RETURN = '\u000D'
  const val NEXT_LINE = '\u0085'
  const val LINE_SEPARATOR = '\u2028'
  const val PARAGRAPH_SEPARATOR = '\u2029'

  val VERTICAL = setOf(
    LINE_FEED,
    LINE_TABULATION,
    FORM_FEED,
    CARRIAGE_RETURN,
    NEXT_LINE,
    LINE_SEPARATOR,
    PARAGRAPH_SEPARATOR,
  )
}