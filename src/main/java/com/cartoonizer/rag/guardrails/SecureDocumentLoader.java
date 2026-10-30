package com.cartoonizer.rag.guardrails;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SecureDocumentLoader {

    private final SecurityPolicy securityPolicy;
    private final DocumentParser parser = new TextDocumentParser();

    public SecureDocumentLoader(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public List<Document> loadAllowedTextFiles() throws IOException {
        List<Document> documents = new ArrayList<>();

        for (Path folder : securityPolicy.allowedFolders()) {
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                continue;
            }

            try (var stream = Files.walk(folder)) {
                stream.filter(Files::isRegularFile)
                      .filter(this::isAllowedFileType)
                      .forEach(path -> {
                          if (!securityPolicy.isAllowedLocalPath(path)) {
                              return;
                          }
                          try {
                              // Load only plain text-like files in this example
                              String content = Files.readString(path);
                              Document doc = Document.from(content);
                              doc.metadata().put("source", path.toAbsolutePath().toString());
                              documents.add(doc);
                          } catch (IOException e) {
                              throw new RuntimeException("Failed to read file: " + path, e);
                          }
                      });
            }
        }

        return documents;
    }

    private boolean isAllowedFileType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".csv");
    }
}
