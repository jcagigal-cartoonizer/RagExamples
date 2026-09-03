package com.cartoonizer.rag.ragexamples;



import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import java.io.File;
import static java.util.stream.Collectors.joining;

public class SimpleExampleRag {

    public static void main(String[] args) {
        String documentToRead = "./example-files/story-about-loan-cagigal.txt";
        File f = new File(documentToRead);
        if (f.exists()) {
            System.out.println("Before setup ");
            setup(documentToRead);
            String question = "Who is Juan Cagigal";
            System.out.println("After setup question: " + question);
            String answer = askQuestion(question);
            System.out.println("Answer: " + answer);
        }
    }
    public static String askQuestion(String question) {

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant embeddings in embedding store by semantic similarity
        // You can play with parameters below to find a sweet spot for your specific use case
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(3)
                .minScore(0.7)
                .build();
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore.search(embeddingSearchRequest).matches();

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

        // Send the prompt to the OpenAI chat model
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(ModelNames.CHAT_GPT_MINI)
                .timeout(Duration.ofSeconds(60))
                .build();
        AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();

        // See an answer from the model
        String answer = aiMessage.text();
        return answer;
    }
    public static DocumentParser documentParser = new TextDocumentParser();
    public static EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
    public static EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    public static void setup(String documentToRead) {
        
        // Load the document that includes the information you'd like to "chat" about with the model.
        Document document = loadDocument(documentToRead, documentParser);

        // Split document into segments 100 tokens each
        DocumentSplitter splitter = DocumentSplitters.recursive(
                512,
                0,
                new OpenAiTokenCountEstimator(ModelNames.CHAT_GPT_MINI)
        );
        List<TextSegment> segments = splitter.split(document);
        for (TextSegment segment : segments) {
            System.out.println("segment " + segment.text());
        }

        // Embed segments (convert them into vectors that represent the meaning) using embedding model        
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // Store embeddings into embedding store for further search / retrieval
        embeddingStore.addAll(embeddings, segments);

    }
    
}
