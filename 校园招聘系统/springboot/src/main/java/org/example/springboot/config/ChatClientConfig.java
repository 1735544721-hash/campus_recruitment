package org.example.springboot.config;

import io.micrometer.observation.ObservationRegistry;
import org.example.springboot.model.OllamaAlibabaChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;
import org.springframework.ai.model.ollama.autoconfigure.OllamaInitializationProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class ChatClientConfig {

    /**
     * 自定义兼容阿里巴巴模型
     * @param ollamaApi ollamaApi
     * @param properties properties
     * @param initProperties initProperties
     * @param toolCallingManager toolCallingManager
     * @param observationRegistry observationRegistry
     * @param observationConvention observationConvention
     * @param ollamaToolExecutionEligibilityPredicate ollamaToolExecutionEligibilityPredicate
     * @return OllamaAlibabaChatModel
     */
    @Bean
    public OllamaAlibabaChatModel ollamaAlibabaChatModel(OllamaApi ollamaApi, OllamaChatProperties properties, OllamaInitializationProperties initProperties, ToolCallingManager toolCallingManager, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention, ObjectProvider<ToolExecutionEligibilityPredicate> ollamaToolExecutionEligibilityPredicate) {
        PullModelStrategy chatModelPullStrategy = initProperties.getChat().isInclude() ? initProperties.getPullModelStrategy() : PullModelStrategy.NEVER;
        OllamaAlibabaChatModel chatModel = OllamaAlibabaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(properties.getOptions()).toolCallingManager(toolCallingManager).toolExecutionEligibilityPredicate((ToolExecutionEligibilityPredicate)ollamaToolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new)).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> {
            return ObservationRegistry.NOOP;
        })).modelManagementOptions(new ModelManagementOptions(chatModelPullStrategy, initProperties.getChat().getAdditionalModels(), initProperties.getTimeout(), initProperties.getMaxRetries())).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }



    @Bean("ollama")
    public ChatClient ollamaChatClient(OllamaAlibabaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .build();


    }

    @Bean("open-ai")
    //硅基流动
    public ChatClient openAIChatClient(OpenAiChatModel openAiChatModel){
        return ChatClient.builder(openAiChatModel).build();
    }
}
