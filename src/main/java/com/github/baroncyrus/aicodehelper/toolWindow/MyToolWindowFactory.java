package com.github.baroncyrus.aicodehelper.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;

public class MyToolWindowFactory implements ToolWindowFactory, DumbAware {
    private static final Icon SEND_ICON = AllIcons.Actions.Execute;
    private static final Icon LOADING_ICON = AllIcons.Process.Step_passive;
    private static final Icon CLEAR_ICON = AllIcons.Actions.GC;

    private static ChatWindow myWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ChatWindow.getInstance(project);

        // 添加清空按钮到标题栏
        AnAction clearAction = new AnAction("Clear Messages", "Clear all chat messages", CLEAR_ICON) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                myWindow.clearMessages();
            }
        };
        toolWindow.setTitleActions(Collections.singletonList(clearAction));

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
        private final JBScrollPane messageScrollPane;
        private JBScrollPane inputScrollPane;
        private JPanel inputWrapper; // 新增包装面板

        // 输入框高度设置
        private static final int MIN_ROWS = 3;  // 默认最小行数 (a)
        private static final int MAX_ROWS = 10; // 最大行数 (b)

        public ChatWindow(Project project) {
            this.project = project;
            panel = new JPanel(new BorderLayout());

            // 消息显示区域，使用自定义 VerticalFlowLayout
            messageContainer = new JPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 5));
            messageScrollPane = new JBScrollPane(messageContainer);
            messageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            messageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            messageScrollPane.getVerticalScrollBar().setUnitIncrement(16); // 平滑滚动
            panel.add(messageScrollPane, BorderLayout.CENTER);


            // 输入区域
            JPanel inputPanel = new JPanel(new BorderLayout());
            inputArea = new JTextArea(MIN_ROWS, 20);
            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);

            // 使用包装面板控制输入框向上扩展
            inputWrapper = new JPanel(new BorderLayout());
            inputScrollPane = new JBScrollPane(inputArea);
            inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
            inputWrapper.add(inputScrollPane, BorderLayout.CENTER);
            adjustInputHeight();

            inputArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    adjustInputHeight();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    adjustInputHeight();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    adjustInputHeight();
                }
            });



            sendButton = new JButton(SEND_ICON);
            sendButton.addActionListener(e -> sendMessage());
            inputPanel.add(inputWrapper, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            panel.add(inputPanel, BorderLayout.SOUTH);

            // 窗口大小变化监听
            panel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        messageContainer.revalidate();
                        messageContainer.repaint();
                        adjustInputHeight(); // 窗口大小变化时也调整输入框
                    });
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

        // 调整输入框高度
        private void adjustInputHeight() {
            SwingUtilities.invokeLater(() -> {
                int rowHeight = inputArea.getFontMetrics(inputArea.getFont()).getHeight();
                int availableWidth = inputArea.getWidth() > 0 ? inputArea.getWidth() : 300;
                int requiredRows = getRowsForText(inputArea.getText(), availableWidth);
                int displayRows = Math.min(Math.max(requiredRows, MIN_ROWS), MAX_ROWS);

                // 设置 inputArea 的首选高度为实际所需高度
                int contentHeight = requiredRows * rowHeight;
                inputArea.setPreferredSize(new Dimension(availableWidth, contentHeight));

                // 设置 inputWrapper 的显示高度，限制在 MAX_ROWS
                int displayHeight = displayRows * rowHeight;
                inputWrapper.setPreferredSize(new Dimension(availableWidth, displayHeight));
                inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, displayHeight));

                // 控制滚动条显示
                if (requiredRows > MAX_ROWS) {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                } else {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                }

                inputWrapper.revalidate();
                inputWrapper.repaint();
                panel.revalidate();
                panel.repaint();
            });
        }

        // 计算文本所需的行数
        private int getRowsForText(String text, int width) {
            if (text.isEmpty()) return 1;
            FontMetrics fm = inputArea.getFontMetrics(inputArea.getFont());
            int lineHeight = fm.getHeight();
            int availableWidth = width - 10; // 留出边距
            int totalHeight = 0;

            String[] lines = text.split("\n");
            for (String line : lines) {
                int lineWidth = fm.stringWidth(line);
                int wrappedLines = (int) Math.ceil((double) lineWidth / availableWidth);
                totalHeight += Math.max(wrappedLines, 1) * lineHeight;
            }

            return (int) Math.ceil((double) totalHeight / lineHeight);
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

        }

        public void addMessage(String sender, String content, boolean isUser) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> addMessage(sender, content, isUser));
                return;
            }

            MessageBubble bubble = new MessageBubble(sender, content, isUser, messageContainer.getWidth());
            messageContainer.add(bubble);
            messageContainer.revalidate();
            messageContainer.repaint();
            scrollToBottom();
        }

        // 添加流式消息
        private void addStreamingMessage(String sender, String content) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> addStreamingMessage(sender, content));
                return;
            }

            MessageBubble bubble = new MessageBubble(sender, "", false, messageContainer.getWidth());
            messageContainer.add(bubble);
            messageContainer.revalidate();
            messageContainer.repaint();
            scrollToBottom();

            // 使用 SwingWorker 实现流式输出
            new SwingWorker<Void, Character>() {
                @Override
                protected Void doInBackground() {
                    for (char c : content.toCharArray()) {
                        publish(c);
                        try {
                            Thread.sleep(100); // 模拟延迟
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Character> chunks) {
                    StringBuilder sb = new StringBuilder(bubble.getText());
                    for (char c : chunks) {
                        sb.append(c);
                    }
                    bubble.updateText(sb.toString());
                    messageContainer.revalidate();
                    messageContainer.repaint();
                    scrollToBottom();
                }
            }.execute();
        }

        // 清空所有消息
        public void clearMessages() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this::clearMessages);
                return;
            }
            messageContainer.removeAll();
            messageContainer.revalidate();
            messageContainer.repaint();
        }


        // 滚动到底部
        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        }

    }
}