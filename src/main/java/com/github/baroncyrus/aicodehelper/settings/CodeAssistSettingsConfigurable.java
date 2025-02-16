package com.github.baroncyrus.aicodehelper.settings;

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
        return false;
    }

    @Override
    public void apply() {

    }

    private void loadSettings() {
        if (ui != null) {
            ui.getClientComboBox().setSelectedItem(settings.getSelectedClient());
            //ui.getModuleComboBox().setSelectedItem(settings.getSelectedModule());
            //ui.getLanguageComboBox().setSelectedItem(settings.getCommitLanguage());

            // 设置表格数据
            //loadCustomPrompts();
            // 设置下拉框选中项
            //loadChoosedPrompt();

            // 设置提示类型
            //ui.getPromptTypeComboBox().setSelectedItem(settings.getPromptType());
        }
    }
}
