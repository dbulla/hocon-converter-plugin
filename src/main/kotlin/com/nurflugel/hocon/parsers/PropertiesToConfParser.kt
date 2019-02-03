package com.nurflugel.hocon.parsers

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils

class PropertiesToConfParser {
  companion object {


    fun convertPropertiesToConf(existingLines: List<String>): MutableList<String> {
      val propsMap: PropertiesMap = populatePropsMap(existingLines)
      return generateConfOutput(propsMap)
    }

    /** take a map of property folders and values, output to conf format
     * Basically, if a key has a map under it, print out the key (add 1 to the indent level) and a {
     *
     * When a key has a String/numeric/Boolean value, then print out the key = value
     *
     * Done with the contents of a map?  Then print  a } and decrease the indent
     * */
    private fun generateConfOutput(propsMap: PropertiesMap): MutableList<String> {
      val lines = mutableListOf<String>()
      // add the includes
      propsMap.includesList.forEach { lines.add(it) }
      if (propsMap.includesList.isNotEmpty()) lines.add("")

      // add the properties transformed into map format
      outputMap(propsMap.map, 0, lines, true)

      // trim the last blank line if there is one
      if (lines.last().isBlank()) {
        lines.removeAt(lines.size - 1)
        //      return lines.subList(0, lines.size - 1)
      }
      return lines
    }

    /** Output the map into conf format */
    private fun outputMap(propsMap: Map<String, Any>, initialIndentLevel: Int, lines: MutableList<String>, addBlankLineAfterKey: Boolean): Int {
      var indentLevel = initialIndentLevel;
      for (key in propsMap.keys.toSortedSet()) {

        val value = propsMap[key]
        // is the value a map, or a key?
        val whiteSpace = StringUtils.repeat("  ", indentLevel)
        when (value) {
          is Map<*, *> -> {// increase the indent level
            // check to see if the value for this key has any maps under it with nothing more than a single
            // key/value at the end - if so, just output
            if (false) {
              if (isSingleKeyValue(value)) {

                val wholeKey: Pair<StringBuilder, String> = getWholeKeyValue(value, StringBuilder())
                lines.add("""$whiteSpace${wholeKey.first} = ${wholeKey.second}""")
              }
            }
            indentLevel++
            lines.add("$whiteSpace$key {")
            indentLevel = outputMap(value as Map<String, Any>, indentLevel, lines, false)
          }
          else -> {
            var textValue = value as String
            textValue = writeText(textValue)// add quotes if none exist (check for number/booleans first)
            lines.add("$whiteSpace$key = $textValue")
          }
        }
      }

      indentLevel--
      if (indentLevel >= 0) {
        val whiteSpace = StringUtils.repeat("  ", indentLevel)
        lines.add("$whiteSpace}")
      }
      if (indentLevel == 0) {
//      if (addBlankLineAfterKey)
        lines.add("")
      }
      return indentLevel
    }

    /** Go through the map and see if there are any more than one key for any sub-map */
    fun isSingleKeyValue(map: Map<*, *>): Boolean {
      if (map.keys.size > 1) return false
      val values = map.values
      if (values.isEmpty()) return false
      val value = values.first()
      if (value is String) return true
      return isSingleKeyValue(value as Map<*, *>)
    }

    /** Return a single key/value, even through it's a series of map keys and concatenate into a single key/value pair.
     * This method assumes that each map only contains one key down to the final value */
    private fun getWholeKeyValue(map: Map<*, *>, stringBuilder: StringBuilder): Pair<StringBuilder, String> {
      val key = map.keys.first() as String
      if (stringBuilder.isNotEmpty()) stringBuilder.append(".")
      stringBuilder.append(key)
      val value = map[key]
      if (value is Map<*, *>) {
        return getWholeKeyValue(value, stringBuilder)
      }
      // it's not a map, so it's the final value - return
      return Pair(stringBuilder, value as String)
    }

    /** If the value is a String, ensure it's wrapped in quotes.  Numbers or Booleans, however, are ok as-is */
    private fun writeText(textValue: String): String {

      return when {
        // It's a number
        NumberUtils.isParsable(textValue) -> textValue
        // It's a boolean
        BooleanUtils.toBooleanObject(textValue) != null -> textValue
        // It's a String
        else -> {
          var textValue1 = textValue
          if (!textValue1.startsWith('"')) textValue1 = """"$textValue1""""
          textValue1
        }
      }
    }

    /** We're taking in a list of key-value pairs (no {} mapping and creating the map */
    private fun populatePropsMap(existingLines: List<String>): PropertiesMap {
      val propsMap = PropertiesMap()

      // add any includes
      existingLines
        .map { it.trim() }
        .filter { it.startsWith("include") }
        .forEach { propsMap.addInclude(it) }

      existingLines
        .asSequence()
        .map { it.trim() }
        .filter { StringUtils.isNotEmpty(it) }
        .filter { it.contains('=') }//only process lines with '=' in them
        .filter { !it.startsWith('#') }//only process lines with '=' in them
        .filter { !it.startsWith("//") }//only process lines with '=' in them
        .map { StringUtils.substringBefore(it, "=").trim() to StringUtils.substringAfter(it, "=").trim() }
        .toList()
        .forEach { ConfToPropertyParser.addToPropsMap(it, propsMap) }

      return propsMap
    }
  }
}