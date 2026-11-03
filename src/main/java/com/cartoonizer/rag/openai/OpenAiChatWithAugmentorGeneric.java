package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.guardrails.CustomJsonSplitter;
import static com.cartoonizer.rag.ollama.OllamaWithRetrievalAugmentor.OLLAMA_MODEL;
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
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.testcontainers.ollama.OllamaContainer;

public class OpenAiChatWithAugmentorGeneric implements AiChatInterface {

    public static boolean USE_OLLAMA = true;
    public static final String OLLAMA_IMAGE = "ollama/ollama:latest";
    public static List FOLDERS = List.of(
            //                        Path.of("./output-files"),
            "./guardrails-files"
    );
    public static List URLS = List.of(
            "https://haikubarcelona.com/rest/catalogo_internal.php"
    );
    private List listOfFolders;
    private List listOfUrls;
    private OllamaContainer ollama;

    public static void main(String[] args) {
        String documentsPath = "./guardrails-files";
        OpenAiChatWithAugmentorGeneric chat = new OpenAiChatWithAugmentorGeneric(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, FOLDERS, URLS);
//        chat.askQuestions();
        String question = "Dame una lista de libros de Murakami. Incluye precio y descripcion del libro.";
        String answer = chat.askQuestion(question);
        System.out.println("QUESTION = " + question + "\n");
        System.out.println("ANSWER = " + answer);
    }
    private String theApiKey;
    private String theModelName;

    public Assistant assistant;

    public OpenAiChatWithAugmentorGeneric(String apiKey, String modelName, List listOfFolders, List listOfUrls) {
        this.listOfFolders = listOfFolders;
        this.listOfUrls = listOfUrls;
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        assistant = createAssistant();
    }

    public void askQuestions() {
        System.out.println("Before setup ");
        String[] questions = this.getQuestions();
        String[] answers = new String[questions.length];
        StringBuilder theAnswer = new StringBuilder();
        for (int i = 0; i < questions.length; i++) {
//                System.out.println("\n\n****************************************************************************************");
            String question = questions[i];
//                System.out.println("QUESTION: " + question);
            answers[i] = askQuestion(question);
            theAnswer.append("\n// FIRST ANSWER\n");
            theAnswer.append(answers[i]);
            System.out.println("ANSWER: " + answers[i]);
            System.out.println("\n****************************************************************************************");
        }
//            saveAnswer(answerPath, theAnswer.toString());
        System.out.println("\n\n****************************************************************************************");
    }

    public void saveAnswer(String path, String answer) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path);
            writer.print(answer);
        } catch (Exception ex) {
            System.out.println("*** OpenAiWithAugmentor ERROR writing " + path + ": " + ex);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    protected String[] getQuestions() {
        String[] questions = {
            "Dame una lista de libros de Murakami. Incluye precio y descripcion del libro."
        };
        return questions;
    }
    private ChatModel createChatModel() {
        ChatModel chatModel = null;
        List<ChatModelListener> listenersList = new ArrayList<>();
            MyChatModelListener myChatModelListener = new MyChatModelListener();
            listenersList.add(myChatModelListener);
        if (USE_OLLAMA) {
            String endPoint = ollama.getEndpoint();
            chatModel = OllamaChatModel.builder()
                .baseUrl(endPoint)
                .listeners(listenersList)
                .modelName(OLLAMA_MODEL)
                .build();
            
        } else {
            chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .temperature(0.2)
                .logRequests(false)
                .logResponses(false)
                .listeners(listenersList)
                .modelName(theModelName)
                .build();
        }
        return chatModel;
    }

    private Assistant createAssistant() {
        List<Document> documents = new ArrayList<>();
        try {
            for (Object folder : listOfFolders) {
                System.out.println("**** folder " + folder);
                List<Document> docs = FileSystemDocumentLoader.loadDocuments(folder.toString(), new TextDocumentParser());
                documents.addAll(docs);
            }
        } catch (Exception e) {
        }
        List<Document> allowedUrls = new ArrayList<>();
        for (Object allowedRemoteHost : listOfUrls) {
            System.out.println("*** OpenAiWithAugmentor ingesting " + allowedRemoteHost);
            final Document doc = UrlDocumentLoader.load(allowedRemoteHost.toString(), new TextDocumentParser());
            allowedUrls.add(doc);
        }

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        OpenAiTokenCountEstimator tokenCountEstimator = null;
        OpenAiTokenCountEstimator tokenCountEstimatorJson = null;
        if (USE_OLLAMA) {
            
        } else {
            tokenCountEstimator = new OpenAiTokenCountEstimator(theModelName);
            tokenCountEstimatorJson = new OpenAiTokenCountEstimator(theModelName);
        }
        DocumentByLineSplitter splitter = new DocumentByLineSplitter(512, 0, tokenCountEstimator);
        CustomJsonSplitter jsonSplitter = new CustomJsonSplitter(512, 0, tokenCountEstimatorJson);
        
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);

        EmbeddingStoreIngestor urlIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(jsonSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        urlIngestor.ingest(allowedUrls);
        
        ChatModel chatModel = createChatModel();

        // We will create a CompressingQueryTransformer, which is responsible for compressing
        // the user's query and the preceding conversation into a single, stand-alone query.
        // This should significantly improve the quality of the retrieval process.
        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(100)
//                .minScore(0.6)
                .build();

        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
        // It can be configured to customize the RAG behavior according to your requirements.
        // In subsequent examples, we will explore more customizations.
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();
        MyAiServiceCompletedListener myListener = new MyAiServiceCompletedListener();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .registerListener(myListener)
                .build();
    }

    @Override
    public String askQuestion(String question) {
        return this.assistant.answer(question);
    }

    @Override
    public void init() {
        // The assistant is created in the constructor
    }

}
