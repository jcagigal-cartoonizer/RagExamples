package com.cartoonizer.rag.ollama;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleInfoRepo implements IInfoRepo {
  private InMemoryEmbeddingStore<TextSegment> embeddingStore;
  private OllamaEmbeddingModel embeddingModel;
  private ContentRetriever contentRetriever;
  private RetrievalAugmentor retrievalAugmentor;

  public SimpleInfoRepo(QueryTransformer queryTransformer, ChatModel ollamaModel) {
    embeddingStore = new InMemoryEmbeddingStore<>();
    embeddingModel = OllamaEmbeddingModel.builder()
      .baseUrl(OllamaAppConf.getOllamaEndpoint())
      .modelName(OllamaAppConf.getEmbeddingModel())
      .build();

    ingestDocuments(embeddingModel, embeddingStore);

    contentRetriever = EmbeddingStoreContentRetriever.builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .maxResults(3)
      .minScore(0.75)
      .build();
        // We will create a CompressingQueryTransformer, which is responsible for compressing
        // the user's query and the preceding conversation into a single, stand-alone query.
        // This should significantly improve the quality of the retrieval process.
        queryTransformer = new CompressingQueryTransformer(ollamaModel);

        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
        // It can be configured to customize the RAG behavior according to your requirements.
        // In subsequent examples, we will explore more customizations.
        retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();
  }

  @Override
  public String query(String query) {
    String result = null;

    List<Content> retrievedContents = contentRetriever.retrieve(Query.from(query));

    if (!retrievedContents.isEmpty()) {
      // You can also do something with the metadata here.
      // Example: retrievedContents.get(0).metadata().get("google-maps-coordinates");

      result = retrievedContents.stream()
        .map(content -> content.textSegment().text())
        .collect(Collectors.joining("\n\n"));
    }

    return result;
  }

  private void ingestDocuments(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
    List<Document> docs = FileSystemDocumentLoader.loadDocuments(Path.of(OllamaAppConf.getDocumentsPath()),
      new TextDocumentParser());
    EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
      .documentTransformer(doc -> {
        // Add some metadata here.
        //doc.metadata().put("", "");
        return doc;
      })
      .documentSplitter(new DocumentByParagraphSplitter(1000, 200))
      .embeddingModel(embeddingModel)
      .embeddingStore(embeddingStore)
      .build();
    ingestor.ingest(docs);
  }
}