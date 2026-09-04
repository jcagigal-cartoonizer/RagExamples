package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
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

public class SimpleExampleOpenAi {

    public static void main(String[] args) {
        String documentsPath = "./example-files";
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            setup(documentsPath);
//            String question = "Who is Juan Cagigal";
            String[] questions = {
//                "Who is Juan Cagigal",
//                "Give me some reasons tu use RAG software",
                "How to use RAG to migrate from Jetpack Views to Jetpack Compose with langchain4j ",
                "provide a **complete langchain4j sample project structure** for migration from Jetpack Views to Jetpack Compose with langchain4j",
            };
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
                System.out.println("QUESTION: " + question);
                answers[i] = askQuestion(question);
                System.out.println("ANSWER: " + answers[i]);
            }
            System.out.println("\n\n****************************************************************************************");
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

    public static void setup(String documentsPath) {

        // Load the document that includes the information you'd like to "chat" about with the model.
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentsPath, documentParser);

        // Split document into segments 100 tokens each
        DocumentSplitter splitter = DocumentSplitters.recursive(
                512,
                0,
                new OpenAiTokenCountEstimator(ModelNames.CHAT_GPT_MINI)
        );
        for (Document document : documents) {
            List<TextSegment> segments = splitter.split(document);
            for (TextSegment segment : segments) {
                System.out.println("Doc " + document.metadata() + " segment " + segment.text());
            }

            // Embed segments (convert them into vectors that represent the meaning) using embedding model        
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // Store embeddings into embedding store for further search / retrieval
            embeddingStore.addAll(embeddings, segments);

        }
    }

}
