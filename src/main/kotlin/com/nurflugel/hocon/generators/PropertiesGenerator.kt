package com.nurflugel.hocon.generators

import com.nurflugel.hocon.generators.ConfGenerator.writeValueMaybeQuotes
import com.nurflugel.hocon.parsers.domain.HoconList
import com.nurflugel.hocon.parsers.domain.HoconMap
import com.nurflugel.hocon.parsers.domain.PropertiesMap
import org.apache.commons.lang3.StringUtils
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
        // add the includes first
        propertiesMap.includesList
            .forEach {
                outputLines.add(it)
            }
        // a blank line after the includes
        if (propertiesMap.includesList.isNotEmpty()) outputLines.add("")

        populateOutputLinesFromMap(propertiesMap.map, outputLines, keyStack)

        // now output the map into property format
        return outputLines
    }

    // recursive function - go through each fork of the map - when you hit a terminal value, add all the parent keys to the line and it's value
    private fun populateOutputLinesFromMap(map: HoconMap, outputLines: MutableList<String>, keyStack: Stack<String>) {

        for (key in map.getKeys().toSortedSet()) {
            // if this is a terminal property, get the list of all parent keys and add to the output lines
            val value = map.get(key)
            when (value) {
                is HoconMap -> {
                    // if it's not a terminal entry, push the key into the stack & recurse into the map
                    keyStack.push(key)
                    populateOutputLinesFromMap(value, outputLines, keyStack)
                }
                else -> {
                    val prefix = keyStack.joinToString(separator = ".")

                    // the full key includes everything in the stack as a prefix
                    val fullKey = when {
                        prefix.isNotBlank() -> "$prefix.$key"
                        else -> key
                    }


                    when (value) {
                        is HoconList -> {
                            val indentLevel = keyStack.size
                            populateOutputLinesFromList(value, outputLines, fullKey, indentLevel)
                        }
                        else -> {
                            val wrappedValue = writeValueMaybeQuotes(value.toString())
                            value.addCommentsLines(outputLines)
                            outputLines.add("$fullKey = $wrappedValue")
                        }
                    }
                }
            }
        }
        if (keyStack.size > 0)// this is a bug, I shouldn't need it
            keyStack.pop()
    }


    private fun populateOutputLinesFromList(list: HoconList, outputLines: MutableList<String>, fullKey: String, indentLevel: Int) {

        val values = list.values
        outputLines.add("$fullKey = [")
        val whitespace = StringUtils.repeat("  ", indentLevel + 1)

        val allButLast = values.size - 1
        (0 until allButLast).forEach {
            outputLines.add(whitespace + writeValueMaybeQuotes(values[it]) + ",")
        }
        outputLines.add(whitespace + writeValueMaybeQuotes(values[allButLast]))
        outputLines.add("]")
    }
}