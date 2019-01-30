package com.nuflugel.hocon.plugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

public class Utils {
    
    static void processLines(Editor editor, boolean useReverseSort) {
        final Document doc = editor.getDocument();

        int startLine;
        int endLine;

        SelectionModel selectionModel = editor.getSelectionModel();
        boolean hasSelection = selectionModel.hasSelection();

        if (hasSelection) {
            startLine = doc.getLineNumber(selectionModel.getSelectionStart());
            endLine = doc.getLineNumber(selectionModel.getSelectionEnd());
            if (doc.getLineStartOffset(endLine) == selectionModel.getSelectionEnd()) {
                endLine--;
            }
        } else {
            startLine = 0;
            endLine = doc.getLineCount() - 1;
        }

        // Ignore last lines (usually one) which are only '\n'
        endLine = ignoreLastEmptyLines(doc, endLine);

        if (startLine >= endLine) {
            return;
        }

        // Extract text as a list of lines
        List<String> lines = extractLines(doc, startLine, endLine);

        // dumb sort
        if (useReverseSort)
            lines.sort(Comparator.reverseOrder());
        else
            lines.sort(Comparator.naturalOrder());

        StringBuilder convertedText = joinLines(lines);
        
        // Replace text
        int startOffset = doc.getLineStartOffset(startLine);
        int endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine);

        editor.getDocument().replaceString(startOffset, endOffset, convertedText);
    }

    private static int ignoreLastEmptyLines(Document doc, int endLine) {
        while (endLine >= 0) {
            if (doc.getLineEndOffset(endLine) > doc.getLineStartOffset(endLine)) {
                return endLine;
            }

            endLine--;
        }

        return -1;
    }

    private static List<String> extractLines(Document doc, int startLine, int endLine) {
        List<String> lines = IntStream.rangeClosed(startLine, endLine)
                .mapToObj(i -> extractLine(doc, i))
                .collect(toCollection(() -> new ArrayList<>(endLine - startLine)));

        return lines;
    }

    private static String extractLine(Document doc, int lineNumber) {
        int lineSeparatorLength = doc.getLineSeparatorLength(lineNumber);
        int startOffset = doc.getLineStartOffset(lineNumber);
        int endOffset = doc.getLineEndOffset(lineNumber) + lineSeparatorLength;

        String line = doc.getCharsSequence().subSequence(startOffset, endOffset).toString();

        // If last line has no \n, add it one
        // This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
        // with \n... This is fixed after.
        if (lineSeparatorLength == 0) {
            line += "\n";
        }

        return line;
    }

    private static StringBuilder joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
        }

        return builder;
    }
    
}
