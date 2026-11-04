package com.cartoonizer.rag.openai;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class OpenAiRagDatabases implements AiChatInterface {
    public static void main(String[] args) {
        String documentsPath = "./database-rag-files";
        String theModelName = ModelNames.CLAUDE_HAIKU;
        String theApiKey = ApiKeys.CLAUDE_API_KEY;
        OpenAiRagDatabases rag = new OpenAiRagDatabases(documentsPath, theApiKey, theModelName);
        rag.init();
//        rag.askQuestion("How many customers do we have?");
    }

    private String theApiKey;
    private String theModelName;
    private String documentsPath;
    public OpenAiRagDatabases(String documentsPath, String theApiKey, String theModelName) {
        this.documentsPath = documentsPath;
        this.theApiKey = theApiKey;
        this.theModelName = theModelName;
    }

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private DocumentByLineSplitter splitter;
    private EmbeddingStoreIngestor ingestor;
    private ChatModel chatModel;
    private RetrievalAugmentor retrievalAugmentor;
    private Assistant assistant;
    private DataSource dataSource;
    private static String URL_DB = "jdbc:derby:./data/haikugourmet";
    private static String USER_DB = "";
    private static String PWD_DB = "";
    private static String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private Connection connection;

    @Override
    public String askQuestion(String question) {
        String answer = assistant.answer(question);
        return answer;
    }
    private Document loadCatalog() {
//        String sql = "select a.id, a.descripcion, a.ampliacion, a.precio, a.imagen, a.sinstock, aut.nombre as autor from ARTICULOS a left join AUTORES aut on a.autor = aut.id where publicar = 1 order by descripcion";
        String sql = "select a.id, a.descripcion, a.precio, a.imagen, aut.nombre as autor from ARTICULOS a left join AUTORES aut on a.autor = aut.id order by descripcion";
        try {
            Statement statement = connection.createStatement();
            boolean ok = statement.execute(sql.trim());
            StringBuilder results = new StringBuilder();
            if (ok) {
                ResultSet res = statement.getResultSet();
                ResultSetMetaData metadata = res.getMetaData();
                int columnCount = metadata.getColumnCount();
                while (res.next()) {
                    StringBuilder sb = new StringBuilder();
                    String descripcion = res.getObject("descripcion").toString();
//                    String ampliacion = res.getObject("ampliacion").toString();
                    String precio = res.getObject("precio") == null ? "0" : res.getObject("precio").toString();
                    String autor = res.getObject("autor") == null ? "" : res.getObject("autor").toString();
//                    String sinstock = res.getObject("sinstock").toString().equals("1") ? "EN STOCK" : "AGOTADO";
                    String id = res.getObject("id") == null ? "" : res.getObject("id").toString();
//                    sb.append("Titulo: ").append(descripcion).append(" Descripcion: ").append(ampliacion).append(" Precio: ")
                    sb.append("Descripcion: ").append(descripcion).append(" Precio: ")
                        .append(precio);
                    if (autor != null && !autor.isBlank()) {
                        sb.append(" Autor: ").append(autor);
                    }
                    sb.append(" Id: ").append(id).append("\n");
                    results.append(sb);
                }
            }
            return Document.from(results.toString());
        } catch (Exception e) {
            return Document.from("ERROR " + e);
        }
    }
    @Override
    public void init() {
        try {
            Class.forName(DRIVER);
        } catch (Exception e) {
            System.out.println("ERROR Driver Database: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        try {
            if (connection == null) {
                connection = java.sql.DriverManager.getConnection(URL_DB, USER_DB, PWD_DB);
            }
        } catch (Exception e) {
            System.out.println("ERROR Database: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        final Document catalog = loadCatalog();
        System.out.println("======================");
        System.out.println(catalog.text());
        System.out.println("======================");

        embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        embeddingStore = new InMemoryEmbeddingStore<>();
        
        splitter = new DocumentByLineSplitter(1024, 0, (TokenCountEstimator)null);
        ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(catalog);

        chatModel = AnthropicChatModel.builder()
                .apiKey(theApiKey)
                .logRequests(false)
                .logResponses(false)
                .modelName(theModelName)
                .build();

        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    
}