package com.github.baroncyrus.aicodehelper.settings;

import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class CodeAssistSettingsConfigurable implements Configurable {


    private PluginConfigPanel ui;
    private final ApiKeySettings settings = ApiKeySettings.getInstance();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "AI Code Assist"; // 修改显示名称
    }

    @Override
    public @Nullable JComponent createComponent() {
        ui = new PluginConfigPanel();
        loadSettings();
        return ui.getPanel();
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        if (ui == null) {
            return;  // 如果UI已经被销毁，直接返回
        }

        // 保存当前设置到临时变量
        String selectedClient = (String) ui.getClientComboBox().getSelectedItem();
        String selectedModule = (String) ui.getModuleComboBox().getSelectedItem();
        String commitLanguage = (String) ui.getLanguageComboBox().getSelectedItem();

        // 应用设置
        settings.setSelectedClient(selectedClient);
        settings.setSelectedModule(selectedModule);
        settings.setCommitLanguage(commitLanguage);

        // 保存prompt内容
        //Object selectedPromptType = ui.getPromptTypeComboBox().getSelectedItem();
        //if (Constants.CUSTOM_PROMPT.equals(selectedPromptType)) {
        //    saveCustomPromptsAndChoosedPrompt();
        //}
        // 保存prompt类型
        //settings.setPromptType((String) selectedPromptType);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
        ui = null;
    }

    private void loadSettings() {
        if (ui != null) {
            ui.getClientComboBox().setSelectedItem(settings.getSelectedClient());
            ui.getModuleComboBox().setSelectedItem(settings.getSelectedModule());
            ui.getLanguageComboBox().setSelectedItem(settings.getCommitLanguage());

            // 设置表格数据
            //loadCustomPrompts();
            // 设置下拉框选中项
            //loadChoosedPrompt();

            // 设置提示类型
            //ui.getPromptTypeComboBox().setSelectedItem(settings.getPromptType());
        }
    }
}
