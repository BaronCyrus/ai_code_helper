package com.github.baroncyrus.aicodehelper.toolWindow;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

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


        // 获取 IDE 默认字体大小
        Font ideFont = UIUtil.getFont(UIUtil.FontSize.NORMAL, null); // 获取系统默认字体
        int ideFontSize = ideFont.getSize();
        // 消息头
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setHorizontalAlignment(SwingConstants.LEFT);
        senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, ideFontSize + 2));
        senderLabel.setForeground(isUser ? new JBColor(new Color(0x2B5278), new Color(0x8CACD6))
                : new JBColor(new Color(0x616161), new Color(0xBBBBBB)));
        senderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(senderLabel, BorderLayout.NORTH);

        // 内容区域
        contentPane = new JTextPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        contentPane.setOpaque(false);
        updateText(content,ideFontSize -1);

        add(contentPane, BorderLayout.CENTER);
    }

    // 更新文本内容
    public void updateText(String content,int ideFontSize) {
        int currentMaxWidth = Math.max(parentContainer.getWidth() - 50, 200);
        String css = "<style>" +
                "body { margin:0; padding:0; overflow-wrap:break-word; word-wrap:break-word; white-space:pre-wrap; max-width:" + currentMaxWidth + "px; font-size:" + ideFontSize + "px; font-family:'" + UIUtil.getFont(UIUtil.FontSize.NORMAL, null).getFamily() + "'; }" +
                ".code-block { background:#F5F5F5; padding:5px; border-radius:4px; margin:3px 0; overflow-x:auto; }" +
                ".code-block pre { margin:0; padding:5px 8px; white-space:pre; font-size:" + ideFontSize + "px; font-family:monospace; }" +
                "</style>";

        String processedContent = content.replaceAll("\n", "<br>");
        processedContent = processedContent.replaceAll("```(\\s*(\\w+)\\s*\\n)?([\\s\\S]*?)```",
                "<div class='code-block'><pre>$3</pre></div>");

        contentPane.setText("<html>" + css + "<body>" + processedContent + "</body></html>");
        revalidate();
        repaint();
    }

    // 重载 updateText 以保持兼容性
    public void updateText(String content) {
        Font ideFont = UIUtil.getFont(UIUtil.FontSize.NORMAL, null);
        int ideFontSize = ideFont.getSize() - 1;
        updateText(content, ideFontSize);
    }

    public String getText() {
        return contentPane.getText().replaceAll("<[^>]+>", ""); // 去除 HTML 标签
    }

}
