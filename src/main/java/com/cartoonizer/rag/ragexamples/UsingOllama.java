
package com.cartoonizer.rag.ragexamples;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;

public class UsingOllama {

  static final String OLLAMA_IMAGE = "ollama/ollama:latest";
  static final String TINY_DOLPHIN_MODEL = "tinydolphin";
  static final String DOCKER_IMAGE_NAME = "tc-ollama/ollama:latest-tinydolphin";

  public static void main(String[] args) {
    // Create and start the Ollama container
    DockerImageName dockerImageName = DockerImageName.parse(OLLAMA_IMAGE);
    DockerClient dockerClient = DockerClientFactory.instance().client();
    List<Image> images = dockerClient.listImagesCmd().withReferenceFilter(DOCKER_IMAGE_NAME).exec();
    OllamaContainer ollama;
    if (images.isEmpty()) {
        ollama = new OllamaContainer(dockerImageName);
    } else {
        ollama = new OllamaContainer(DockerImageName.parse(DOCKER_IMAGE_NAME).asCompatibleSubstituteFor(OLLAMA_IMAGE));
    }
    ollama.start();

    // Pull the model and create an image based on the selected model.
    try {
//        log.info("Start pulling the '{}' model ... would take several minutes ...", TINY_DOLPHIN_MODEL);
        Container.ExecResult r = ollama.execInContainer("ollama", "pull", TINY_DOLPHIN_MODEL);
//        log.info("Model pulling competed! {}", r);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException("Error pulling model", e);
    }
    ollama.commitToImage(DOCKER_IMAGE_NAME);

    // Build the ChatModel
    ChatModel model = OllamaChatModel.builder()
            .baseUrl(ollama.getEndpoint())
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .modelName(TINY_DOLPHIN_MODEL)
            .build();

    // Example usage
    String answer = model.chat("Provide 3 short bullet points explaining why Java is awesome");
    System.out.println("Answer = " + answer);

    // Stop the Ollama container
    ollama.stop();
  }
    
}
