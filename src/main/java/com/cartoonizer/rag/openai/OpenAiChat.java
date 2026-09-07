package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.migration.prompters.PrompterViewToCompose;
import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
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

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .modelName(theModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        PrompterViewToCompose prompter = new PrompterViewToCompose(relevantEmbeddings, chatModel, documents, answerPath, processedAnswerPath);
        // Send the prompt to the OpenAI chat model and get the answer
        String answer = prompter.getPrompt(question);
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
    public String answerPath;
    public String processedAnswerPath;

    private OpenAiChat() {

    }

    public OpenAiChat(String apiKey, String modelName, String documentsPath, String answerPath, String processedAnswerPath) {
        theFilesPath = documentsPath;
        theApiKey = apiKey;
        theModelName = modelName;
        this.answerPath = answerPath;
        this.processedAnswerPath = processedAnswerPath;
    }

    protected DocumentSplitter createSplitter() {
//        DocumentByLineSplitter subSplitter = new DocumentByLineSplitter(
//                1024,
//                1024,
//                new OpenAiTokenCountEstimator(theModelName)
//        );
//        DocumentByLineSplitter splitter = new DocumentByLineSplitter(
//                1024,
//                1024,
//                new OpenAiTokenCountEstimator(theModelName),
//                subSplitter
//        );
        DocumentSplitter splitter = new DocumentByParagraphSplitter(
                512,
                0,
                new OpenAiTokenCountEstimator(theModelName)
        );
        return splitter;
    }

    protected void processTextSegments(Document document, List<TextSegment> segments) {
        Metadata docMetadata = document.metadata();
        String docFileName = docMetadata.getString("file_name");
        String docPath = docMetadata.getString("absolute_directory_path") + "/" + docFileName;
        System.out.println("========================================================================");
        System.out.println("Doc metadata " + docPath + " " + docMetadata);
        for (TextSegment segment : segments) {
            System.out.println("    segment metadata " + segment.metadata() + " text " + segment.text());
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
