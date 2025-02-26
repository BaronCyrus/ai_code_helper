package com.github.baroncyrus.aicodehelper.services;

import com.github.baroncyrus.aicodehelper.util.OpenAIUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AIService
 *
 * @author hmydk
 */
public interface AIService {

    boolean generateByStream();

    String generateCommitMessage(String content) throws Exception;

    void generateCommitMessageStream(String content, Consumer<String> onContent,Consumer<String> onReasoning,Consumer<Throwable> onError,Runnable finishCallBack) throws Exception;

    boolean checkNecessaryModuleConfigIsRight();


    default boolean validateConfig(Map<String, String> config) {
        int statusCode;
        try {
            HttpURLConnection connection = OpenAIUtil.getHttpURLConnection(config.get("url"), config.get("module"), config.get("apiKey"), "hi");
            statusCode = connection.getResponseCode();
//            System.out.println("HTTP Status Message: " + connection.getResponseMessage());
//            if (statusCode != 200){
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.out.println("Error Response: " + line);
//                    }
//                }
//            }else{
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.out.println("OutPut Response: " + line);
//                    }
//                }
//            }
        } catch (IOException e) {
            return false;
        }
        // 打印状态码
        //System.out.println("HTTP Status Code: " + statusCode);
        return statusCode == 200;
    }
}