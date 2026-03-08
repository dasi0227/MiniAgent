package com.dasi.config;

import com.dasi.properties.SystemModelProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemModelProperties.class)
public class SystemModelConfig {

    @Bean(name = "SystemModel")
    public OpenAiChatModel systemModel(SystemModelProperties systemModelProperties) {
        String baseUrl = systemModelProperties.getBaseUrl();
        String apiKey = systemModelProperties.getApiKey();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath("v1/chat/completions")
                .build();

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(systemModelProperties.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
    }

}
