package com.cartoonizer.rag.migration.prompters;

import com.cartoonizer.rag.answers.GeneralAnswerProcessor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import java.io.PrintWriter;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.joining;

public class PrompterViewToCompose {
    public ChatModel chatModel;
    public List<EmbeddingMatch<TextSegment>> relevantEmbeddings;
    public List<Document> documents;
    public String answerPath;
    public String processedAnswerPath;
    public boolean save;
    
    private PrompterViewToCompose() {
        
    }
    public PrompterViewToCompose(List<EmbeddingMatch<TextSegment>> relevantEmbeddings,ChatModel chatModel, List<Document> documents, String answerPath, String processedAnswerPath) {
        this(relevantEmbeddings, chatModel, documents, answerPath, processedAnswerPath, true);
    }
    public PrompterViewToCompose(List<EmbeddingMatch<TextSegment>> relevantEmbeddings,ChatModel chatModel, List<Document> documents, String answerPath, String processedAnswerPath, boolean save) {
        this.relevantEmbeddings = relevantEmbeddings;
        this.chatModel = chatModel;
        this.documents = documents;
        this.answerPath = answerPath;
        this.processedAnswerPath = processedAnswerPath;
        this.save = save;
    }
    public String getPrompt(String question) {
        // Create a prompt for the model that includes question and relevant embeddings
        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                + "\n"
                + "Question:\n"
                + "{{question}}\n"
                + "\n"
                + "Base your answer on the following information:\n"
                + "{{information}}");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(joining("\n\n"));
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);
        AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();
        String answer = aiMessage.text();
        if (save) {
            saveAnswer(answerPath, answer);
            if (answerPath != null && !answerPath.isEmpty() && processedAnswerPath != null && !processedAnswerPath.isEmpty()) {
                GeneralAnswerProcessor reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
                reader.load();
            }
        } else {
            System.out.println("ANSWER:");
            System.out.println(answer);
        }
        return answer;
    }
    public void saveAnswer(String path, String answer) {
        if (path == null || path.isEmpty()) {
            System.out.println("ANSWER:");
            System.out.println(answer);
        } else {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(path);
                writer.print(answer);
            } catch (Exception ex) {
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
        
    }
}
