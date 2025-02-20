package com.github.baroncyrus.aicodehelper.toolWindow;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;



public class MessageBubble extends JPanel {
    private final JTextArea contentArea;
    private final JPanel parentContainer; // 用于获取父容器宽度
    private static final int MIN_HEIGHT = 50; // 最小高度，避免初始闪烁

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
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ideFontSize + 2));
        contentArea.setBackground(isUser ? new JBColor(new Color(0xDBE8FF), new Color(0x2B5278))
                : new JBColor(new Color(0xFFFFFF), new Color(0x3C3F41)));
        contentArea.setMinimumSize(new Dimension(0, MIN_HEIGHT));
        updateText(content);

        add(contentArea, BorderLayout.CENTER);
    }

    // 更新文本内容
    public void updateText(String processedContent) {
        contentArea.setText(processedContent);
    }

    public void updateLayout() {
        revalidate(); // 仅在需要时重新布局
    }


    public String getText() {
        return contentArea.getText();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        return new Dimension(pref.width, Math.max(pref.height, MIN_HEIGHT)); // 确保高度不小于最小值
    }
}
