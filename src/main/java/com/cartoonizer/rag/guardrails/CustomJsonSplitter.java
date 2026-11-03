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
            String imagen = product.get("imagen") == null ? "" : product.get("imagen").toString();
            String descripcion = product.get("descripcion") == null ? "" : product.get("descripcion").toString();
            String precio = product.get("precio") == null ? "" : product.get("precio").toString();
            Object obj = product.get("ampliacion");
            String ampliacion = obj == null ? "" : obj.toString();
            String sentences[] = ampliacion.split(Pattern.quote("."));
            StringBuilder s = new StringBuilder();
            for (int i = 0; sentences != null && i < sentences.length; i++) {
                String sentence = sentences[i];
                if (i < 2) {
                    s.append(sentence).append(" ");
                }
            }
            ampliacion = s.toString();
            String autor = product.get("autor") == null ? "" : product.get("autor").toString().trim();
            if (!autor.isBlank()) {
                autor = "Autor: " + autor;
            }

            Metadata metadata = new Metadata();
            metadata.put("id", id);
            metadata.put("descripcion", descripcion);
            metadata.put("ampliacion", descripcion);
            metadata.put("precio", precio);
            metadata.put("imagen", imagen);
            String segmentText = formatSegment(id, descripcion, ampliacion, precio, imagen);
            TextSegment segment = new TextSegment(segmentText, metadata);
            segments.add(segment);
//            System.out.println("--> " + segmentText);
        }
//        System.out.println("*** " + parsed);
        return segments;
    }

    private static String formatSegment(String id, String descripcion, String ampliacion, String precio, String imagen) {
        StringBuilder sb = new StringBuilder();
        sb.append("Titulo: ").append(descripcion).append(" Sinopsis: ").append(ampliacion).append(" Precio: ").append(precio).append(" Img: ").append(imagen).append(" Id: ").append(id);
        return sb.toString();
    }

    private static String formatSegmentHtml(String id, String descripcion, String ampliacion, String precio, String imagen) {
        StringBuilder sb = new StringBuilder();
        String pathThumb = "//haikubarcelona.com/img/catalogo/thumbs/thumb_" + imagen;
        String text = 
                """
		<div class="entrada cataleg">
                      	<div class="titulo clear">{{descripcion}}></div>
                      	<div class="info-article">
                      		<div class="article-left left">
                                    <div class="left imatge">
                                    <img class="thumb_cataleg" src="{{pathThumb}}" align="left" alt="Pulsa para ampliar la imagen"  title="Pulsa para ampliar la imagen" />
                      		</div>
                      		<div class="precio">Precio: {{precio}} &euro;</div>
                      		<div class="div-comprar">
                                    <div class="comprar"><a href="#id_{{id}}" onclick="comprar_link(this); return false;"  aria-label="Comprar">Comprar</a></div>
                      		</div>
                      	</div>
                      	<div class="article-right left">
                      		{{ampliacion}}
                        </div>
                    </div>
                </div>                      
                """;
        sb.append(text.replaceAll(Pattern.quote("{{descripcion}}"), descripcion)
        .replaceAll(Pattern.quote("{{pathThumb}}"), pathThumb)
        .replaceAll(Pattern.quote("{{precio}}"), precio)
        .replaceAll(Pattern.quote("{{id}}"), id)
        .replaceAll(Pattern.quote("{{ampliacion}}"), ampliacion));
        return sb.toString();
    }

}
