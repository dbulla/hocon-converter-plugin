package com.nurflugel.hocon.parsers

import com.nurflugel.hocon.parsers.domain.*
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
            val index = IndexIndent()
            while (index.index < existingLines.size) {
                processLine(existingLines[index.index].trim(), existingLines, propsMap, index)
            }

            return propsMap
        }

        private fun processLine(
            line: String,
            existingLines: List<String>,
            propsMap: PropertiesMap,
            index: IndexIndent
        ): HoconType {
            return when {
                // empty line, skip for now
                StringUtils.isEmpty(line) -> {
                    index.increment()
                    HoconBlankLine()
                }
                // comments
                isComment(line) -> processComment(existingLines, index, line, propsMap)
                // it's the beginning of a list
                isListStart(line) -> processList(existingLines, index, line, propsMap)
                // properties
                isProperty(line) -> processProperty(line, propsMap, index)
                // includes
                isInclude(line) -> processInclude(line, propsMap, index)
                // beginning of a map
                isMap(line) -> processMap(line, propsMap, index)
                // wtf?
                else -> processUnknown(index, line)
            }
        }


        private fun isInclude(line: String) = line.startsWith("include")

        private fun isProperty(line: String) = line.contains("=")

        private fun isListStart(line: String) = line.contains("[")

        private fun isComment(line: String) = line.startsWith("//") || line.startsWith("#")

        private fun isMap(line: String) = line.contains("{")

        private fun processInclude(line: String, propsMap: PropertiesMap, index: IndexIndent): HoconType {
            propsMap.addInclude(line)
            index.increment()
            return HoconInclude(line)
        }

        private fun processUnknown(index: IndexIndent, line: String): HoconType {
            println("Unknown case in parsing line $index: $line")
            index.increment()
            return HoconUnknown()
        }

        /** take the line and convert it into a key/value pair */
        private fun processProperty(line: String, propsMap: PropertiesMap, index: IndexIndent): HoconType {
            val pair = HoconPair(
                StringUtils.substringBefore(line, "=").trim(),
                HoconValue(
                    StringUtils.substringAfter(line, "=").trim(),
                    listOf()
                ), listOf()
            )
            addToPropsMap(pair, propsMap)
            index.increment()
            return pair
        }

        /** take the line and start processing the list */
        private fun processList(
            existingLines: List<String>,
            index: IndexIndent,
            line: String,
            propsMap: PropertiesMap
        ): HoconList {
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

        private fun processMultilineList(
            line: String,
            index: IndexIndent,
            existingLines: List<String>,
            propsMap: PropertiesMap
        ): HoconList {
            // now iterate until we find the closing bracket
            val listLines = mutableListOf<String>()
            val key = StringUtils.substringBefore(line, "[").trim()
            index.increment()
            // check for a value(s) on the same line as the opening bracket

            val possibleValue = StringUtils.substringAfter(line, "[").trim()
            addBracketValues(possibleValue, listLines)
            // process the rest
            while (index.index < existingLines.size) {
                val nextLine = existingLines[index.index].trim()
                if (nextLine.contains("]")) {
                    // check for any values on the same line as the ]
                    val possibleValue1 = StringUtils.substringBefore(nextLine, "]").trim()
                    addBracketValues(possibleValue1, listLines)
                    break
                }
                listLines.add(nextLine)
                index.increment()
            }
            val list = HoconList(key, listLines)
            propsMap.addList(key, list)
            return list
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

        private fun processSingleLineList(line: String, propsMap: PropertiesMap, index: IndexIndent): HoconList {
            val contents = StringUtils.substringBefore(StringUtils.substringAfter(line, "[").trim(), "]").trim()
            val key = StringUtils.substringBefore(line, "[").trim()
            val values = contents.split(",")
            val list = HoconList(key, values)// no comments for now
            propsMap.addList(key, list)
            index.increment()
            return list
        }

        /** read down the list of lines until we find a non-comment line.  Parse that, then, we bind the comment to that line's property key. */
        private fun processComment(
            existingLines: List<String>,
            index: IndexIndent,
            line: String,
            propsMap: PropertiesMap
        ): HoconType {
            val comments = mutableListOf<String>()
            comments.add(line)
            var nextLine: String
            // process the rest
            index.increment()// start at the next line
            while (index.index < existingLines.size) {
                nextLine = existingLines[index.index].trim()
                if (isComment(nextLine)) {
                    comments.add(nextLine)
                } else {
                    // we have a line that's not a comment - parse that and get the type
                    val type = processLine(nextLine, existingLines, propsMap, index)
                    type.comments = comments
                    return type
                }
                index.increment()
            }
            return HoconBlankLine(listOf())
        }

        private fun processMap(line: String, propsMap: PropertiesMap, index: IndexIndent): HoconType {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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