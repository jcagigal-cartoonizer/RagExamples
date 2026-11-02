package com.cartoonizer.rag.ollama;

import com.cartoonizer.rag.guardrails.CustomJsonSplitter;
import static com.cartoonizer.rag.guardrails.CustomJsonSplitter.fromJsonArticles;
import static com.cartoonizer.rag.ollama.OllamaWithRetrievalAugmentor.OLLAMA_MODEL;
import com.cartoonizer.rag.utils.Utils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.Response;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleInfoRepo implements IInfoRepo {

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private OllamaEmbeddingModel embeddingModel;
    private ContentRetriever contentRetriever;
    private RetrievalAugmentor retrievalAugmentor;
    private List listOfFolders;
    private List listOfUrls;

    public SimpleInfoRepo(QueryTransformer queryTransformer, ChatModel ollamaModel, List listOfFolders, List listOfUrls) {
        this.listOfFolders = listOfFolders;
        this.listOfUrls = listOfUrls;

        embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(OllamaAppConf.getOllamaEndpoint())
                .modelName(OLLAMA_MODEL)
                .build();

        ingestDocuments(embeddingModel, embeddingStore);

        contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(100)
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
            System.out.println("SimpleInfoRepo query = " + query);
        String result = null;

        List<Content> retrievedContents = contentRetriever.retrieve(Query.from(query));

        List<String> newSegments = new ArrayList<>();
        if (!retrievedContents.isEmpty()) {
            // You can also do something with the metadata here.
            // Example: retrievedContents.get(0).metadata().get("google-maps-coordinates");
            for (Content retrievedContent : retrievedContents) {
                String text = retrievedContent.toString();
                newSegments.add(text);
//                List<TextSegment> segments = fromJsonArticles(text);
//                for (TextSegment segment : segments) {
//                    newSegments.add(segment.text());
//                }
            }
            result = String.join("\n----\n", newSegments);
//            result = retrievedContents.stream()
//                    .map(content -> content.textSegment().text())
//                    .collect(Collectors.joining("\n\n"));
            System.out.println("SimpleInfoRepo result length = " + result.length());
        }

        return result;
    }

    private void ingestDocuments(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        List<Document> files = new ArrayList<>();
        // Save the urls response and process them as files
        for (Object url : listOfUrls) {
            String strUrl = url.toString();
            final Document document = UrlDocumentLoader.load(strUrl, new TextDocumentParser());
            String text = document.text();
            System.out.println("ingestDocuments " + url.toString() + " text length = " + text.length());
            String folder = listOfFolders.get(0).toString();
            int idx = strUrl.lastIndexOf("/");
            String name = "catalog.txt";
            if (idx > 0) {
                name = strUrl.substring(idx + 1).replaceAll(Pattern.quote(".php"), ".txt");
            }
            List<TextSegment> segments = fromJsonArticles(text);
            if (segments.size() > 400) {
                for (int i = 0; i < segments.size(); i+=400) {
                    List<TextSegment> partial = new ArrayList<>();
                    for (int j = i; j < (i + 400) && j < segments.size(); j++) {
                        TextSegment segment = segments.get(j);
                        if (j < 10) {
                            System.out.println("====== SimpleInfoRepo " + segment.text());
                        }
                        partial.add(segment);
                    }
                    String path = folder + "/" + name + "-" + i;
                    saveSegments(path, partial);
                }
            } else {
                String path = folder + "/" + name;
                saveSegments(path, segments);
            }
        }
        for (Object folder : listOfFolders) {
            List<Document> docs = FileSystemDocumentLoader.loadDocuments(folder.toString(), new TextDocumentParser());
            files.addAll(docs);
        }
        
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentTransformer(doc -> {
                    // Add some metadata here.
                    //doc.metadata().put("", "");
                    return doc;
                })
                .documentSplitter(new DocumentByLineSplitter(512, 0, (TokenCountEstimator)null))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        int batchSize = 200;
        for (int i = 0; i < files.size() && i < batchSize; i++) {
            System.out.println("*** ingesting file " + i);
            ingestor.ingest(files.get(i));
        }
//        ingestor.ingest(files);
    }

    private void saveSegments(String path, List<TextSegment> segments) {
        System.out.println("*** saveSegments path = " + path);
            StringBuilder sb = new StringBuilder();
            for (TextSegment segment : segments) {
                sb.append(segment.text()).append("\n");
            }
            String text = sb.toString();
            Utils.saveFile(path, text);
    }
}
