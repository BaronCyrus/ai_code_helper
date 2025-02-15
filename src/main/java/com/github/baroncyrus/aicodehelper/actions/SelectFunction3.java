package com.github.baroncyrus.aicodehelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class SelectFunction3 extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showInfoMessage("SelectFunction3" + e.getDataContext(), "Tab Clock SelectFunction3");
    }
}