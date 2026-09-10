
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import java.util.List;
import static java.util.stream.Collectors.joining;

public class OpenAiChatWithAugmentor {
    public static void main(String[] args) {
        String documentsPath = "./example-files";
        String answerPath = null;
        String processedAnswerPath = null;
        OpenAiChatWithAugmentor chat = new OpenAiChatWithAugmentor(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
        Assistant assistant = chat.createAssistant();

        String questions[] = {
            """
            Provide a full Compose recreation of the entire fragment_home.xml file and the HomeFragment.kt file, keeping all the imports and components as is and using jetpack views navigation instead of Compose navigation
            """
        };
        String answers[] = new String[questions.length];
        for (int i = 0; i < questions.length; i++) {
            answers[i] = assistant.answer(questions[i]);            
        }
        for (int i = 0; i < questions.length; i++) {
            System.out.println("\n************************************************************************************");
//            System.out.println("QUESTION: " + questions[i] + " is:");        
            System.out.println("ANSWER: " + answers[i]);        
        }
        System.out.println("\n==========================================================================================");
    }
    
    public String theFilesPath;
    private String theApiKey;
    private String theModelName;
    public String answerPath;
    public String processedAnswerPath;
    private OpenAiChatWithAugmentor() {
        
    }
    public OpenAiChatWithAugmentor(String apiKey, String modelName, String documentsPath, String answerPath, String processedAnswerPath) {
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        this.theFilesPath = documentsPath;
        this.answerPath = answerPath;
        this.processedAnswerPath = processedAnswerPath;
    }
    public Assistant createAssistant() {

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(theFilesPath, new TextDocumentParser());

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(1024, 0, new OpenAiTokenCountEstimator(theModelName));
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .temperature(0.2)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .build();

        // We will create a CompressingQueryTransformer, which is responsible for compressing
        // the user's query and the preceding conversation into a single, stand-alone query.
        // This should significantly improve the quality of the retrieval process.
        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
        // It can be configured to customize the RAG behavior according to your requirements.
        // In subsequent examples, we will explore more customizations.
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    
}
