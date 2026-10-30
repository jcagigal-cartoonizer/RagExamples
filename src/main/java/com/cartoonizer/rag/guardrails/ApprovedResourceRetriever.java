package com.cartoonizer.rag.guardrails;

import com.cartoonizer.rag.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;

public class ApprovedResourceRetriever {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private List<TextSegment> textSegments;

    public ApprovedResourceRetriever(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        this.textSegments = new ArrayList<>();
    }

    public void ingest(List<Document> documents) {
        for (Document doc : documents) {
            Metadata metadata = doc.metadata();
            TextSegment segment = TextSegment.from(doc.text(), metadata);
            String text = segment.text();
            String url = metadata.getString("url");
            if (url == null) {
                url = "";
            }
            String source = metadata.getString("source");
            if (source == null) {
                source = "";
            }
            OpenAiTokenCountEstimator tokenCountEstimator = new OpenAiTokenCountEstimator(ModelNames.CHAT_GPT_MINI);
            DocumentSplitter splitter = url.endsWith(".php") ?
                new CustomJsonSplitter(512, 0, tokenCountEstimator) :
                new DocumentByLineSplitter(
                    512,
                    0,
                    tokenCountEstimator
            );
            List<TextSegment> segments = splitter.split(doc);
            System.out.println("*** ApprovedResourceRetriever after split segments size = " + segments.size()); // + text);
            List<TextSegment> newSegments = new ArrayList<>();
            for (TextSegment textSegment : segments) {
                Metadata meta = textSegment.metadata();
                meta.put("url", url);
                meta.put("source", source);
                TextSegment newSegment = new TextSegment(textSegment.text(), meta);
//                System.out.println("    -> newSegment = " + newSegment);
                newSegments.add(newSegment);
            }
            Response<List<Embedding>> response = embeddingModel.embedAll(newSegments);
            List<Embedding> embedding = response.content();
            System.out.println("*** ApprovedResourceRetriever embedding = " + embedding.size() + " newSegments = " + newSegments.size());
            embeddingStore.addAll(embedding, newSegments);
            textSegments.addAll(newSegments);
        }
    }

    public List<TextSegment> retrieve(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(10)
                        .minScore(0.7)
                        .build()
        );

        textSegments = result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());
        int numNotNull = 0;
        for (TextSegment textSegment : textSegments) {
            if (textSegment != null) {
                numNotNull++;
            }
        }
        System.out.println("*** ApprovedResourceRetriever retrieve textSegments size = " + textSegments.size() + " NOT NULL = " + numNotNull);
        return textSegments;
    }

    /**
     * @return the textSegments
     */
    public List<TextSegment> getTextSegments() {
        return textSegments;
    }
}
