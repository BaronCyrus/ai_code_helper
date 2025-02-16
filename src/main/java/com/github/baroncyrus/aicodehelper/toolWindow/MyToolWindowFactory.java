package com.github.baroncyrus.aicodehelper.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;

public class MyToolWindowFactory implements ToolWindowFactory {

    private static JEditorPane outputPane;

    private JPanel createToolWindowPanel(){
        JPanel mainPanel = new JPanel(new BorderLayout());

        //init html area
        outputPane = new JEditorPane();
        outputPane.setContentType("text/html");
        outputPane.setEditable(false);
        initHtmlTemplate();

        JBScrollPane scrollPane = new JBScrollPane(outputPane);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 输入区域
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void initHtmlTemplate() {
        String css = """
            <style>
                .user-msg {
                    background: #e3f2fd;
                    border-left: 4px solid #2196F3;
                    margin: 8px 0;
                    padding: 8px;
                }
                .ai-msg {
                    background: #f8f9fa;
                    border-left: 4px solid #4CAF50;
                    margin: 8px 0;
                    padding: 8px;
                }
                .label {
                    font-weight: bold;
                    margin-bottom: 4px;
                }
                pre {
                    margin: 0;
                    white-space: pre-wrap;
                }
            </style>
        """;
        outputPane.setText("<html><head>" + css + "</head><body></body></html>");
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton submitButton = new JButton("提交");

        submitButton.addActionListener(e -> {
            String input = inputField.getText();
            processInput(input);
            inputField.setText("");
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        inputPanel.setBorder(JBUI.Borders.empty(5));
        return inputPanel;
    }

    private void processInput(String input) {
        appendMessage(input, true);
        // 模拟AI处理（实际应调用服务）
        String response = "处理结果: " + input.toUpperCase();
        appendMessage(response, false);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        JPanel panel = createToolWindowPanel();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static void appendMessage(String text, boolean isUser) {
        String escapedText = StringUtil.escapeXmlEntities(text);
        String template = buildMessageTemplate(isUser, escapedText);
        System.out.println("test point 2" + template);
        SwingUtilities.invokeLater(() -> {
            try {
                HTMLDocument doc = (HTMLDocument) outputPane.getDocument();
                Element body = doc.getElement("body");
                if (body != null) {
                    doc.insertAfterEnd(body, template);
                    outputPane.setCaretPosition(doc.getLength());
                }
            } catch (Exception e) {
                Logger.getInstance(MyToolWindowFactory.class).error("消息插入失败", e);
            }
        });
    }

    private static String buildMessageTemplate(boolean isUser, String text) {
        return """
            <div class="%s">
                <div class="label">%s</div>
                <pre>%s</pre>
            </div>
        """.formatted(
                isUser ? "user-msg" : "ai-msg",
                isUser ? "User" : "AiCodeAssist",
                text
        );
    }

}