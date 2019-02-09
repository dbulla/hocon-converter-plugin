package com.nurflugel.hocon.parsers

import com.nurflugel.hocon.parsers.domain.PropertiesMap
import org.apache.commons.lang3.StringUtils
import java.util.*

@Deprecated("Use PropertiesToConfParser instead")
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
    @Deprecated("go away")
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

    @Deprecated("go away")

    fun createParsingMap(
      existingLines: List<String>,
      keyStack: Stack<String>,
      outputLines: MutableList<String>
    ): PropertiesMap {
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

    @Deprecated("go away")

    private fun processLine(
      line: String,
      map: PropertiesMap,
      keyStack: Stack<String>,
      outputLines: MutableList<String>
    ) {
      println("line = $line")
      when {
        line.startsWith("include") -> map.addInclude(line)
        // we're adding another level to the stack, but not if the { is inside quotes
        !line.contains("=") && line.contains("{") -> addLevelToKeyStack(line, keyStack)
        // clear last item on stack
        !line.contains("=") && line.contains("}") -> keyStack.pop()
        else -> processProperty(line, keyStack, outputLines, map)
      }
    }

    @Deprecated("go away")

    private fun processProperty(
      line: String,
      keyStack: Stack<String>,
      outputLines: MutableList<String>,
      map: PropertiesMap
    ) {
      // it's a property
      val pair: List<String> = line.split("=")
      if (pair.size == 2) {
        val key = pair[0].trim()
        val value = pair[1].trim()
        //            map[key] = value

        val prefix = keyStack
          .joinToString(separator = ".")

        val fullKey = when {
          StringUtils.isEmpty(prefix) -> key
          else -> "$prefix.$key"
        }
        val fullLine = "$fullKey = $value"

        outputLines.add(fullLine)
//dgbtodo        map.map[fullKey]=value
      }
    }

    @Deprecated("go away")

    private fun addLevelToKeyStack(line: String, keyStack: Stack<String>) {
      val keyName = StringUtils.substringBefore(line, "{").trim()
      keyStack.push(keyName)
    }
  }
}