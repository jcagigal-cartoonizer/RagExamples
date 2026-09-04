package com.cartoonizer.rag.ollama;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public class ChatResponseHandler implements StreamingChatResponseHandler {
  private final IClientChatResponse clientChatResponse;
  private final ChatMemory memory;

  public ChatResponseHandler(IClientChatResponse clientChatResponse, ChatMemory memory) {
    this.clientChatResponse = clientChatResponse;
    this.memory = memory;
  }

  @Override
  public void onPartialResponse(String s) {
    clientChatResponse.send(s);
  }

  @Override
  public void onCompleteResponse(ChatResponse chatResponse) {
    memory.add(chatResponse.aiMessage());
    this.clientChatResponse.send("~done~");
  }

  @Override
  public void onError(Throwable throwable) {
    this.clientChatResponse.send("~error~");
  }
}
