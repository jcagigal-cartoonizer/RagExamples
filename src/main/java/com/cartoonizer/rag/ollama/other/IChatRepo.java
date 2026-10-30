package com.cartoonizer.rag.ollama.other;

public interface IChatRepo {
  void chat(String message, String information, IClientChatResponse clientChatSession) throws Exception;
}