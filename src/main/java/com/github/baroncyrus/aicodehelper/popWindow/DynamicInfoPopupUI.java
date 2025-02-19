package com.github.baroncyrus.aicodehelper.popWindow;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DynamicInfoPopupUI {
    private JBPopup popup;
    private JTextArea contentArea;
    private boolean isShowing = false;

    // 初始化方法改为显式初始化
    public void init(@NotNull Project project) {
        contentArea = new JTextArea(10, 50);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        // 适配深色主题
        contentArea.setBackground(UIUtil.getPanelBackground());
        contentArea.setForeground(UIUtil.getLabelForeground());
        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UISettings.getInstance().getFontSize()));

        JBScrollPane scrollPane = new JBScrollPane(contentArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, null)
                .setTitle("AI Thinking")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setCancelOnOtherWindowOpen(false)  // 禁用其他窗口打开时关闭
                .setCancelOnWindowDeactivation(false)  // 禁用窗口失焦关闭
                .setCancelCallback(() -> {
                    isShowing = false;
                    return true;
                })
                .createPopup();
    }

    // 显示弹窗（项目上下文相关）
    public void show(@NotNull Project project) {
        if (!isShowing) {
            Window window = WindowManager.getInstance().getFrame(project);
            if (window != null) {
                popup.showInCenterOf(window);
                isShowing = true;
            }
        }
    }

    // 暴露文本区域用于精细控制
    public JTextArea getContentArea() {
        return contentArea;
    }

    // 追加文本（带自动滚动）
    public void appendText(String text) {
        if (contentArea != null) {
            contentArea.append(text);
            contentArea.setCaretPosition(contentArea.getDocument().getLength());
        }
    }

    // 安全关闭方法
    public void close() {
        if (isShowing && popup != null) {
            popup.cancel();
            isShowing = false;
        }
    }

    // 获取当前显示状态
    public boolean isShowing() {
        return isShowing;
    }
}