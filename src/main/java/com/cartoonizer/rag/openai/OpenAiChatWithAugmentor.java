
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
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ANSWER_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PROCESSED_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.SUFFIX;
import com.cartoonizer.rag.utils.ModelNames;
import com.cartoonizer.rag.utils.Utils;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import java.io.File;
import java.util.List;

public class OpenAiChatWithAugmentor {
    public static void main(String[] args) {
        String documentsPath = "./example-files";
        PREFIX = "Dashboard";
        LAYOUT = "fragment_dashboard.xml";
        OpenAiChatWithAugmentor chat = new OpenAiChatWithAugmentor(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath);
        chat.askQuestions();
    }
    public String documentsPath;
    private String theApiKey;
    private String theModelName;

    public String savedViewModel;
    public String savedLayout;
    public String savedViewClass;
    public String pathForViewClass; // the Jetpack Views class
    public String pathViewModel; // the Jetpack Views
    public String fileToGenerate;
    public Assistant assistant;
    public OpenAiChatWithAugmentor(String apiKey, String modelName, String documentsPath) {
        this.theApiKey = apiKey;
        this.theModelName = modelName;
        this.documentsPath = documentsPath;
        fileToGenerate = PREFIX + SUFFIX;
        pathForViewClass = documentsPath  + "/" + fileToGenerate + ".kt";
        pathViewModel = documentsPath  + "/" + PREFIX + "ViewModel.kt";
        
        String pathLayout = documentsPath  + "/" + LAYOUT;
        
        savedViewClass = Utils.readFullFile(pathForViewClass);
        savedViewModel = Utils.readFullFile(pathViewModel);
        savedLayout = Utils.readFullFile(pathLayout);
        assistant = createAssistant();
    }
    public void askQuestions() {
        String answerPath = ANSWER_FOLDER_WITH_ASSISTANT + "/" + fileToGenerate + ".txt";
        String processedAnswerPath = PROCESSED_FOLDER_WITH_ASSISTANT + "/processed-" + fileToGenerate + ".txt";
        
        String pathLayout = documentsPath  + "/" + LAYOUT;
        
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChatWithAugmentor chat = new OpenAiChatWithAugmentor(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath);
            String[] questions = chat.getQuestions();
//            - provide Jetpack Compose Modifier extensions that implement the properties of the android xml styles file styles.xml
//            - provide a jetpack compose composable function that implements the following layout xml file used in Jetpack Views using those Compose Theme and Modifiers
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
//                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
//                System.out.println("QUESTION: " + question);
                answers[i] = chat.assistant.answer(question);
                System.out.println("ANSWER: " + answers[i]);
                String secondQuestion = 
                    """
                    Now do a second pass and provide:
                    1. a **more exact `HomeButtonsState` reducer** that mirrors every `collect {}` branch from the fragment one-by-one, and  
                    2. a **full `MainActivityViewModel` Compose bridge** so your shared flows like `locationEnabledFlow`, `roofLightFlow`, `zoneFlow`, `shortBreakStatus`, etc. are integrated into the Compose screen exactly like the fragment did.
                    """; 
                String secondAnswer = chat.assistant.answer(secondQuestion);
                System.out.println("\n****************************************************************************************");
                System.out.println("2nd. ANSWER: " + secondAnswer);
            }
            System.out.println("\n\n****************************************************************************************");
        }
    }
    protected String[] getQuestions() {
            String[] questions = {
            "1. provide a jetpack Compose composable that implements the following jetpack views class \n" +
            savedViewClass +
            "2. provide a Compose viewModel to be used by the composable based on the following Jetpack Views viewModel, exposing a Compose-friendly `UiState + UiEvent` architecture \n" + 
            savedViewModel +
            "3. When implementing the composable and viewModel preserve Jetpack Views navigation  \n" +
            "4. Use dialog state, lifecycle collection of state/events for dialog handling \n" +
            "5. Use state holders/data classes to fully replace the fragment button logic. \n" +
            "6. provide a full `" + PREFIX + "ButtonsState` with exact button coloring/visibility matching the XML behavior and using `SharedFlow<" + PREFIX + "UiEffect>` instead of multiple event types\n" +
            "7. Provide a compose CustomDialog implementation based on the custom_dialog.xml file and the dialog implemented in the CustomDialog.kt file \n" +
            "8. Provide also a `" + PREFIX + "ButtonsState` with exact Compose button styling helpers matching the custom button component more closely \n" +
            "Use only android and jetpack compose references\n"

            };
            return questions;
    }
    public Assistant createAssistant() {

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentsPath, new TextDocumentParser());

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
