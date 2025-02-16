package com.github.baroncyrus.aicodehelper.services.impl;

import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.github.baroncyrus.aicodehelper.services.AIService;
import com.github.baroncyrus.aicodehelper.util.OpenAIUtil;

import java.util.function.Consumer;

public class VolcanoEngineService implements AIService {
    @Override
    public boolean generateByStream() {
        return true;
    }

    @Override
    public String generateCommitMessage(String content) throws Exception {
        return "null";
    }

    @Override
    public void generateCommitMessageStream(String content, Consumer<String> onNext)
            throws Exception {
        OpenAIUtil.getAIResponseStream(Constants.VolcanoEngine, content, onNext);
    }

    @Override
    public boolean checkNecessaryModuleConfigIsRight() {
        return OpenAIUtil.checkNecessaryModuleConfigIsRight(Constants.VolcanoEngine);
    }
}
