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
import com.intellij.openapi.ui.Messages;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.github.baroncyrus.aicodehelper.toolWindow.MyToolWindowFactory;

import static com.github.baroncyrus.aicodehelper.constant.Constants.LANGUAGE_MAP;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Action 类，用于生成 diff code reivew 消息
 * 继承自 AnAction 以集成到 IDEA 的操作系统中
 */
public class DiffCodeReviewMessageAction extends AnAction {

    /**
     * 获取CommitMessage对象
     */
    private CommitMessage getCommitMessage(AnActionEvent e) {
        return (CommitMessage) e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        if (project == null) {
            return;
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


        List<Change> includedChanges = commitWorkflowHandler.getUi().getIncludedChanges();
        List<FilePath> includedUnversionedFiles = commitWorkflowHandler.getUi().getIncludedUnversionedFiles();

        if (includedChanges.isEmpty() && includedUnversionedFiles.isEmpty()) {
            Messages.showErrorDialog("No File Selected!", "Error");
            return;
        }


        ApiKeySettings settings = ApiKeySettings.getInstance();
        String selectedModule = settings.getSelectedModule();


        String selectedText = GItUtil.computeDiff(includedChanges, includedUnversionedFiles, project);
        // 获取当前文件
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);


        // 动态构造提示
        String questionString = "Code Review these Code Diff: \n" + selectedText;

        // 显示工具窗口
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AICodeAssist");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                MyToolWindowFactory.ChatWindow window = MyToolWindowFactory.ChatWindow.getInstance(project);
                if (window != null) {
                    window.sendMessageBySelectDiffFiles(questionString);
                }
            });
        }

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
        boolean inProgress = false;
        Project project = e.getProject();
        if (project != null) {
            MyToolWindowFactory.ChatWindow window = MyToolWindowFactory.ChatWindow.getInstance(project);
            if (window != null) {
                inProgress = window.isGenerating;
            }
        }
        e.getPresentation().setEnabled(!inProgress);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 指定在后台线程更新 Action 状态，提高性能
        return ActionUpdateThread.BGT;
    }

}
