package com.nurflugel.hocon


import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.nurflugel.hocon.FileUtil.Companion.processLines
import com.nurflugel.hocon.parsers.ConfToPropertyParser.Companion.convertConfToProperties
import com.nurflugel.hocon.parsers.PropertiesToConfParser.Companion.convertPropertiesToConf

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

  }
}



