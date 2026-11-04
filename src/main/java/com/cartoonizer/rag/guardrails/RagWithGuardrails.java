package com.cartoonizer.rag.guardrails;

import static com.cartoonizer.rag.ollama.OllamaWithRetrievalAugmentor.DOCKER_IMAGE_NAME;
import static com.cartoonizer.rag.ollama.OllamaWithRetrievalAugmentor.OLLAMA_IMAGE;
import static com.cartoonizer.rag.ollama.OllamaWithRetrievalAugmentor.OLLAMA_MODEL;
import com.cartoonizer.rag.openai.AiChatInterface;
import static com.cartoonizer.rag.openai.OpenAiChatWithAugmentorGeneric.USE_ANTHROPIC;
import static com.cartoonizer.rag.openai.OpenAiChatWithAugmentorGeneric.USE_OLLAMA;
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

public class RagWithGuardrails implements AiChatInterface {

    public static void main(String[] args) throws Exception {
        runAll();
    }

    public static void runAll() {
        try {
            RagWithGuardrails guardrails = new RagWithGuardrails();
            guardrails.init();

//        String question = "Por favor, dame información sobre talleres de shodô en Haiku Barcelona.";
//        String question = "Por favor, dame información sobre talleres de pintura o caligrafia en Casa Asia. Solo dame infomacion oficial de Casa Asia, no de otros sitios.";
            String question = "Dame una lista de libros de autores japonesees. Incluye el precio y descripcion del libro";
            String answer = guardrails.askQuestion(question);
            save("\nQUESTION:\n" + question + "\nANSWER:\n" + answer);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("RagWithGuardrails EXCEPTION " + e);
        }
    }

    private static void save(String answer) {
        System.out.println(answer);
    }
    private SecureAssistant assistant;
    private ApprovedResourceRetriever retriever;
    private SecureWebFetcher webFetcher;
    private EmbeddingModel embeddingModel;
    private EnterpriseTools tools;
    private SecurityPolicy policy;
    private List listOfFolders = List.of(
            //                        Path.of("./output-files"),
            Path.of("./guardrails-files")
    );
    private List listOfUrls = List.of(
            "https://haikubarcelona.com/rest/catalogo_internal.php"
    );
    private OllamaContainer ollama;
    private ChatModel createChatModel() {
        ChatModel chatModel = null;
        if (USE_OLLAMA) {
            String endPoint = ollama.getEndpoint();
            chatModel = OllamaChatModel.builder()
                .baseUrl(endPoint)
                .modelName(OLLAMA_MODEL)
                .build();
            
        } else if (USE_ANTHROPIC) {
            chatModel = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.CLAUDE_API_KEY)
                    .modelName(ModelNames.CLAUDE_HAIKU)
                    .build();
        } else {
            chatModel = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(ModelNames.CHAT_GPT_MINI)
                    .build();
        }
        return chatModel;
    }
    public void startOllama() {
        System.out.println("Create and start the Ollama container");

        DockerImageName dockerImageName = DockerImageName.parse(OLLAMA_IMAGE);
        DockerClient dockerClient = DockerClientFactory.instance().client();
        List<Image> images = dockerClient.listImagesCmd().withReferenceFilter(DOCKER_IMAGE_NAME).exec();
        if (images.isEmpty()) {
            ollama = new OllamaContainer(dockerImageName);
        } else {
            ollama = new OllamaContainer(DockerImageName.parse(DOCKER_IMAGE_NAME).asCompatibleSubstituteFor(OLLAMA_IMAGE));
        }
        ollama.start();

        try {
            System.out.println("RagWithGuardrails Start pulling the '" + OLLAMA_MODEL + "' model ... would take several minutes ...");
            Container.ExecResult r = ollama.execInContainer("ollama", "pull", OLLAMA_MODEL);
            System.out.println("RagWithGuardrails Model pulling completed! {}" + r);
        } catch (Exception e) {
            throw new RuntimeException("RagWithGuardrails Error pulling model", e);
        }
        System.out.println("RagWithGuardrails commit to image " + DOCKER_IMAGE_NAME);
        ollama.commitToImage(DOCKER_IMAGE_NAME);
        
    }
    public void init() {
        if (USE_OLLAMA) {
            startOllama();
        }
        this.policy = new SecurityPolicy(
                listOfFolders,
                listOfUrls,
                List.of(
                        "haikubarcelona.com/admin"
                )
        );
        SecureDocumentLoader localLoader = new SecureDocumentLoader(policy);
        List<Document> documents = new ArrayList<>();
        try {
            documents = new ArrayList<>(localLoader.loadAllowedTextFiles());
        } catch (Exception e) {
        }
        // 3) Optionally fetch approved remote docs only
        webFetcher = new SecureWebFetcher(policy);

        embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        // 5) Build retriever and ingest approved docs
        retriever = new ApprovedResourceRetriever(embeddingModel);
        // Ingest files
        retriever.ingest(documents);

        // Ingest urls
        List<Document> allowedUrls = new ArrayList<>();
        for (String allowedRemoteHost : policy.allowedRemoteHosts()) {
            System.out.println("*** RagWithGuardrails ingesting " + allowedRemoteHost);
            final Document document = UrlDocumentLoader.load(allowedRemoteHost, new TextDocumentParser());
            Metadata meta = document.metadata();
            meta.put("url", allowedRemoteHost);
            allowedUrls.add(document);
        }
        retriever.ingest(allowedUrls);
        ChatModel chatModel = createChatModel();

        tools = new EnterpriseTools(policy, retriever, webFetcher);
        assistant = AiServices.builder(SecureAssistant.class)
                .chatModel(chatModel)
                .tools(tools)
                .build();

    }

    boolean allApproved(String question) {
        List<TextSegment> contextSegments = retriever.getTextSegments();
        System.out.println("*** RagWithGuardrails Check allApproved retrieve question " + question + " contextSegments = " + contextSegments.size());
        try {
            for (TextSegment contextSegment : contextSegments) {
                Metadata metadata = contextSegment.metadata();
                String source = metadata.getString("source");
                String url = metadata.getString("url");
                boolean approved = source == null || source.isBlank() || isSourceApproved(source);
//                System.out.println("*** approved = " + approved + " source " + source + " url = " + url);
//                if (url.endsWith(".php")) {
//                    System.out.println("-> " + contextSegment.text());
//                }
                if (!approved) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("ERROR allApproved " + e);
            return true;
        }
    }

    boolean isSourceApproved(String source) {
        boolean approved = false;
        for (Path allowedFolder : policy.allowedFolders()) {
            if (source.startsWith(allowedFolder.toAbsolutePath().toString())) {
                return true;
            }
        }
        for (String allowedRemoteHost : policy.allowedRemoteHosts()) {
            if (source.contains(allowedRemoteHost)) {
                return true;
            }
        }
        for (String blacklistedHost : policy.blacklistedRemoteHosts()) {
            if (source.contains(blacklistedHost)) {
                return false;
            }
        }
        return approved;
    }

    public String askQuestion(String question) {
        if (!allApproved(question)) {
            System.out.println("RagWithGuardrails askQuestion: Unapproved source detected in retrieval results.");
            return "Unapproved source detected in retrieval results.";
        }
        System.out.println("RagWithGuardrails askQuestion: All approved question = " + question);
        return assistant.answer(question);
    }

}
