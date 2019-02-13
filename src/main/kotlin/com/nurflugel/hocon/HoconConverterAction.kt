package com.nurflugel.hocon


import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.nurflugel.hocon.FileUtil.Companion.processLines
import com.nurflugel.hocon.generators.ConfGenerator.generateConfOutput
import com.nurflugel.hocon.generators.PropertiesGenerator.generatePropertiesOutput
import com.nurflugel.hocon.parsers.HoconParser.Companion.populatePropsMap

class HoconConvertToPropertiesAction : TextComponentEditorAction(HoconConvertToPropertiesHandler()) {

  private class HoconConvertToPropertiesHandler : EditorWriteActionHandler() {
    override fun executeWriteAction(editor: Editor, dataContext: DataContext) {
      processLines(editor, true, false)// todo - read this in from a GUI setting
    }
  }
}

class HoconConvertToConfAction : TextComponentEditorAction(HoconConvertToConfHandler()) {

  private class HoconConvertToConfHandler : EditorWriteActionHandler() {
    override fun executeWriteAction(editor: Editor, dataContext: DataContext) {
      processLines(editor, false, false)// todo - read this in from a GUI setting
    }
  }
}

class FileUtil {
  companion object {
    /**
     * dgbtodo ugly, think of a better way to pass the logic
     * @param isToProperties if true, we're going from conf to properties.  If false, from properties to conf
     *
     */
    fun processLines(
      editor: Editor,
      isToProperties: Boolean,
      flattenKeys: Boolean
    ) {
      val triple = extractText(editor)
      val doc = triple.first
      val startLine: Int = triple.second
      val endLine: Int = triple.third

      if (startLine >= endLine) {
        return
      }


      // todo if this is a YAML file, read in only the stuff after app |- and ensure it's indented
      // Extract text as a list of lines
      val lines = extractLines(doc, startLine, endLine)

      val propertiesMap = populatePropsMap(lines)

      // Convert to the new format
      val newLines: List<String> = when {
        isToProperties -> generatePropertiesOutput(propertiesMap)
        else -> generateConfOutput(propertiesMap, flattenKeys)
      }

      // Stick it back into the document
      replaceTextInDocument(newLines, doc, startLine, endLine, editor)
    }

    private fun extractText(editor: Editor): Triple<Document, Int, Int> {
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
      return Triple(doc, startLine, endLine)
    }

    private fun replaceTextInDocument(newLines: List<String>, doc: Document, startLine: Int, endLine: Int, editor: Editor) {
      val convertedText = joinLines(newLines)

      // Replace text
      val startOffset = doc.getLineStartOffset(startLine)
      val endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine)

      editor.document.replaceString(startOffset, endOffset, convertedText)
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

  }
}



