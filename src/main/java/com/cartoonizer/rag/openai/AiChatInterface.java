package com.cartoonizer.rag.openai;

import dev.langchain4j.data.message.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public interface AiChatInterface {
    String askQuestion(String question);
    void init();
    default List<ChatMessage> listHistory() {
        return new ArrayList<>();
    }
}
