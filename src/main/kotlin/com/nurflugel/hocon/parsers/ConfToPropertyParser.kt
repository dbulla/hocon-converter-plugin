package com.nurflugel.hocon.parsers

import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.stream.Collectors

class ConfToPropertyParser {
  companion object {


//    /** We're taking in a list of key-value pairs (no {} mapping and creating the map */
//    private fun populatePropsMap(existingLines: List<String>): PropertiesMap {
//      val propsMap = PropertiesMap()
//
//      // add any includes
//      existingLines
//        .map { it.trim() }
//        .filter { it.startsWith("include") }
//        .forEach { propsMap.addInclude(it) }
//
//      existingLines
//        .asSequence()
//        .map { it.trim() }
//        .filter { StringUtils.isNotEmpty(it) }
//        .filter { it.contains('=') }//only process lines with '=' in them
//        .filter { !it.startsWith('#') }//only process lines with '=' in them
//        .filter { !it.startsWith("//") }//only process lines with '=' in them
//        .map { StringUtils.substringBefore(it, "=").trim() to StringUtils.substringAfter(it, "=").trim() }
//        .toList()
//        .forEach { addToPropsMap(it, propsMap) }
//
//      return propsMap
//    }

    // take the pair and add it to the properties map
    fun addToPropsMap(keyValue: Pair<String, String>, propsMap: PropertiesMap) {
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

    /** any line with a { is a map, anything with a = in it is a key/value pair

    AGENT_CONFIG_PATH = "agent-config.txt"

    auth {
    okta {
    enabled = true
    url = "https://auth-qa.nike.net/auth"
    }
    }

    should come out like

    AGENT_CONFIG_PATH = "agent-config.txt"
    auth.okta.enabled = true
    auth.okta.url = "https://auth-qa.nike.net/auth"

     */
    fun convertConfToProperties(lines: List<String>): MutableList<String> {
      val keyStack = Stack<String>()
      val outputLines = mutableListOf<String>()

      val propertiesMap = createParsingMap(lines, keyStack, outputLines)

      outputLines.sort()
      val finalLines = mutableListOf<String>()

      // add the includes first
      propertiesMap.includesList
        .forEach {
          finalLines.add(it)
        }

      // a blank line after the includes
      if (propertiesMap.includesList.isNotEmpty()) finalLines.add("")
      finalLines.addAll(outputLines)
      // now output the map into property format
      return finalLines
    }

    fun createParsingMap(existingLines: List<String>, keyStack: Stack<String>, outputLines: MutableList<String>): PropertiesMap {
      val map = PropertiesMap()

      existingLines
        .asSequence()
        .map { it.trim() }
        .filter { StringUtils.isNotEmpty(it) }
        .filter { !it.startsWith("//") }
        .filter { !it.startsWith("#") }
        .filter {
          it.contains("{")
            || it.contains("]")
            || it.contains("=")
            || it.contains("}")
            || it.startsWith("include")
        }// todo regex?
        .toList()// use a regex
        .forEach { processLine(it, map, keyStack, outputLines) }
      return map
    }

    private fun processLine(
      line: String,
      map: PropertiesMap,
      keyStack: Stack<String>,
      outputLines: MutableList<String>
    ) {
      println("line = $line")
      when {
        line.startsWith("include") -> map.addInclude(line)
        !line.contains("=") && line.contains("{") -> { // we're adding another level to the stack, but not if the { is inside quotes
          val keyName = StringUtils.substringBefore(line, "{").trim()
          keyStack.push(keyName)
        }
        // clear last item on stack
        !line.contains("=") && line.contains("}") -> keyStack.pop()
        else -> {// it's a property
          val pair: List<String> = line.split("=")
          if (pair.size == 2) {
            val key = pair[0].trim()
            val value = pair[1].trim()
//            map[key] = value
//            val prefix = keyStack
//              .joinToString { "." }
            val prefix = keyStack.stream()
              .collect(Collectors.joining("."))
            val fullKey = when {
              StringUtils.isEmpty(prefix) -> key
              else -> "$prefix.$key"
            }
            val fullLine = "$fullKey = $value"
            outputLines.add(fullLine)
          }
        }
      }
    }
  }
}