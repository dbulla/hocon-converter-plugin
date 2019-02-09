package com.nurflugel.hocon.generators

import com.nurflugel.hocon.parsers.domain.HoconMap
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import java.util.*

object PropertiesGenerator {
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
  fun generatePropertiesOutput(propertiesMap: PropertiesMap): MutableList<String> {
    val outputLines = mutableListOf<String>()
    val keyStack = Stack<String>()
    populateOutputLinesFromMap(propertiesMap.map, outputLines, keyStack)
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

  // recursive function - go through each fork of the map - when you hit a terminal value, add all the parent keys to the line and it's value
  private fun populateOutputLinesFromMap(map: HoconMap, outputLines: MutableList<String>, keyStack: Stack<String>) {
    val entries = map.entries()
    for (entry in entries) {
      // if this is a terminal property, get the list of all parent keys and add to the output lines
      val value = entry.value
      when (value) {
        is HoconMap -> {
          // if it's not a terminal entry, push the key into the stack & recurse into the map
          keyStack.push(entry.key)
          populateOutputLinesFromMap(value, outputLines, keyStack)
        }
        else -> {
          val prefix = keyStack.joinToString(separator = ".")
          val key = entry.key

          // the full key includes everything in the stack as a prefix
          val fullKey = when {
            prefix.isNotBlank() -> "$prefix.$key"
            else -> key
          }

          val wrappedValue = ConfGenerator.writeText(value.toString())
          outputLines.add("$fullKey = $wrappedValue")
        }
      }
    }
  }
}