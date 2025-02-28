package com.github.baroncyrus.aicodehelper.services.impl;

import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.github.baroncyrus.aicodehelper.services.AIService;
import com.github.baroncyrus.aicodehelper.util.OpenAIUtil;

import java.util.function.Consumer;

/**
 * SiliconFlowService
 *
 * @author hmydk
 */
public class SiliconFlowService implements AIService {

    @Override
    public boolean generateByStream() {
        return true;
    }

    @Override
    public String generateCommitMessage(String content) throws Exception {
        return "null";
    }

    @Override
    public void generateCommitMessageStream(String content, Consumer<String> onNext,Consumer<String> onThinking,Consumer<Throwable> onError,Runnable finishCallBack) throws Exception {
        OpenAIUtil.getAIResponseStream(Constants.SiliconFlow, content, onNext,onThinking,finishCallBack);
    }

    @Override
    public boolean checkNecessaryModuleConfigIsRight() {
        return OpenAIUtil.checkNecessaryModuleConfigIsRight(Constants.SiliconFlow);
    }


}