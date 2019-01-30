package com.nuflugel.hocon.plugin;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;

public class ConvertHoconToPropertiesAction extends TextComponentEditorAction {
    public ConvertHoconToPropertiesAction() {
        super(new HoconConfToPropertiesHandler());
    }

    private static class HoconConfToPropertiesHandler extends EditorWriteActionHandler {
        public void executeWriteAction(Editor editor, DataContext dataContext) {
            Utils.processLines(editor,false);
        }
    }
}