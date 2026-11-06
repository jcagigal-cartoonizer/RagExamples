package com.cartoonizer.rag.ollama;

import com.cartoonizer.rag.openai.AiChatInterface;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OllamaWithRetrievalAugmentor implements AiChatInterface {

    public static final String OLLAMA_MODEL = "nomic-embed-text";
    public static final String OLLAMA_IMAGE = "ollama/ollama:latest";
    public static final String DOCKER_IMAGE_NAME = "tc-ollama/ollama:latest-nomic-embed-text";

    public OllamaContainer ollama;
    public ChatModel ollamaModel;
    public IInfoRepo infoRepo;

    public static List folders = List.of(
            //                        Path.of("./files/output-files"),
            Path.of("./files/guardrails-files")
    );
    public static List urls = List.of(
            "https://haikubarcelona.com/rest/catalogo_internal.php"
    );
    public static void main(String[] args) {
        // Example usage
        String[] questions = {
            "Dame una lista de libros de Murakami con su descripcion y precio"
        };
        String[] answers = new String[questions.length];
        String model = OLLAMA_MODEL;
        String imageName = DOCKER_IMAGE_NAME;
        OllamaWithRetrievalAugmentor usingOllama = new OllamaWithRetrievalAugmentor(model, imageName, folders, urls);
        usingOllama.start();
        usingOllama.chatSession(questions, answers);
        usingOllama.stop();

    }

    public String model;
    public String imageName;
    private List listOfFolders;
    private List listOfUrls;
    public OllamaWithRetrievalAugmentor(String model, String imageName, List listOfFolders, List listOfUrls) {
        this.model = model;
        this.imageName = imageName;
        this.listOfFolders = listOfFolders;
        this.listOfUrls = listOfUrls;
    }
    public void start() {
        init();
    }
    @Override
    public void init() {
        startOllama();
        setupOllamaRAG(0.7, listOfFolders, listOfUrls);
    }
    public void stop() {
            ollama.stop();
    }
    public void chatSession(String[] questions, String[] answers) {
            for (int i = 0; i < questions.length; i++) {
                String question = questions[i];
                answers[i] = askQuestion(question);
                System.out.println("Answer = " + answers[i]);
            }
    }
    @Override
    public String askQuestion(String question) {
        return infoRepo.query(question);
    }
    public void startOllama() {
        System.out.println("Create and start the Ollama container");

        DockerImageName dockerImageName = DockerImageName.parse(OLLAMA_IMAGE);
        DockerClient dockerClient = DockerClientFactory.instance().client();
        List<Image> images = dockerClient.listImagesCmd().withReferenceFilter(imageName).exec();
        if (images.isEmpty()) {
            ollama = new OllamaContainer(dockerImageName);
        } else {
            ollama = new OllamaContainer(DockerImageName.parse(imageName).asCompatibleSubstituteFor(OLLAMA_IMAGE));
        }
        ollama.start();

        try {
            System.out.println("Start pulling the '" + model + "' model ... would take several minutes ...");
            Container.ExecResult r = ollama.execInContainer("ollama", "pull", model);
            System.out.println("Model pulling completed! {}" + r);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error pulling model", e);
        }
        System.out.println("commit to image " + imageName);
        ollama.commitToImage(imageName);
        
    }
    public QueryTransformer queryTransformer;
    public void setupOllamaRAG(double temperature, List listOfFolders, List listOfUrls) {
        System.out.println("setupOllamaRAG Build the ChatModel");
        ollamaModel = OllamaChatModel.builder()
                .baseUrl(ollama.getEndpoint())
                .temperature(temperature)
                .logRequests(false)
                .logResponses(false)
                .modelName(model)
                .build();
        infoRepo = new SimpleInfoRepo(queryTransformer, ollamaModel, listOfFolders, listOfUrls);
        System.out.println("ChatModel built " + model);
    }

    public EmbeddingStore<TextSegment> embeddingStore;
    public EmbeddingModel embeddingModel;

}
