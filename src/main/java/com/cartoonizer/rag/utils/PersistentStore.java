package com.cartoonizer.rag.utils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.util.ArrayList;
import java.util.List;

public class PersistentStore implements ChatMemoryStore {

    public List<ChatMessage> messageList = new ArrayList<>();
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: Load JSON from Database/Redis by memoryId
        System.out.println("PersistentStore getMessages = " + messageList.size());
        return messageList;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: Save List<ChatMessage> as JSON to Database/Redis
        System.out.println("PersistentStore updateMessages = " + messageList.size());
        messageList = messages;
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: Delete JSON from Database/Redis by memoryId
        System.out.println("PersistentStore deleteMessages before = " + messageList.size());
        messageList.clear();
        System.out.println("PersistentStore deleteMessages after = " + messageList.size());
    }
}