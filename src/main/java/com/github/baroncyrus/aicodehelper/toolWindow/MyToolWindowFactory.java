package com.github.baroncyrus.aicodehelper.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.CompletableFuture;

public class MyToolWindowFactory implements ToolWindowFactory, DumbAware {
    private static final Icon SEND_ICON = AllIcons.Actions.Execute;
    private static final Icon LOADING_ICON = AllIcons.Process.Step_passive;

    private static ChatWindow myWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatWindow.getInstance(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(myWindow.getPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static class ChatWindow {
        private final JPanel panel;
        private final JTextArea inputArea;
        private final JButton sendButton;
        private final JPanel messageContainer;
        private final Project project;
        private JBScrollPane messageScrollPane;


        public ChatWindow(Project project) {
            this.project = project;
            panel = new JPanel(new BorderLayout());

            // 消息显示区域
            messageContainer = new JPanel();
            messageContainer.setLayout(new VerticalFlowLayout(
                    VerticalFlowLayout.LEFT,   // 水平左对齐
                    10,                        // 水平间隙 (hGap)
                    5,                         // 垂直间隙 (vGap)
                    true,                      // 水平填充 (fillHorizontally)
                    false                      // 不垂直填充 (fillVertically)
            ));



            messageScrollPane = new JBScrollPane(messageContainer);
            messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            messageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            messageScrollPane.getVerticalScrollBar().setUnitIncrement(16);//平滑移动

            panel.add(messageScrollPane, BorderLayout.CENTER);

            // 输入区域
            JPanel inputPanel = new JPanel(new BorderLayout());
            inputArea = new JTextArea(3, 20);
            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);


            sendButton = new JButton(SEND_ICON);
            sendButton.addActionListener(e -> sendMessage());
            inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            panel.add(inputPanel, BorderLayout.SOUTH);

            // 添加窗口尺寸监听（构造器中）
            panel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateAllMessageWidths();
                }
            });
        }

        public static ChatWindow getInstance(Project project) {
            if (myWindow == null) {
                myWindow = new ChatWindow(project);
            }
            return myWindow;
        }

        public JPanel getPanel() {
            return panel;
        }

        private void sendMessage() {
            String message = inputArea.getText().trim();
            if (message.isEmpty()) return;

            // 添加用户消息
            addMessage("Me", message, true);
            inputArea.setText("");
            sendButton.setIcon(LOADING_ICON);
            sendButton.setEnabled(false);


            addMessage("AI Code Assistant", "你的代码非常的棒！加油", false);
            inputArea.setText("");
            sendButton.setIcon(SEND_ICON);
            sendButton.setEnabled(true);

            // 调用OpenAI API（示例代码）
//            CompletableFuture.runAsync(() -> {
//                try {
//                    OpenAIClient client = new OpenAIClient();
//                    client.streamingChat(message, chunk -> {
//                        // 在EDT线程更新UI
//                        SwingUtilities.invokeLater(() -> {
//                            updateAssistantMessage(chunk.getContent());
//                        });
//                    });
//                } finally {
//                    SwingUtilities.invokeLater(() -> {
//                        sendButton.setIcon(SEND_ICON);
//                        sendButton.setEnabled(true);
//                    });
//                }
//            });
        }

        public void addMessage(String sender, String content, boolean isUser) {
            System.out.println("addMessage: " + content);
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> addMessage(sender, content, isUser));
                return;
            }

            JPanel messagePanel = new JPanel(new BorderLayout());
            messagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createMatteBorder(0, 3, 0, 0, isUser ? JBColor.yellow : JBColor.GRAY)));
            messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            messagePanel.setBackground(isUser ? new JBColor(new Color(0xDBE8FF), new Color(0x2B5278)) :new JBColor(new Color(0xFFFFFF), new Color(0x3C3F41)));

            // 同步调整边框颜色（可选）
            Border borderColor = BorderFactory.createMatteBorder(0, 0, 0, 0, JBColor.WHITE); // 浅灰/深灰
            messagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), borderColor));

            // 消息头
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD).deriveFont(14f));
            senderLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            senderLabel.setForeground(isUser ? new JBColor(new Color(0x2B5278), new Color(0x8CACD6)) : new JBColor(new Color(0x616161), new Color(0xBBBBBB)));

            // 内容面板
            // 内容面板
            JTextPane contentPane = new JTextPane() {
                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    d.width = Math.min(calculateMaxWidth(), d.width);
                    return d;
                }
            };
            contentPane.setContentType("text/html");
            contentPane.setEditable(false);
            contentPane.setOpaque(false);
            contentPane.setBackground(null);
            contentPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);

            // 动态CSS
            String fontFamily = contentPane.getFont().getName().replaceAll("'", "''");
            String css = "<style>"
                    + "body {"
                    + "  margin:0; padding:0;"
                    + "  word-wrap:break-word !important;"
                    + "  white-space:pre-wrap !important;"
                    + "  max-width:" + calculateMaxWidth() + "px !important;"
                    + "  font-family:'" + fontFamily + "', sans-serif;"
                    + "  font-size:" + contentPane.getFont().getSize() + "px;"
                    + "}"
                    + ".lang-label {"
                    + "  background: #e0e0e0;"
                    + "  color: #666;"
                    + "  padding: 2px 8px;"
                    + "  border-radius: 4px 4px 0 0;"
                    + "  font-size: 0.8em;"
                    + "  font-family: monospace;"
                    + "  display: none;"
                    + "}"
                    + ".lang-label:not(:empty) { display: block; }"
                    + ".code-block {"
                    + "  background:#F5F5F5;"
                    + "  padding:5px 0;"
                    + "  border-radius:4px;"
                    + "  margin:3px 0;"
                    + "  overflow-x:auto;"
                    + "}"
                    + ".code-block pre {"
                    + "  margin:0;"
                    + "  padding:5px 8px;"
                    + "  white-space:pre;"
                    + "}"
                    + "</style>";

            // 处理代码块
            String processed = content.replaceAll("```(\\s*(\\w+)\\s*\\n)?([\\s\\S]*?)```","<div class='code-block'><div class='lang-label'>$2</div><pre>$3</pre></div>");

            contentPane.setText("<html>" + css + "<body>" + processed + "</body></html>");

            // 精确计算文本高度
            SwingUtilities.invokeLater(() -> {
                try {
                    int textHeight = contentPane.getPreferredSize().height;
                    contentPane.setPreferredSize(new Dimension(calculateMaxWidth(), textHeight));
                    messagePanel.revalidate();
                } catch (Exception e) {
                    Logger.getInstance(ChatWindow.class).error(e);
                }
            });

            // 添加组件
            messagePanel.add(senderLabel, BorderLayout.NORTH);
            messagePanel.add(contentPane, BorderLayout.CENTER);

            // 添加到容器
            messageContainer.add(messagePanel);
            messageContainer.add(Box.createVerticalStrut(10));

            // 触发更新
            messageContainer.revalidate();
            messageContainer.repaint();
            scrollToBottom();
        }

        // 新增宽度计算方法
        private int calculateMaxWidth() {
            int panelWidth = panel.getWidth();
            return Math.max(panelWidth - 50, 200); // 确保最小宽度200px
        }

        // 新增消息宽度更新方法
        private void updateAllMessageWidths() {
            for (Component comp : messageContainer.getComponents()) {
                if (comp instanceof JPanel) {
                    Component[] children = ((JPanel) comp).getComponents();
                    for (Component child : children) {
                        if (child instanceof JTextPane) {
                            JTextPane pane = (JTextPane) child;
                            updateTextPaneWidth(pane);
                        }
                    }
                }
            }
        }

        private void updateTextPaneWidth(JTextPane pane) {
            int width = calculateMaxWidth();
            pane.setSize(width, pane.getHeight()); // 显式设置尺寸
            HTMLDocument doc = (HTMLDocument) pane.getDocument();
            doc.getStyleSheet().addRule(
                    "body { max-width: " + width + "px !important; }"
            );
            pane.setContentType("text/html"); // 强制刷新
            pane.setText(pane.getText());
        }

        // 在scrollToBottom方法中添加异步滚动
        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
                messageScrollPane.revalidate();
                messageScrollPane.repaint();
            });
        }

    }
}