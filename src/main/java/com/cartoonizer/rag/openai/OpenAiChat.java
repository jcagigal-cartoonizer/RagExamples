package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
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
import static java.util.stream.Collectors.joining;

public class OpenAiChat {
    public int maxResults;
    public String askQuestion(String question, int maxResults, double minScore) {

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant embeddings in embedding store by semantic similarity
        // You can play with parameters below to find a sweet spot for your specific use case
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
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
                .apiKey(theApiKey)
                .modelName(theModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();

        // See an answer from the model
        String answer = aiMessage.text();
        return answer;
    }
    public DocumentParser documentParser = new TextDocumentParser();
    public EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
    public EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    public List<Document> documents;
    public DocumentSplitter splitter;
    public String theApiKey = ApiKeys.OPENAI_API_KEY;
    public String theModelName = ModelNames.CHAT_GPT_MINI;
    public String theFilesPath;
    private OpenAiChat() {
        
    }
    public OpenAiChat(String apiKey, String modelName, String documentsPath) {
        theFilesPath = documentsPath;
        theApiKey = apiKey;
        theModelName = modelName;
    }
    protected DocumentSplitter createSplitter() {
        // Split document into segments 100 tokens each
        DocumentSplitter splitter = new DocumentByParagraphSplitter(
                512,
                0,
                new OpenAiTokenCountEstimator(theModelName)
        );
        return splitter;
    }
    protected void processTextSegments(Document document, List<TextSegment> segments) {
            for (TextSegment segment : segments) {
                System.out.println("Doc metadata " + document.metadata());
                System.out.println("segment metadata " + segment.metadata() + " text " + segment.text());
            }

            // Embed segments (convert them into vectors that represent the meaning) using embedding model        
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // Store embeddings into embedding store for further search / retrieval
            embeddingStore.addAll(embeddings, segments);

    }
    public void setup() {
        // Load the document that includes the information you'd like to "chat" about with the model.
        documents = FileSystemDocumentLoader.loadDocuments(theFilesPath, documentParser);
        splitter = createSplitter();
        for (Document document : documents) {
            processTextSegments(document, splitter.split(document));
        }
    }


    
}
