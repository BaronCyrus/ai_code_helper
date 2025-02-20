package com.github.baroncyrus.aicodehelper.toolWindow;

import com.github.baroncyrus.aicodehelper.services.CommitMessageService;
import com.github.baroncyrus.aicodehelper.settings.ApiKeySettings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
                // 添加二次确认弹窗
                int result = Messages.showYesNoDialog(
                        "Are you sure you want to clear all messages?", // 提示信息
                        "Confirm Clear",                        // 对话框标题
                        "Yes", "No",                            // 自定义按钮文本（确认/取消）
                        Messages.getQuestionIcon()              // 图标
                );
                if (result == Messages.YES) { // 用户点击“确认”（Yes）
                    myWindow.clearMessages();
                }
                // 如果点击“No”或关闭对话框，则不执行任何操作
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
        private volatile boolean isGenerating = false; // 标记是否正在生成回答

        // 输入框高度设置
        private static final int MIN_ROWS = 3;  // 默认最小行数 (a)
        private static final int MAX_ROWS = 10; // 最大行数 (b)

        private static final int UPDATE_INTERVAL_MS = 200; // 每200ms更新一次 UI
        private final Timer updateTimer;
        private volatile boolean needsUpdate = false;
        private MessageBubble currentBubble;

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
                        adjustInputHeight();
                        messageContainer.revalidate();
                        messageContainer.repaint();
                    });
                }
            });

            // 初始化更新定时器
            updateTimer = new Timer(true);
            updateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (needsUpdate && isGenerating && currentBubble != null) {
                        SwingUtilities.invokeLater(() -> {
                            currentBubble.updateLayout(); // 更新布局
                            messageContainer.repaint(); // 只重绘，不重新布局
                            scrollToBottom();
                            needsUpdate = false;
                        });
                    }
                }
            }, 0, UPDATE_INTERVAL_MS);
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

                int contentHeight = requiredRows * rowHeight;
                inputArea.setPreferredSize(new Dimension(availableWidth, contentHeight));

                int displayHeight = displayRows * rowHeight;
                inputWrapper.setPreferredSize(new Dimension(availableWidth, displayHeight));
                inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, displayHeight));

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
            int availableWidth = width - 10;
            int totalHeight = 0;

            String[] lines = text.split("\n");
            for (String line : lines) {
                int lineWidth = fm.stringWidth(line);
                int wrappedLines = (int) Math.ceil((double) lineWidth / availableWidth);
                totalHeight += Math.max(wrappedLines, 1) * lineHeight;
            }

            return (int) Math.ceil((double) totalHeight / lineHeight);
        }


        public void sendMessageBySelectFunction(String questionMessage){
            if (isGenerating) {
                System.out.println("Please wait for the current AI response to complete.");
                return; // 如果正在生成回答，阻止新消息发送
            }

            if (questionMessage.isEmpty()) return;

            // 添加用户消息
            addMessage("Me", questionMessage, true);

            inputArea.setText("");
            sendButton.setIcon(LOADING_ICON);
            sendButton.setEnabled(false);
            isGenerating = true; // 设置生成标志

            // 在后台线程中调用 AI 流式回答，避免卡死
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    addStreamingMessage("AI Code Assistant", questionMessage);
                    return null;
                }
            }.execute();
        }

        private void sendMessage() {
            if (isGenerating) {
                System.out.println("Please wait for the current AI response to complete.");
                return; // 如果正在生成回答，阻止新消息发送
            }

            String message = inputArea.getText().trim();
            if (message.isEmpty()) return;

            // 添加用户消息
            addMessage("Me", message, true);
            inputArea.setText("");
            sendButton.setIcon(LOADING_ICON);
            sendButton.setEnabled(false);
            isGenerating = true; // 设置生成标志


            // 在后台线程中调用 AI 流式回答，避免卡死
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    addStreamingMessage("AI Code Assistant", message);
                    return null;
                }
            }.execute();
        }

        public void addMessage(String sender, String content, boolean isUser) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> addMessage(sender, content, isUser));
                return;
            }

            MessageBubble bubble = new MessageBubble(sender, content, isUser, messageContainer);
            messageContainer.add(bubble);
            messageContainer.revalidate();
            messageContainer.repaint();
            scrollToBottom();
        }

        private final StringBuilder contentBuilder = new StringBuilder();
        private final StringBuilder contentResBuilder = new StringBuilder();
        // 添加流式消息，使用 DeepSeek API
        private void addStreamingMessage(String sender, String userMessage) {
            var commitMessageService = new CommitMessageService();
            ApiKeySettings settings = ApiKeySettings.getInstance();
            var promptContent = "Reply in {language}.";
            promptContent = promptContent.replace("{language}", settings.getCommitLanguage());
            //内置语言prompt
            userMessage = promptContent + userMessage;

            contentBuilder.setLength(0);//清理
            contentResBuilder.setLength(0);
            contentResBuilder.append("think start\n");
            inputArea.setText("Generating Answer...");//设置正在生成
            currentBubble = new MessageBubble(sender, "", false, messageContainer);

            SwingUtilities.invokeLater(() -> {
                messageContainer.add(currentBubble);
                messageContainer.revalidate();
                messageContainer.repaint();
                scrollToBottom();
            });

            try {

                commitMessageService.generateCommitMessageStreamWithoutPrompt(
                        userMessage,
                        token -> SwingUtilities.invokeLater(() -> {
                            contentBuilder.append(token);
                            if (contentResBuilder.length() > 100){
                                currentBubble.updateText(contentResBuilder + "\nthink end" + contentBuilder);
                            }else{
                                currentBubble.updateText(contentBuilder.toString());
                            }
                            needsUpdate = true;
                        }),
                        reasoning -> {
                            if (!Objects.equals(reasoning, "null") && !Objects.equals(reasoning, "")){
                                contentResBuilder.append(reasoning);
                                currentBubble.updateText(contentResBuilder.toString());
                                needsUpdate = true;
                            }
                        }, // 可选处理推理内容
                        error -> SwingUtilities.invokeLater(() -> {
                            addMessage("System", "Error: " + error.getMessage(), false);
                            finishGeneration();
                        }),
                        () -> {
                            SwingUtilities.invokeLater(this::finishGeneration);
                        }
                );
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    addMessage("System", "Error generating response: " + ex.getMessage(), false);
                    finishGeneration();
                });
            }
        }

        // 完成生成后的处理
        private void finishGeneration() {
            sendButton.setIcon(SEND_ICON);
            sendButton.setEnabled(true); // 恢复发送按钮
            isGenerating = false; // 重置生成标志
            inputArea.setText("");
            currentBubble = null;
            messageContainer.revalidate();
            messageContainer.repaint();
            scrollToBottom();
        }

        public void clearMessages() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this::clearMessages);
                return;
            }
            messageContainer.removeAll();
            messageContainer.revalidate();
            messageContainer.repaint();
        }

        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                if (currentBubble != null) {
                    Rectangle rect = currentBubble.getBounds();
                    messageScrollPane.getViewport().scrollRectToVisible(rect);
                }
            });
        }

        // 检查是否正在生成回答
        public boolean isGenerating() {
            return isGenerating;
        }
    }
}