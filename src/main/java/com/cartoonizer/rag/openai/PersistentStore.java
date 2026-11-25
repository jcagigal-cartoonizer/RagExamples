package com.cartoonizer.rag.openai;

import com.cartoonizer.utils.Utils;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.util.ArrayList;
import java.util.List;

public class PersistentStore implements ChatMemoryStore {

    public List<ChatMessage> messageList = new ArrayList<>();
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: Load JSON from Database/Redis by memoryId
        String savedMessages = loadMessagesJson();
        messageList = ChatMessageDeserializer.messagesFromJson(savedMessages);
        System.out.println("PersistentStore getMessages = " + messageList.size());
        return messageList;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: Save List<ChatMessage> as JSON to Database/Redis
        String savedMessages = ChatMessageSerializer.messagesToJson(messageList);
        saveMessagesJson(savedMessages);
        System.out.println("PersistentStore updateMessages = " + messageList.size());
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: Delete JSON from Database/Redis by memoryId
        System.out.println("PersistentStore deleteMessages before = " + messageList.size());
        saveMessagesJson("[]");
        messageList.clear();
        System.out.println("PersistentStore deleteMessages after = " + messageList.size());
    }
    public static String PATH_MESSAGES = "./history.json";
    private String loadMessagesJson() {
        String savedMessages = Utils.readFullFile(PATH_MESSAGES);
        System.out.println("PersistentStore loadMessagesJson = \n" + savedMessages);
        return savedMessages;
    }

    private void saveMessagesJson(String savedMessages) {
        System.out.println("PersistentStore saveMessagesJson = \n" + savedMessages);
        Utils.saveFile(PATH_MESSAGES, savedMessages);
    }
}