package com.cartoonizer.rag.guardrails;

import com.google.gson.Gson;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomJsonSplitter extends DocumentByLineSplitter {

    public CustomJsonSplitter(int maxSegmentSizeInTokens, int maxOverlapSizeInTokens, TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator);
    }

    @Override
    public List<TextSegment> splitAll(List<Document> documents) {
        return super.splitAll(documents);
    }

    @Override
    public List<TextSegment> split(Document doc) {
        List<TextSegment> segments = fromJsonArticles(doc.text());
//        doc = Document.from(text);
//        return super.split(doc);
        return segments;
    }
    
    public static List<TextSegment> fromJsonArticles(String text) {
        List<TextSegment> segments = new ArrayList<>(2000);
        Map map = new Gson().fromJson(text, Map.class);
        List dades = (List) map.get("dades");
        System.out.println("CustomJsonSplitter fromJsonArticles -> dades size = " + dades.size());
        for (Object article : dades) {
            Map product = (Map) article;
            StringBuilder sb = new StringBuilder();
            String id = product.get("id").toString();
            String ampliacion = product.get("ampliacion").toString();
            String sentences[] = ampliacion.split(Pattern.quote("."));
            StringBuilder s = new StringBuilder();
            for (int i = 0; sentences != null && i < sentences.length; i++) {
                String sentence = sentences[i];
                if (i < 2) {
                    s.append(sentence).append(" ");
                }
            }
            ampliacion = s.toString();
            Object obj = product.get("autor");
            String autor = obj == null ? "" : obj.toString().trim();
            if (!autor.isBlank()) {
                autor = "Autor: " + autor;
            }
            String segmentText = sb.append(product.get("descripcion").toString()).append(" ").append(ampliacion.replaceAll("\n", " ")).append(" Precio: ").append(product.get("precio")).append(autor).toString();
            Metadata metadata = new Metadata();
            metadata.put("id", id);
            TextSegment segment = new TextSegment(segmentText, metadata);
            segments.add(segment);
//            System.out.println("--> " + segmentText);
        }
//        System.out.println("*** " + parsed);
        return segments;
    }
}
