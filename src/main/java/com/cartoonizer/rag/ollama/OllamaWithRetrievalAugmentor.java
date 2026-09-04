package com.cartoonizer.rag.ollama;

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
import java.util.List;

public class OllamaWithRetrievalAugmentor {

    static final String OLLAMA_IMAGE = "ollama/ollama:latest";
    static final String NOMIC_EMBED_MODEL = "nomic-embed-text";
    static final String DOCKER_NOMIC_EMBED_IMAGE_NAME = "tc-ollama/ollama:latest-nomic-embed-text";
    static final String TINY_DOLPHIN_MODEL = "tinydolphin";
    static final String DOCKER_TINY_DOLPHIN_IMAGE_NAME = "tc-ollama/ollama:latest-tinydolphin";

    public OllamaContainer ollama;
    public ChatModel ollamaModel;
    public IInfoRepo infoRepo;

//    public static void main(String[] args) {
//        // Example usage
//        String[] questions = {
////            "Give me information about Joan Cagigal",
////            "Who is Charlie",
//            "Tell me about the dummy planet",
//            "Is there life in the dummy planet",
////            "When was Joan Cagigal born",
//        };
//        String[] answers = new String[questions.length];
//        String model = NOMIC_EMBED_MODEL;
//        String imageName = DOCKER_NOMIC_EMBED_IMAGE_NAME;
//        OllamaWithRetrievalAugmentor usingOllama = new OllamaWithRetrievalAugmentor(model, imageName);
//        usingOllama.start();
//        usingOllama.chatSession(questions, answers);
//        usingOllama.stop();
//
//    }

    public String model;
    public String imageName;
    public OllamaWithRetrievalAugmentor(String model, String imageName) {
        this.model = model;
        this.imageName = imageName;
    }
    public void start() {
        startOllama();
        setupOllamaRAG(0.1);
    }
    public void stop() {
            ollama.stop();
    }
    public void chatSession(String[] questions, String[] answers) {
            for (int i = 0; i < questions.length; i++) {
                String question = questions[i];
                answers[i] = infoRepo.query(question);
                System.out.println("Answer = " + answers[i]);
            }
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
    public void setupOllamaRAG(double temperature) {
        System.out.println("Build the ChatModel");
        ollamaModel = OllamaChatModel.builder()
                .baseUrl(ollama.getEndpoint())
                .temperature(temperature)
                .logRequests(true)
                .logResponses(true)
                .modelName(model)
                .build();
        infoRepo = new SimpleInfoRepo(queryTransformer, ollamaModel);
        System.out.println("ChatModel built " + model);
    }

    public EmbeddingStore<TextSegment> embeddingStore;
    public EmbeddingModel embeddingModel;

}
