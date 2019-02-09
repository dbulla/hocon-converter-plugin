package com.nurflugel.hocon.generators

import com.nurflugel.hocon.parsers.HoconParser
import com.nurflugel.hocon.parsers.domain.*
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils

object ConfGenerator {
  /** take a map of property folders and values, output to conf format
   * Basically, if a key has a map under it, print out the key (add 1 to the indent level) and a {
   *
   * When a key has a String/numeric/Boolean value, then print out the key = value
   *
   * Done with the contents of a map?  Then print  a } and decrease the indent
   * */
  internal fun generateConfOutput(propsMap: PropertiesMap): MutableList<String> {
    val lines = mutableListOf<String>()

    // add any includes first
    propsMap.includesList.forEach { lines.add(it) }
    if (propsMap.includesList.isNotEmpty()) lines.add("")

    // add the properties transformed into map format
    outputMap(propsMap.map, 0, lines)

    // trim the last blank line if there is one
    if (lines.last().isBlank()) {
      lines.removeAt(lines.size - 1)
      //      return lines.subList(0, lines.size - 1)
    }
    return lines
  }

  /** Output the map into conf format */
  private fun outputMap(propsMap: HoconMap, initialIndentLevel: Int, lines: MutableList<String>): Int {
    var indentLevel = initialIndentLevel;
    for (key in propsMap.getKeys().toSortedSet()) {

      val value = propsMap.get(key)
      // is the value a map, or a key?
      val whiteSpace = StringUtils.repeat("  ", indentLevel)
      // increase the indent level
      when (value) {
        is HoconMap -> {
          indentLevel = outputMapLines(value as HoconMap, lines, whiteSpace, indentLevel, key)
        }
        is HoconList -> indentLevel = outputListLines(value, lines, whiteSpace, indentLevel, key)
        else -> {
          outputPropertyLines(value, lines, whiteSpace, key)
        }
      }
    }

    indentLevel--
    if (indentLevel >= 0) {// add closing }
      val whiteSpace = StringUtils.repeat("  ", indentLevel)
      lines.add("$whiteSpace}")
    }
    if (indentLevel == 0) { // add padding between maps
      lines.add("")
    }
    return indentLevel
  }

  /** output the list */
  private fun outputListLines(list: HoconList, lines: MutableList<String>, whiteSpace: String?, indentLevel: Int, key: String?): Int {

    lines.add("$whiteSpace$key [")
    for (value in list.values.withIndex()) {
      var quotedValue = writeText(value.value)
      if (!quotedValue.endsWith(",")) {
        if (value.index < list.values.size - 1) {
          quotedValue += ","
        }
      }
      lines.add("$whiteSpace  $quotedValue")
    }
    lines.add("$whiteSpace]")
    return indentLevel
  }

  private fun outputPropertyLines(value: HoconType?, lines: MutableList<String>, whiteSpace: String?, key: String?) {
    val hoconType = value as HoconType
    var textValue = hoconType.toName()

    textValue = writeText(textValue)// add quotes if none exist (check for number/booleans first)
    lines.add("$whiteSpace$key = $textValue")
  }

  private fun outputMapLines(value: HoconMap, lines: MutableList<String>, whiteSpace: String?, indentLevel: Int, key: String?): Int {
    // check to see if the value for this key has any maps under it with nothing more than a single
    // key/value at the end - if so, just output
    var indentLevel1 = indentLevel
    if (false) {
      if (HoconParser.isSingleKeyValue(value)) {

        val wholeKey: Pair<StringBuilder, String> = getWholeKeyValue(value, StringBuilder())
        lines.add("""$whiteSpace${wholeKey.first} = ${wholeKey.second}""")
      }
    }
    indentLevel1++
    lines.add("$whiteSpace$key {")
    indentLevel1 = outputMap(value, indentLevel1, lines)
    return indentLevel1
  }

  /** Return a single key/value, even through it's a series of map keys and concatenate into a single key/value pair.
   * This method assumes that each map only contains one key down to the final value */
  private fun getWholeKeyValue(map: HoconMap, stringBuilder: StringBuilder): Pair<StringBuilder, String> {
    val key = map.getKeys().first()
    if (stringBuilder.isNotEmpty()) stringBuilder.append(".")
    stringBuilder.append(key)
    val value = map.get(key) as HoconType
    if (value is HoconMap) {
      return getWholeKeyValue(value, stringBuilder)
    }
    // it's not a map, so it's the final value - return
    return Pair(stringBuilder, (value as HoconString).toString())
  }

  /** If the value is a String, ensure it's wrapped in quotes.  Numbers or Booleans, however, are ok as-is */
  internal fun writeText(textValue: String): String {

    return when {
      // It's a number
      NumberUtils.isParsable(textValue) -> textValue
      // It's a boolean
      BooleanUtils.toBooleanObject(textValue) != null -> textValue
      // It's a String
      else -> {
        var result = textValue

        // wrap in quotes unless it's a list
        if (!result.contains('[') && !result.startsWith('"')) result = """"$result""""
        result
      }
    }
  }

}