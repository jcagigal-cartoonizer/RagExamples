package com.cartoonizer.rag.ollama;

import java.net.URI;

public class OllamaAppConf {

    public static String getDocumentsPath() {
        return "./example-files";
    }

    public static String getOllamaEndpoint() {
        return "http://localhost:11434";
    }

    public static String getEmbeddingModel() {
        return "nomic-embed-text:latest";
    }

    public static String getLlm() {
        return "llama3.1:8b";
    }

    public static double getLlmTemperature() {
        return 0.2;
    }
}
