package com.github.baroncyrus.aicodehelper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;


public class SelectFunction2 extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showInfoMessage("SelectFunction2" + e.getDataContext(), "Tab Clock SelectFunction2");
    }
}