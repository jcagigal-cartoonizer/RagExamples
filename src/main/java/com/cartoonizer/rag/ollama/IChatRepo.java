package com.cartoonizer.rag.ollama;

public interface IChatRepo {
  void chat(String message, String information, IClientChatResponse clientChatSession) throws Exception;
}