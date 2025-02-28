package com.github.baroncyrus.aicodehelper.constant;

import com.github.baroncyrus.aicodehelper.settings.ApiKeySettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants
 *
 * @author hmydk
 */
public class Constants {

    public static final String NO_FILE_SELECTED = "No file selected";
    public static final String GENERATING_COMMIT_MESSAGE = "Generating commit message...";
    public static final String TASK_TITLE = "Generating commit message";

    public static final String[] languages = {"English", "Chinese", "Japanese", "Korean", "French", "Spanish",
            "German", "Russian", "Arabic", "Portuguese"};

    public static final String[] thinkingModels = {"deepseek-reasoner", "deepseek-ai/DeepSeek-R1","Pro/deepseek-ai/DeepSeek-R1"};

    public static final String PROJECT_PROMPT_FILE_NAME = "commit-prompt.txt";
    public static final String PROJECT_PROMPT = "Project Prompt";
    public static final String CUSTOM_PROMPT = "Custom Prompt";

    public static String[] getAllPromptTypes() {
        return new String[]{PROJECT_PROMPT, CUSTOM_PROMPT};
    }

    public static final String Gemini = "Gemini";
    public static final String DeepSeek = "DeepSeek";
    public static final String Ollama = "Ollama";
    public static final String OpenAI_API = "OpenAI";
    public static final String 阿里云百炼 = "阿里云百炼(Model Hub)";
    public static final String SiliconFlow = "SiliconFlow(Model Hub)";//硅基流动
    public static final String CloudflareWorkersAI = "Cloudflare Workers AI";
    public static final String VolcanoEngine = "VolcanoEngine(Model Hub)";//火山引擎 字节跳动旗下
    public static final String Grok = "Grok";

    public static final String[] LLM_CLIENTS = {DeepSeek,Gemini,OpenAI_API,Grok,SiliconFlow,VolcanoEngine};

    // 文件后缀到语言标记的映射表
    public static final Map<String, String> LANGUAGE_MAP = new HashMap<>(){
        {
            put("cs", "csharp");
            put("java", "java");
            put("py", "python");
            put("cpp", "cpp");
            put("c", "c");
            put("js", "javascript");
            put("ts", "typescript");
            put("rb", "ruby");
            put("php", "php");
            put("go", "go");
            put("kt", "kotlin");
        }
    };


    public static final Map<String, String[]> CLIENT_MODULES = new HashMap<>() {
        {
            put(DeepSeek, new String[]{"deepseek-chat","deepseek-reasoner"});
            put(Gemini, new String[]{"gemini-2.0-flash", "gemini-2.0-flash-lite", "gemini-1.5-flash", "gemini-1.5-pro"});
            put(OpenAI_API, new String[]{"gpt-4o","gpt-4o-mini","o1","o1-mini","o3-mini"});
            put(SiliconFlow, new String[]{"deepseek-ai/DeepSeek-V3","deepseek-ai/DeepSeek-R1", "Pro/deepseek-ai/DeepSeek-V3","Pro/deepseek-ai/DeepSeek-R1"});
            put(Ollama, new String[]{"qwen2.5:14b", "llama3.2:3b"});
            put(CloudflareWorkersAI,new String[]{"@cf/meta/llama-3.1-70b-instruct", "@cf/meta/llama-3.1-8b-instruct"});
            put(阿里云百炼, new String[]{"qwen-plus"});
            put(VolcanoEngine,new String[]{""});
            put(Grok,new String[]{"grok-2"});
        }
    };

    public static Map<String, ApiKeySettings.ModuleConfig> moduleConfigs = new HashMap<>() {
        {
            put(Gemini, new ApiKeySettings.ModuleConfig("https://generativelanguage.googleapis.com/v1/models", ""));
            put(DeepSeek, new ApiKeySettings.ModuleConfig("https://api.deepseek.com/chat/completions", ""));
            put(Ollama, new ApiKeySettings.ModuleConfig("http://localhost:11434/api/generate", ""));
            put(OpenAI_API, new ApiKeySettings.ModuleConfig("https://api.openai.com/v1/chat/completions", ""));
            put(阿里云百炼, new ApiKeySettings.ModuleConfig("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", ""));
            put(SiliconFlow, new ApiKeySettings.ModuleConfig("https://api.siliconflow.cn/v1/chat/completions", ""));
            put(CloudflareWorkersAI, new ApiKeySettings.ModuleConfig("https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/v1/chat/completions", ""));
            put(VolcanoEngine,new ApiKeySettings.ModuleConfig("https://ark.cn-beijing.volces.com/api/v3/chat/completions", ""));
            put(Grok,new ApiKeySettings.ModuleConfig("https://api.x.ai/v1/chat/completions", ""));
        }
    };

    public static final Map<String, String> CLIENT_HELP_URLS = new HashMap<>() {
        {
            put(Constants.Gemini, "https://aistudio.google.com/app/apikey");
            put(Constants.DeepSeek, "https://platform.deepseek.com/api_keys");
            put(Constants.CloudflareWorkersAI, "https://developers.cloudflare.com/workers-ai/get-started/rest-api");
            put(Constants.阿里云百炼, "https://help.aliyun.com/zh/model-studio/developer-reference/get-api-key?spm=0.0.0.i7");
            put(Constants.SiliconFlow, "https://cloud.siliconflow.cn/i/lszKPlCW");
            put(Constants.OpenAI_API, "https://platform.openai.com/docs/overview");
            put(Constants.Grok,"https://console.x.ai");
            put(Constants.VolcanoEngine,"https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey");
        }
    };

    public static String getHelpText(String client) {
        return switch (client) {
            case Gemini ->
                    "<html>Get your API key from <a href='https://aistudio.google.com/app/apikey'>Google AI Studio</a></html>";
            case DeepSeek -> "<html>" +
                    "<li>Get your API key from <a href='https://platform.deepseek.com/api_keys'>platform.deepseek.com</a></li>" +
                    "<li>Current model is deepseek-v3.</li>" +
                    "<li>DeepSeek servers is not stable currently.</li>" +
                    "</html>";
            case Ollama ->
                    "<html><li>Make sure Ollama is running locally on the specified URL</li><li>API Key is not required</li></html>";
            case OpenAI_API -> "<html>" +
                    "<li>Please confirm whether the current model supports the OpenAI API format.</li>"
                    +
                    "<li>Replace {host} with the host defined in the model.</li>" +
                    "<li>Refer to the API definition on the <a href='https://platform.openai.com/docs/overview'>OpenAI Platform</a>.</li>" +
                    "</html>";
            case CloudflareWorkersAI -> "<html>" +
                    "<li>Please refer to the <a href='https://developers.cloudflare.com/workers-ai/get-started/rest-api'>official documentation</a> for details</li>"
                    +
                    "<li>Replace {account_id} with your Cloudflare account ID</li>" +
                    "</html>";
            case 阿里云百炼 ->
                    "<html>Get your API key from <a href='https://help.aliyun.com/zh/model-studio/developer-reference/get-api-key?spm=0.0.0.i7'>" + 阿里云百炼 + "</a></html>";
            case SiliconFlow ->
                    "<html>Get your API key from <a href='https://cloud.siliconflow.cn/i/lszKPlCW'>" + SiliconFlow + "</a></html>";
            case Grok ->
                    "<html>Get your API key from <a href='https://console.x.ai'>" + Grok + "</a></html>";
            case VolcanoEngine ->
                    "<html>Get your API key from <a href='https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey'>" + VolcanoEngine + "</a></html>";
            default -> "";
        };
    }
}