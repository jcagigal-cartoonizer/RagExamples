package com.cartoonizer.rag.openai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;
import java.util.List;

public class MyChatModelListener implements ChatModelListener {

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        ChatModelListener.super.onError(errorContext);
        System.out.println("*** MyChatModelListener onError: " + errorContext.toString());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        System.out.println("*** MyChatModelListener onResponse");
        ChatResponse chatResponse = responseContext.chatResponse();

        AiMessage aiMessage = chatResponse.aiMessage();
//        System.out.println(aiMessage);

        ChatResponseMetadata metadata = chatResponse.metadata();
//        System.out.println(metadata.id());
//        System.out.println(metadata.modelName());
        System.out.println("    finishReason = " + metadata.finishReason());

        if (metadata instanceof OpenAiChatResponseMetadata openAiMetadata) {
//            System.out.println(openAiMetadata.created());
//            System.out.println(openAiMetadata.serviceTier());
//            System.out.println(openAiMetadata.systemFingerprint());
        }

        TokenUsage tokenUsage = metadata.tokenUsage();
        System.out.println("    inputTokenCount = " + tokenUsage.inputTokenCount());
        System.out.println("    outputTokenCount = " + tokenUsage.outputTokenCount());
        System.out.println("    totalTokenCount = " + tokenUsage.totalTokenCount());
        if (tokenUsage instanceof OpenAiTokenUsage openAiTokenUsage) {
            System.out.println("    input cachedTokens = " + openAiTokenUsage.inputTokensDetails().cachedTokens());
            System.out.println("    reasoningTokens = " + openAiTokenUsage.outputTokensDetails().reasoningTokens());
        }
    }

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        System.out.println("*** MyChatModelListener onRequest: ");
        ChatRequest chatRequest = requestContext.chatRequest();

        List<ChatMessage> messages = chatRequest.messages();
        System.out.println("    messages " + messages.size());

        ChatRequestParameters parameters = chatRequest.parameters();
//        System.out.println(parameters.modelName());
//        System.out.println(parameters.temperature());
//        System.out.println(parameters.topP());
//        System.out.println(parameters.topK());
//        System.out.println(parameters.frequencyPenalty());
//        System.out.println(parameters.presencePenalty());
        System.out.println("    maxOutputTokens = " + parameters.maxOutputTokens());
//        System.out.println(parameters.stopSequences());
//        System.out.println(parameters.toolSpecifications());
//        System.out.println(parameters.toolChoice());
//        System.out.println(parameters.responseFormat());

        if (parameters instanceof OpenAiChatRequestParameters openAiParameters) {
//            System.out.println(openAiParameters.maxCompletionTokens());
//            System.out.println(openAiParameters.logitBias());
//            System.out.println(openAiParameters.parallelToolCalls());
//            System.out.println(openAiParameters.seed());
//            System.out.println(openAiParameters.user());
//            System.out.println(openAiParameters.store());
//            System.out.println(openAiParameters.metadata());
//            System.out.println(openAiParameters.serviceTier());
//            System.out.println(openAiParameters.reasoningEffort());
        }

//        System.out.println(requestContext.modelProvider());
    }
    
}
