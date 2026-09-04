package com.cartoonizer.rag.ollama;

public class OllamaChatService {
  private final IChatRepo chatRepo;
  private final IInfoRepo infoDataRepo;

  public OllamaChatService(IChatRepo chatRepo, IInfoRepo infoDataRepo) {
    this.chatRepo = chatRepo;
    this.infoDataRepo = infoDataRepo;
  }

  public void sendMessage(String message, IClientChatResponse chatResponse) {
    try {
      String info = infoDataRepo.query(message);
      chatRepo.chat(message, info, chatResponse);
    } catch (Exception e) {
      throw new RuntimeException("An error occurred while processing the message " + message + ": " + e);
    }
  }
}