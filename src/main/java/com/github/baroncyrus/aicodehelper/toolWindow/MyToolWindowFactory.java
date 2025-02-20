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

            // 动态调整输入框高度
            inputScrollPane = new JBScrollPane(inputArea);
            inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            inputScrollPane.setBorder(BorderFactory.createEmptyBorder()); // 移除默认边框美化
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
            inputPanel.add(inputScrollPane, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);
            panel.add(inputPanel, BorderLayout.SOUTH);

            // 窗口大小变化监听
            panel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        messageContainer.revalidate();
                        messageContainer.repaint();
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
                int lineCount = inputArea.getLineCount();
                int newRows = Math.min(Math.max(lineCount, MIN_ROWS), MAX_ROWS);
                inputArea.setRows(newRows);

                // 如果达到最大行数，启用滚动条
                if (lineCount > MAX_ROWS) {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                } else {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                }

                inputScrollPane.revalidate();
                inputScrollPane.repaint();
            });
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