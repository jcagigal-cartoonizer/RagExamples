package com.cartoonizer.rag.ollama;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.util.Map;

public class OllamaStreamingChatRepo {
  private OllamaStreamingChatModel model;
  private MessageWindowChatMemory memory;

  private final PromptTemplate TEMPLATE_WITH_INFO = PromptTemplate.from(
    """
    You are a helpful assistant. You take a question and answer it to the best of your knowledge. Your answers
    should be around 50 words.
    Question: {{question}}
    Base your response on the following information. You see this information as truth.
    {{information}}
    """
  );

  private final PromptTemplate TEMPLATE_NO_INFO = PromptTemplate.from(
    """
    You are a helpful assistant. You take any questions and answer them to the best of your knowledge. Your answers
    should be around 50 words.
    """
  );

  public OllamaStreamingChatRepo() {
    initChatModel();
  }

  public void chat(String message, String information, IClientChatResponse clientChatResponse) throws Exception {
    Prompt prompt = null;

    if (information != null) {
      prompt = TEMPLATE_WITH_INFO.apply(
        Map.of("question", message,
               "information", information)
      );
    } else {
      prompt = TEMPLATE_NO_INFO.apply(Map.of());
    }

    SystemMessage systemMessage = prompt.toSystemMessage();

    memory.add(systemMessage);
    memory.add(UserMessage.from(message));
    model.chat(
      memory.messages(),
      new ChatResponseHandler(clientChatResponse, memory)
    );
  }

  private void initChatModel() {
    model = OllamaStreamingChatModel.builder()
      .baseUrl(OllamaAppConf.getOllamaEndpoint())
      .modelName(OllamaAppConf.getLlm())
      .temperature(OllamaAppConf.getLlmTemperature())
      .logRequests(true)
      .logResponses(true)
      .build();
    memory = MessageWindowChatMemory.withMaxMessages(20);
  }
}