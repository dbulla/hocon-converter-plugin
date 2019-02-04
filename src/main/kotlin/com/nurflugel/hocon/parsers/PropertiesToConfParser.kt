package com.nurflugel.hocon.parsers

import com.nurflugel.hocon.parsers.domain.HoconList
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils


class PropertiesToConfParser {
  companion object {

    /** this assumes the lines being parsed are pure property lines - just
     * stuff like aaa.bbb.ccc.dd=true, no maps
     */
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

      // add any includes first
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
          var result = textValue

          // wrap in quotes unless it's a list
          if (!result.contains('[') && !result.startsWith('"')) result = """"$result""""
          result
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
        .filter { StringUtils.isNotEmpty(it) } // remove whitespace
        .filter { !it.startsWith('#') } // skip comments
        .filter { !it.startsWith("//") }// skip comments
        .filter { it.contains('=') }//only process lines with '=' in them
        .map { StringUtils.substringBefore(it, "=").trim() to StringUtils.substringAfter(it, "=").trim() }
        .toList()
        .forEach { addToPropsMap(it, propsMap) }

      // have to iterate the old-fashioned way, as we may have to go forwards to grab lists, comments, etc.
      var index = 0
      if (false)
        while (index < existingLines.size) {
          val line = existingLines[index].trim()
          if (StringUtils.isEmpty(line)) continue
          index = when {
            // comments
            line.startsWith("//") || line.startsWith("#") -> processComment(existingLines, index, line)
            // it's the beginning of a list
            line.contains("[") -> processList(existingLines, index, line, propsMap)
            // properties
            line.contains("=") -> processProperty(line, propsMap, index)
            // wtf?
            else -> processUnknown(index, line)
          }
        }

      return propsMap
    }

    private fun processUnknown(index: Int, line: String): Int {
      println("Unknown case in parsing line $index: $line")
      return index + 1
    }

    /** take the line and convert it into a key/value pair */
    private fun processProperty(line: String, propsMap: PropertiesMap, index: Int): Int {
      val pair = StringUtils.substringBefore(line, "=").trim() to StringUtils.substringAfter(line, "=").trim()
      addToPropsMap(pair, propsMap);
      return index + 1
    }

    /** take the line and start processing the list */
    private fun processList(existingLines: List<String>, index: Int, line: String, propsMap: PropertiesMap): Int {
      // two cases - it's a list all in one line, or it's "vertical"
      if (line.contains("]")) {
        val contents = StringUtils.substringBefore(StringUtils.substringAfter(line, "[").trim(), "]").trim()
        val key = StringUtils.substringBefore(line, "[").trim()
        val values = contents.split(",")
        val list = HoconList(key, values, listOf())
        //todo add to map
        return index + 1
      } else {// now iterate until we find the closing bracket
        // todo implement
        return index + 1
      }
    }

    /** read down the list of lines until we find a non-comment line.  Then, we bind the comment to that line's property key */
    private fun processComment(existingLines: List<String>, index: Int, line: String): Int {
      println("comment found, ignoring: $line")
      //todo complete thhis
      return index + 1
    }

    // take the pair and add it to the properties map
    private fun addToPropsMap(keyValue: Pair<String, String>, propsMap: PropertiesMap) {
      // the key path may be services.cpd.connection.retry, and the value might be 'true'
      val keyPath: List<String> = keyValue.first.split(".")

      var subMap: MutableMap<String, Any> = propsMap.map
      // if the map doesn't contain all the folders, create them
      (0 until keyPath.size - 1)
        .asSequence()
        .map { keyPath[it] }
        .forEach { subMap = getSubMap(subMap, it) }
      // now subMap is the lowest folder, just need to add the key
      subMap[keyPath.last()] = keyValue.second
    }

    private fun getSubMap(propsMap: MutableMap<String, Any>, folderName: String): MutableMap<String, Any> {
      if (!propsMap.containsKey(folderName)) {
        val map = mutableMapOf<String, Any>()
        propsMap[folderName] = map
        return map
      }
      if (propsMap[folderName] is String) {
        println("""Expected a folder, but found a key for "$folderName"""")
      }
      return propsMap[folderName] as MutableMap<String, Any>
    }

  }

}