package com.github.baroncyrus.aicodehelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

public class SelectFunction1 extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        @Nullable Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        if (StringUtil.isEmpty(selectedText)){
            Messages.showInfoMessage("SelectFunction1 选中的内容为空", "Tab Clock SelectFunction1");
            return;
        }
        Messages.showInfoMessage("SelectFunction1 选中的内容" + selectedText, "Tab Clock SelectFunction1");
    }
}