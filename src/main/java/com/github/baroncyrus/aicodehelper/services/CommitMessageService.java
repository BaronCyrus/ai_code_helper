package com.github.baroncyrus.aicodehelper.services;


import com.github.baroncyrus.aicodehelper.settings.ApiKeySettings;
import com.github.baroncyrus.aicodehelper.constant.Constants;
import com.github.baroncyrus.aicodehelper.services.impl.*;
import com.github.baroncyrus.aicodehelper.util.PromptUtil;
import com.intellij.openapi.project.Project;

import java.util.function.Consumer;

public class CommitMessageService {
    private final AIService aiService;

    ApiKeySettings settings = ApiKeySettings.getInstance();

    public CommitMessageService() {
        String selectedClient = settings.getSelectedClient();
        this.aiService = getAIService(selectedClient);
    }

    public boolean checkNecessaryModuleConfigIsRight() {
        return aiService.checkNecessaryModuleConfigIsRight();
    }

    public String generateCommitMessage(Project project, String diff) throws Exception {
        String prompt = PromptUtil.constructPrompt(project, diff);
        return aiService.generateCommitMessage(prompt);
    }

    public void generateCommitMessageStream(Project project, String diff, Consumer<String> onContent,Consumer<String> onReasoning, Consumer<Throwable> onError,Runnable finishCallBack) throws Exception {
        String prompt = PromptUtil.constructPrompt(project, diff);
        //System.out.println(prompt);
        aiService.generateCommitMessageStream(prompt, onContent,onReasoning,onError,finishCallBack);
    }

    public void generateCommitMessageStreamWithoutPrompt(String questionString, Consumer<String> onContent,Consumer<String> onReasoning, Consumer<Throwable> onError,Runnable finishCallBack) throws Exception {
        aiService.generateCommitMessageStream(questionString, onContent,onReasoning,onError,finishCallBack);
    }

    public boolean generateByStream() {
        return aiService.generateByStream();
    }


    public static AIService getAIService(String selectedClient) {
        return switch (selectedClient) {
            //case Constants.Ollama -> new OllamaService();
            case Constants.Gemini -> new GeminiService();
            case Constants.DeepSeek -> new DeepSeekAPIService();
            case Constants.OpenAI_API -> new OpenAIAPIService();
            //case Constants.CloudflareWorkersAI -> new CloudflareWorkersAIService();
            case Constants.VolcanoEngine -> new VolcanoEngineService();
            case Constants.SiliconFlow -> new SiliconFlowService();
            case Constants.Grok -> new GrokService();
            default -> throw new IllegalArgumentException("Invalid LLM client: " + selectedClient);
        };
    }

}