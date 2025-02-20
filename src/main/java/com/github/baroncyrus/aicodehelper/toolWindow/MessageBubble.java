package com.github.baroncyrus.aicodehelper.toolWindow;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {
    private final JTextPane contentPane;
    private final JPanel parentContainer; // 用于获取父容器宽度

    public MessageBubble(String sender, String content, boolean isUser, JPanel parentContainer) {
        this.parentContainer = parentContainer;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createMatteBorder(0, 3, 0, 0, isUser ? JBColor.YELLOW : JBColor.GRAY)
        ));
        setBackground(isUser ? new JBColor(new Color(0xDBE8FF), new Color(0x2B5278))
                : new JBColor(new Color(0xFFFFFF), new Color(0x3C3F41)));

        // 消息头
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setHorizontalAlignment(SwingConstants.LEFT);
        senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 14f));
        senderLabel.setForeground(isUser ? new JBColor(new Color(0x2B5278), new Color(0x8CACD6))
                : new JBColor(new Color(0x616161), new Color(0xBBBBBB)));
        senderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(senderLabel, BorderLayout.NORTH);

        // 内容区域
        contentPane = new JTextPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        contentPane.setOpaque(false);
        updateText(content);

        add(contentPane, BorderLayout.CENTER);
    }

    // 更新文本内容
    public void updateText(String content) {
        // 动态计算当前宽度，基于父容器
        int currentMaxWidth = Math.max(parentContainer.getWidth() - 50, 200); // 减去边距，确保最小宽度
        String css = "<style>" +
                "body { margin:0; padding:0; overflow-wrap:break-word; word-wrap:break-word; white-space:pre-wrap; max-width:" + currentMaxWidth + "px; }" +
                ".code-block { background:#F5F5F5; padding:5px; border-radius:4px; margin:3px 0; overflow-x:auto; }" +
                ".code-block pre { margin:0; padding:5px 8px; white-space:pre; }" +
                "</style>";

        // 处理换行符，普通文本依赖 CSS 换行，代码块保持原始格式
        String processedContent = content.replaceAll("\n", "<br>");
        processedContent = processedContent.replaceAll("```(\\s*(\\w+)\\s*\\n)?([\\s\\S]*?)```",
                "<div class='code-block'><pre>$3</pre></div>");

        contentPane.setText("<html>" + css + "<body>" + processedContent + "</body></html>");
        revalidate();
        repaint();
    }

    public String getText() {
        return contentPane.getText().replaceAll("<[^>]+>", ""); // 去除 HTML 标签
    }

}
