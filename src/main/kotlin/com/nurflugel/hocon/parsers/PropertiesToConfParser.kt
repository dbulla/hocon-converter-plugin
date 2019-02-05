package com.nurflugel.hocon.parsers

import com.nurflugel.hocon.parsers.domain.HoconList
import com.nurflugel.hocon.parsers.domain.HoconPair
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import org.apache.commons.lang3.StringUtils


class PropertiesToConfParser {
  companion object {

    /** this assumes the lines being parsed are pure property lines - just
     * stuff like aaa.bbb.ccc.dd=true, no maps
     */
    fun convertPropertiesToConf(existingLines: List<String>): MutableList<String> {
      val propsMap: PropertiesMap = populatePropsMap(existingLines)
      return ConfGenerator.generateConfOutput(propsMap)
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


    /** We're taking in a list of key-value pairs (no {} mapping and creating the map */
    private fun populatePropsMap(existingLines: List<String>): PropertiesMap {
      val propsMap = PropertiesMap()

      // have to iterate the old-fashioned way, as we may have to go forwards to grab lists, comments, etc.
      var index = 0
      while (index < existingLines.size) {
        index = processLine(index, existingLines[index].trim(), existingLines, propsMap)
      }

      return propsMap
    }

    private fun processLine(index: Int, line: String, existingLines: List<String>, propsMap: PropertiesMap): Int {
      var index1 = index
      index1 = when {
        // empty line, skip for now
        StringUtils.isEmpty(line) -> index1 + 1
        // comments
        isComment(line) -> processComment(existingLines, index1, line)
        // it's the beginning of a list
        isListStart(line) -> processList(existingLines, index1, line, propsMap)
        // properties
        isProperty(line) -> processProperty(line, propsMap, index1)
        // includes
        isInclude(line) -> processInclude(line, propsMap, index1)
        // wtf?
        else -> processUnknown(index1, line)
      }
      return index1
    }

    private fun isInclude(line: String) = line.startsWith("include")

    private fun isProperty(line: String) = line.contains("=")

    private fun isListStart(line: String) = line.contains("[")

    private fun isComment(line: String) = line.startsWith("//") || line.startsWith("#")

    private fun processInclude(line: String, propsMap: PropertiesMap, index: Int): Int {
      propsMap.addInclude(line)
      return index + 1
    }

    private fun processUnknown(index: Int, line: String): Int {
      println("Unknown case in parsing line $index: $line")
      return index + 1
    }

    /** take the line and convert it into a key/value pair */
    private fun processProperty(line: String, propsMap: PropertiesMap, index: Int): Int {
      val pair = HoconPair(StringUtils.substringBefore(line, "=").trim(), StringUtils.substringAfter(line, "=").trim())
      addToPropsMap(pair, propsMap);
      return index + 1
    }

    /** take the line and start processing the list */
    private fun processList(existingLines: List<String>, index: Int, line: String, propsMap: PropertiesMap): Int {
      // two cases - it's a list all in one line, or it's "vertical"
      return when {
        line.contains("]") -> {
          processSingleLineList(line, propsMap, index)
        }
        else -> {
          processMultilineList(line, index, existingLines, propsMap)
        }
      }
    }

    private fun processMultilineList(line: String, index: Int, existingLines: List<String>, propsMap: PropertiesMap): Int {
      // now iterate until we find the closing bracket
      val listLines = mutableListOf<String>()
      val key = StringUtils.substringBefore(line, "[").trim()
      var offset = 1 // start on the next line

      // check for a value(s) on the same line as the opening bracket

      val possibleValue = StringUtils.substringAfter(line, "[").trim()
      addBracketValues(possibleValue, listLines)
      // process the rest
      while (index + offset < existingLines.size) {
        val nextLine = existingLines[index + offset].trim()
        if (nextLine.contains("]")) {
          // check for any values on the same line as the ]
          val possibleValue1 = StringUtils.substringBefore(nextLine, "]").trim()
          addBracketValues(possibleValue1, listLines)
          break
        }
        listLines.add(nextLine)
        offset++
      }
      propsMap.addList(key, HoconList(key, listLines, listOf()))
      return index + offset + 1
    }

    /** take some text we found before or after brackets, parse any values for it */
    private fun addBracketValues(possibleValue: String, listLines: MutableList<String>) {
      if (possibleValue.isNotBlank()) {
        val split = possibleValue.split(",")
        split.forEach { s ->
          if (s.isNotBlank()) {
            listLines.add(s.trim())
          }
        }
      }
    }

    private fun processSingleLineList(line: String, propsMap: PropertiesMap, index: Int): Int {
      val contents = StringUtils.substringBefore(StringUtils.substringAfter(line, "[").trim(), "]").trim()
      val key = StringUtils.substringBefore(line, "[").trim()
      val values = contents.split(",")
      val list = HoconList(key, values, listOf())// no comments for now
      propsMap.addList(key, list)
      return index + 1
    }

    /** read down the list of lines until we find a non-comment line.  Parse that, then, we bind the comment to that line's property key. */
    private fun processComment(existingLines: List<String>, index: Int, line: String): Int {

      if (false) {
        var offset = 1 // start on the next line
        val comments = mutableListOf<String>()
        comments.add(line)
        var nextLine = line
        // process the rest
        while (index + offset < existingLines.size) {
          nextLine = existingLines[index + offset].trim()
          if (isComment(nextLine)) {
            comments.add(nextLine)
            offset++
          } else {
            // we have a line that's not a comment - parse that and get the type
            //todo finish
//          when {
//            // it's the beginning of a list
//            isListStart(line) -> processList(existingLines, index1, line, propsMap)
//            // properties
//            isProperty(line) -> processProperty(line, propsMap, index1)
//            else -> processUnknown(0, line)
//          }

          }
        }
      }
      println("comment found, ignoring: $line")
      //todo complete this

      return index + 1
    }

    // take the pair and add it to the properties map
    private fun addToPropsMap(keyValue: HoconPair, propsMap: PropertiesMap) {
      // the key path may be services.cpd.connection.retry, and the value might be 'true'
      val keyPath: List<String> = keyValue.key.split(".")

      var subMap: MutableMap<String, Any> = propsMap.map
      // if the map doesn't contain all the folders, create them
      (0 until keyPath.size - 1)
        .asSequence()
        .map { keyPath[it] }
        .forEach { subMap = getSubMap(subMap, it) }
      // now subMap is the lowest folder, just need to add the key
      subMap[keyPath.last()] = keyValue.value
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