package com.nurflugel.hocon


import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.nurflugel.hocon.FileUtil.Companion.processLines
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import java.util.*
import java.util.stream.Collectors

class HoconConvertToPropertiesAction : TextComponentEditorAction(HoconConvertToPropertiesHandler()) {

    private class HoconConvertToPropertiesHandler : EditorWriteActionHandler() {
        override fun executeWriteAction(editor: Editor, dataContext: DataContext) {
            processLines(editor, true)
        }
    }
}

class HoconConvertToConfAction : TextComponentEditorAction(HoconConvertToConfHandler()) {

    private class HoconConvertToConfHandler : EditorWriteActionHandler() {
        override fun executeWriteAction(editor: Editor, dataContext: DataContext) {
            processLines(editor, false)
        }
    }
}

class FileUtil {
    companion object {
        fun processLines(editor: Editor, isToProperties: Boolean) {
            val doc = editor.document

            val startLine: Int
            var endLine: Int

            val selectionModel = editor.selectionModel
            val hasSelection = selectionModel.hasSelection()

            if (hasSelection) {
                startLine = doc.getLineNumber(selectionModel.selectionStart)
                endLine = doc.getLineNumber(selectionModel.selectionEnd)
                if (doc.getLineStartOffset(endLine) == selectionModel.selectionEnd) {
                    endLine--
                }
            } else {
                startLine = 0
                endLine = doc.lineCount - 1
            }

            // Ignore last lines (usually one) which are only '\n'
            endLine = ignoreLastEmptyLines(doc, endLine)

            if (startLine >= endLine) {
                return
            }

            // Extract text as a list of lines
            val lines = extractLines(doc, startLine, endLine)

            val newLines: List<String> = doConversion(lines, isToProperties)

            val convertedText = joinLines(newLines)

            // Replace text
            val startOffset = doc.getLineStartOffset(startLine)
            val endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine)

            editor.document.replaceString(startOffset, endOffset, convertedText)
        }

        private fun doConversion(lines: List<String>, toProperties: Boolean): List<String> {
            return when {// todo replace with method references
                toProperties -> convertPropertiesToConf(lines)
                else -> convertConfToProperties(lines)
            }
        }

        private fun ignoreLastEmptyLines(doc: Document, endLine: Int): Int {
            var mutableEndLine = endLine
            while (mutableEndLine >= 0) {
                if (doc.getLineEndOffset(mutableEndLine) > doc.getLineStartOffset(mutableEndLine)) {
                    return mutableEndLine
                }

                mutableEndLine--
            }

            return -1
        }

        private fun extractLines(doc: Document, startLine: Int, endLine: Int): List<String> {
            return (startLine..endLine)
                .map { extractLine(doc, it) }
        }

        private fun extractLine(doc: Document, lineNumber: Int): String {
            val lineSeparatorLength = doc.getLineSeparatorLength(lineNumber)
            val startOffset = doc.getLineStartOffset(lineNumber)
            val endOffset = doc.getLineEndOffset(lineNumber) + lineSeparatorLength

            var line = doc.charsSequence.subSequence(startOffset, endOffset).toString()

            // If last line has no \n, add it one
            // This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
            // with \n... This is fixed after.
            if (lineSeparatorLength == 0) {
                line += "\n"
            }

            return line
        }

        private fun joinLines(lines: List<String>): StringBuilder {
            val builder = StringBuilder()
            for (line in lines) {
                builder.append(line)
                builder.append("\n")
            }

            return builder
        }


        fun convertPropertiesToConf(existingLines: List<String>): MutableList<String> {
            val propsMap: MutableMap<String, Any> = populatePropsMap(existingLines)
            return generateConfOutput(propsMap)
        }

        /** take a map of property folders and values, output to conf format
         * Basically, if a key has a map under it, print out the key (add 1 to the indent level) and a {
         *
         * When a key has a String/numeric/Boolean value, then print out the key = value
         *
         * Done with the contents of a map?  Then print  a } and decrease the indent
         * */
        private fun generateConfOutput(propsMap: MutableMap<String, Any>): MutableList<String> {
            val lines = mutableListOf<String>()
            outputMap(propsMap, 0, lines)
            // trim the last extra }
            return lines.subList(0, lines.size - 1)
        }

        private fun outputMap(propsMap: Map<String, Any>, initialIndentLevel: Int, lines: MutableList<String>): Int {
            var indentLevel = initialIndentLevel;
            val sortedKeys = propsMap.keys
                .toSortedSet()
            for (key in sortedKeys) {
                val value = propsMap[key]
                // is the value a map, or a key
                val whiteSpace = StringUtils.repeat("  ", indentLevel)
                if (value is Map<*, *>) {// increase the indent level
                    indentLevel++
                    lines.add("$whiteSpace$key {")
                    indentLevel = outputMap(value as Map<String, Any>, indentLevel, lines)
                } else {
                    var textValue = value as String
                    textValue = writeText(textValue)// add quotes if none exist (check for number/booleans first)
                    lines.add("$whiteSpace$key = $textValue")
                }
            }
            indentLevel--
            val whiteSpace = StringUtils.repeat("  ", indentLevel)
            lines.add("$whiteSpace}")
            return indentLevel
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

        private fun populatePropsMap(existingLines: List<String>): MutableMap<String, Any> {
            val propsMap: MutableMap<String, Any> = mutableMapOf()
            existingLines
                .asSequence()
                .map { it.trim() }
                .filter { StringUtils.isNotEmpty(it) }
                .filter { it.contains('=') }//only process lines with '=' in them
                .filter { !it.startsWith('#') }//only process lines with '=' in them
                .filter { !it.startsWith("//") }//only process lines with '=' in them
                .map { Pair(StringUtils.substringBefore(it, "=").trim(), StringUtils.substringAfter(it, "=").trim()) }
                .toList()
                .forEach { addToPropsMap(it, propsMap) }
            return propsMap
        }

        // take the pair and add it to the properties map
        private fun addToPropsMap(keyValue: Pair<String, String>, propsMap: MutableMap<String, Any>) {
            // the key path may be services.cpd.connection.retry, and the value might be 'true'
            val keyPath: List<String> = keyValue.first.split(".")

            var subMap: MutableMap<String, Any> = propsMap
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
            val map = mutableMapOf<String, Any>()
            val keyStack = Stack<String>()
            val outputLines = mutableListOf<String>()

            lines
                .asSequence()
                .map { it.trim() }
                .filter { StringUtils.isNotEmpty(it) }
                .filter { !it.startsWith("//") }
                .filter { !it.startsWith("#") }
                .filter { it.contains("{") || it.contains("]") || it.contains("=") || it.contains("}") }// todo regex
                .toList()// use a regex
                .forEach { processLine(it, map, keyStack, outputLines) }

            outputLines.sort()
            // now output the map into property format
            return outputLines
        }

        private fun processLine(
            line: String,
            map: MutableMap<String, Any>,
            keyStack: Stack<String>,
            outputLines: MutableList<String>
        ) {
            println("line = ${line}")
            when {
                !line.contains("=") && line.contains("{") -> { // we're adding another level to the stack, but not if the { is inside quotes
                    val keyName = StringUtils.substringBefore(line, "{").trim()
                    keyStack.push(keyName)
                }
                !line.contains("=") && line.contains("}") -> {// clear last item on stack
                    keyStack.pop()
                }
                else -> {// it's a property
                    val pair: List<String> = line.split("=")
                    if (pair.size == 2) {
                        val key = pair[0].trim()
                        val value = pair[1].trim()
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


