package com.cartoonizer.rag.openai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class OpenAiChatNoDocuments implements AiChatInterface {

    public static void main(String[] args) {
        // For migration to compose this costs 10 times more OpenAi credits
        OpenAiChatNoDocuments chat = new OpenAiChatNoDocuments(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI);
        chat.askQuestions();
    }
    private String theApiKey;
    private String theModelName;
    public Assistant assistant;

    public OpenAiChatNoDocuments(String apiKey, String modelName) {
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        assistant = createAssistant();
    }

    public String askQuestion(String question) {
        String answer = this.assistant.answer(question);
        return answer;
    }

    public void askQuestions() {
        String[] questions = this.getQuestions();
        String[] answers = new String[questions.length];
        StringBuilder theAnswer = new StringBuilder();
        for (int i = 0; i < questions.length; i++) {
//                System.out.println("\n\n****************************************************************************************");
            String question = questions[i];
//                System.out.println("QUESTION: " + question);
            answers[i] = askQuestion(question);
            theAnswer.append("\n// ANSWER\n");
            theAnswer.append(answers[i]);
            System.out.println("ANSWER: " + answers[i]);
            System.out.println("\n****************************************************************************************");
        }
    }

    protected String[] getQuestions() {
        String[] questions = {
            """
            Dame una lista de tiendas en Barcelona donde comprar figuras Daruma. Incluye la dirección y precios aproximados
            """
        };
        return questions;
    }

    private Assistant createAssistant() {
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .temperature(0.7)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Override
    public void init() {
    }

}
