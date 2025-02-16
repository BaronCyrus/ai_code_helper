package com.github.baroncyrus.aicodehelper.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class CodeAssistSettingsConfigurable implements Configurable {

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "AI Code Assist"; // 修改显示名称
    }

    @Override
    public @Nullable JComponent createComponent() {
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {

    }
}
