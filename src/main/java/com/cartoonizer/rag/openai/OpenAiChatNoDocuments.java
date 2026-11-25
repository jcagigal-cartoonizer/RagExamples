package com.cartoonizer.rag.openai;

import static com.cartoonizer.rag.openai.OpenAiChatApp.SESSION_UUID;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import com.cartoonizer.rag.shared.Assistant;
import static com.cartoonizer.rag.utils.ApiKeys.CLAUDE_API_KEY;
import static com.cartoonizer.rag.utils.ApiKeys.OPENAI_API_KEY;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;

public class OpenAiChatNoDocuments implements AiChatInterface {

    public static void main(String[] args) {
//        OpenAiChatNoDocuments chat = new OpenAiChatNoDocuments(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI);
//        String answer = chat.askQuestion("Please, give an example of how to write an ACP-compatible AI Agent using langchain4j and Claude or OpenAI");
//        System.out.println("ANSWER = " + answer);
    }
    private String theApiKey;
    private String theModelName;
    public Assistant assistant;

    public OpenAiChatNoDocuments(String apiKey, String modelName) {
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        assistant = createAssistant();
        System.out.println("*** OpenAiChatNoDocuments theModelName " + theModelName); //  + " apiKey " + apiKey);
    }

    @Override
    public String askQuestion(String question) {
        String answer = "";
        ChatRequestParameters params = this.chatModel.defaultRequestParameters();
        UserMessage userMessage = UserMessage.from(question);
        
        store.messageList.add((ChatMessage)userMessage);
        store.updateMessages(SESSION_UUID, store.messageList);
        
        ChatRequest chatRequest = ChatRequest.builder()
                .parameters(params)
                .messages(
                        SystemMessage.from("You are an expert in creating Android apps with JetPack Compose. Answer the question to the best of your ability"),
                        userMessage
                )
                .build();
        ChatResponse response = this.chatModel.doChat(chatRequest);
        AiMessage aiMessage = response.aiMessage();
        
        store.messageList.add((ChatMessage)aiMessage);
        store.updateMessages(SESSION_UUID, store.messageList);
        
        answer = aiMessage.text();
        System.out.println("ANSWER = " + answer);
        return answer;
    }
    public ChatModel chatModel;
    protected PersistentStore store;
    protected ChatMemory chatMemory;

    private Assistant createAssistant() {
        if (OPENAI_API_KEY.equals(theApiKey)) {
            chatModel = OpenAiChatModel.builder()
                    .apiKey(theApiKey)
                    .temperature(0.0)
                    .logRequests(false)
                    .logResponses(false)
                    .modelName(theModelName)
                    .build();
        } else if (CLAUDE_API_KEY.equals(theApiKey)) {
            chatModel = AnthropicChatModel.builder()
                    .apiKey(theApiKey)
                    //                .temperature(0.0)
                    .logRequests(false)
                    .logResponses(false)
                    .modelName(theModelName)
                    .maxTokens(64000)
                    .build();
        } else {
            chatModel = OllamaChatModel.builder()
                    .temperature(0.0)
                    .logRequests(false)
                    .logResponses(false)
                    .modelName(theModelName)
                    .build();
        }

        store = new PersistentStore();
        chatMemory = MessageWindowChatMemory.builder()
            .id(SESSION_UUID)
            .maxMessages(20)
            .chatMemoryStore(store) // <--- Connect the DB here
            .build(); 
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory)
                .build();
    }

    @Override
    public List<ChatMessage> listHistory() {
        store.messageList = store.getMessages(SESSION_UUID);
        return store.messageList;
    }

    @Override
    public void init() {
    }

}
