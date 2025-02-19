package com.github.baroncyrus.aicodehelper.actions;

import com.github.baroncyrus.aicodehelper.popWindow.DynamicInfoPopupUI;
import com.github.baroncyrus.aicodehelper.settings.ApiKeySettings;
import com.intellij.openapi.actionSystem.*;


import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.github.baroncyrus.aicodehelper.services.CommitMessageService;
import com.github.baroncyrus.aicodehelper.util.GItUtil;
import com.github.baroncyrus.aicodehelper.util.IdeaDialogUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Action 类，用于生成 Git commit 消息
 * 继承自 AnAction 以集成到 IDEA 的操作系统中
 */
public class GenerateCommitMessageAction extends AnAction {

    /**
     * 获取CommitMessage对象
     */
    private CommitMessage getCommitMessage(AnActionEvent e) {
        return (CommitMessage) e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
    }

    //private final StringBuilder messageBuilder = new StringBuilder();
    private final StringBuilder contentBuilder = new StringBuilder();
    private final StringBuilder reasoningBuilder = new StringBuilder();

    private volatile DynamicInfoPopupUI thinkingPopup;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (thinkingPopup != null && thinkingPopup.isShowing()){
            System.out.println("please wait for thinking end");
            return;
        }


        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 初始化弹窗
        if (thinkingPopup == null){
            thinkingPopup = new DynamicInfoPopupUI();
            thinkingPopup.init(project);
        }

        // 根据配置，创建对应的服务
        CommitMessageService commitMessageService = new CommitMessageService();

        if (!commitMessageService.checkNecessaryModuleConfigIsRight()) {
            IdeaDialogUtil.handleModuleNecessaryConfigIsWrong(project);
            return;
        }

        AbstractCommitWorkflowHandler<?, ?> commitWorkflowHandler = (AbstractCommitWorkflowHandler<?, ?>) e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (commitWorkflowHandler == null) {
            IdeaDialogUtil.handleNoChangesSelected(project);
            return;
        }

        CommitMessage commitMessage = getCommitMessage(e);

        List<Change> includedChanges = commitWorkflowHandler.getUi().getIncludedChanges();
        List<FilePath> includedUnversionedFiles = commitWorkflowHandler.getUi().getIncludedUnversionedFiles();

        if (includedChanges.isEmpty() && includedUnversionedFiles.isEmpty()) {
            commitMessage.setCommitMessage(Constants.NO_FILE_SELECTED);
            return;
        }

        commitMessage.setCommitMessage(Constants.GENERATING_COMMIT_MESSAGE);

        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();

        // Run the time-consuming operations in a background task
        ProgressManager.getInstance().run(new Task.Backgroundable(project, Constants.TASK_TITLE, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    String diff = GItUtil.computeDiff(includedChanges, includedUnversionedFiles, project);
//                    System.out.println("diff: " + diff);
                    if (commitMessageService.generateByStream()) {
                        contentBuilder.setLength(0);
                        reasoningBuilder.setLength(0);

                        //带思维的模型显示弹窗
                        if (Arrays.asList(Constants.thinkingModels).contains(selectedModule)){
                            showThinkingPopup(project); // 显示弹窗
                        }

                        commitMessageService.generateCommitMessageStream(
                                project,
                                diff,
                                // onNext 处理每个token
                                tokenC ->updateCommitMessage(tokenC,commitMessage),
                                tokenR -> updateThinkingPopup(tokenR),
                                // onError 处理错误
                                error -> handleException(project, error),
                                () -> finalizeGeneration(commitMessage)
                        );
                    } else {
                        String commitMessageFromAi = commitMessageService.generateCommitMessage(project, diff).trim();
                        ApplicationManager.getApplication().invokeLater(() -> {
                            commitMessage.setCommitMessage(commitMessageFromAi);
                        });
                    }
                } catch (IllegalArgumentException ex) {
                    IdeaDialogUtil.showWarning(project, ex.getMessage(), "AI Commit Message Warning");
                } catch (Exception ex) {
                    IdeaDialogUtil.showError(project, "Error generating commit message: <br>" + getErrorMessage(ex.getMessage()), "Error");
                }
            }
        });
    }

    // 显示思考过程弹窗
    private void showThinkingPopup(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!thinkingPopup.isShowing()) {
                thinkingPopup.show(project);
            }
        });
    }

    // 更新提交消息内容
    private void updateCommitMessage(String token, CommitMessage commitMessage) {
        ApplicationManager.getApplication().invokeLater(() -> {
            contentBuilder.append(token);
            commitMessage.setCommitMessage(contentBuilder.toString());
        });
    }

    // 更新思考弹窗内容
    private void updateThinkingPopup(String token) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (thinkingPopup.isShowing()) {
                reasoningBuilder.append(token);
                thinkingPopup.appendText(token);
                // 可选：自动滚动处理
                JTextArea textArea = thinkingPopup.getContentArea();
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });
    }

    // 完成生成后的处理
    private void finalizeGeneration(CommitMessage commitMessage) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // 清理思考过程内容
            String finalMessage = contentBuilder.toString().trim();
            commitMessage.setCommitMessage(finalMessage);

            closeThinkingPopup();
        });
    }

    // 安全关闭弹窗
    private void closeThinkingPopup() {
        if (thinkingPopup != null){
            if(thinkingPopup.isShowing()){
                thinkingPopup.close();
            }
            thinkingPopup = null;
        }
    }

    // 异常处理
    private void handleException(Project project, Throwable error) {
        closeThinkingPopup();
        // ... 原有异常处理逻辑 ...
        ApplicationManager.getApplication().invokeLater(() -> {
            IdeaDialogUtil.showError(project, "Error generating commit message: <br>" + getErrorMessage(error.getMessage()), "Error");
        });
    }

    // 重置构建器
    private void resetBuilders() {
        contentBuilder.setLength(0);
        reasoningBuilder.setLength(0);
    }

    private static @NotNull String getErrorMessage(String errorMessage) {
        if (errorMessage.contains("429")) {
            errorMessage = "Too many requests. Please try again later.";
        } else if (errorMessage.contains("Read timeout") || errorMessage.contains("Timeout") || errorMessage.contains("timed out")) {
            errorMessage = "Read timeout. Please try again later. <br> " +
                    "This may be caused by the API key or network issues or the server is busy.";
        } else if (errorMessage.contains("400")) {
            errorMessage = "Bad Request. Please try again later.";
        } else if (errorMessage.contains("401")) {
            errorMessage = "Unauthorized. Please check your API key.";
        } else if (errorMessage.contains("403")) {
            errorMessage = "Forbidden. Please check your API key.";
        } else if (errorMessage.contains("404")) {
            errorMessage = "Not Found. Please check your API key.";
        } else if (errorMessage.contains("500")) {
            errorMessage = "Internal Server Error. Please try again later.";
        } else if (errorMessage.contains("502")) {
            errorMessage = "Bad Gateway. Please try again later.";
        } else if (errorMessage.contains("503")) {
            errorMessage = "Service Unavailable. Please try again later.";
        } else if (errorMessage.contains("504")) {
            errorMessage = "Gateway Timeout. Please try again later.";
        }
        return errorMessage;
    }

    //防止重复点击的状态检查
    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean inProgress = thinkingPopup != null && thinkingPopup.isShowing();
        e.getPresentation().setEnabled(!inProgress);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 指定在后台线程更新 Action 状态，提高性能
        return ActionUpdateThread.BGT;
    }

}
