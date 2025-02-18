package com.github.baroncyrus.aicodehelper.actions;

import com.github.baroncyrus.aicodehelper.toolWindow.MyToolWindowFactory;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectFunction1 extends AnAction {
    // 必须覆盖update方法（知识库「Principal Implementation Overrides」要求）
    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        // 根据知识库「Enabling and Setting Visibility for an Action」设置可见性
        e.getPresentation().setEnabledAndVisible(project != null && hasSelection);
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        @Nullable Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (StringUtil.isEmpty(selectedText)) {
            Messages.showInfoMessage("Content is empty", "SelectFunction1 ErrorTip");
            return;
        }

        // 正确获取Project的方式（根据知识库中CommonDataKeys的使用规范）
        Project project = e.getData(CommonDataKeys.PROJECT);

        if (project == null) {
            Messages.showErrorDialog("Project context not available", "Operation Failed");
            return;
        }

        // 显示工具窗口
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AICodeAssist");


        //todo 根据选中内容文件名后缀 拿到当前代码块的语言 比如C# java
        if (toolWindow != null) {
            toolWindow.show(() -> {
                MyToolWindowFactory.ChatWindow window = MyToolWindowFactory.ChatWindow.getInstance(project);
                if (window != null) {
                    window.addMessage("Me", "Explain Code: ```csharp\n" + selectedText + "```", true);
                }
            });
        }
    }

    // 根据知识库「Action IDs」要求定义唯一ID
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}