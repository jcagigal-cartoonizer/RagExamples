package com.cartoonizer.rag.openai;
import com.cartoonizer.rag.shared.DbAssistant;
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.CustomJsonSplitter;
import com.cartoonizer.rag.utils.ModelNames;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
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
import java.util.List;
import java.util.Map;

public class UpdateTableWithAi implements AiChatInterface {
    public static void main(String[] args) {
        String documentsPath = "./files/database-rag-files";
        String theApiKey = ApiKeys.CLAUDE_API_KEY;
        String theModelName = ModelNames.CLAUDE_HAIKU;
        String remoteUrl = "https://haikubarcelona.com/rest/catalogo_internal.php";
        UpdateTableWithAi rag = new UpdateTableWithAi(documentsPath, remoteUrl, theApiKey, theModelName);
        rag.init();
        String question = "Cuantas filas tiene la tabla ARTICULOS";
        String answer = rag.askQuestion(question);
        System.out.println("QUESTION");
        System.out.println(question);
        System.out.println("\nANSWER");
        System.out.println(answer);
    }

    private String theApiKey;
    private String theModelName;
    private String documentsPath;
    private String remoteUrl;
    public UpdateTableWithAi(String documentsPath, String remoteUrl, String theApiKey, String theModelName) {
        this.documentsPath = documentsPath;
        this.remoteUrl = remoteUrl;
        this.theApiKey = theApiKey;
        this.theModelName = theModelName;
    }

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private DocumentByLineSplitter splitter;
    private CustomJsonSplitter jsonSplitter;
    private EmbeddingStoreIngestor ingestor;
    private EmbeddingStoreIngestor urlIngestor;
    private ChatModel chatModel;
    private RetrievalAugmentor retrievalAugmentor;
    private DbAssistant assistant;
    private DataSource dataSource;
    private static String URL_DB = "jdbc:derby:./data/haikugourmet";
    private static String USER_DB = "";
    private static String PWD_DB = "";
    private static String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private Connection connection;

    @Override
    public String askQuestion(String question) {
        String answer = assistant.answer(question, "ARTICULOS", "ARTICULOSWEB");
        return answer;
    }
//    @Tool(
//      "Find remote id with similar description")
//    public List<Map<String, Object>> findId(@P("Find remote id with similar description") String name) {
//      return query(
//        "SELECT " + COLUMNS + 
//        " FROM employees WHERE name LIKE ? ORDER BY name ", 
//        "%" + name + "%");
//    }    
    private Document loadCatalog() {
//        String sql = "select a.id, a.descripcion, a.ampliacion, a.precio, a.imagen, a.sinstock, aut.nombre as autor from ARTICULOS a left join AUTORES aut on a.autor = aut.id where publicar = 1 order by descripcion";
        String sql = "select a.id, a.descripcion, a.precio, a.imagen, aut.nombre as autor from ARTICULOS a left join AUTORES aut on a.autor = aut.id order by descripcion";
        try {
            Statement statement = connection.createStatement();
            boolean ok = statement.execute(sql.trim());
            StringBuilder results = new StringBuilder();
            int rowCount = 0;
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
                    rowCount++;
                }
            }
            Metadata metadata = new Metadata();
            metadata.put("query", sql);
            metadata.put("rowCount", rowCount);
            metadata.put("db", "ARTICULOS");
            Document doc = Document.from(results.toString(), metadata);
            return doc;
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
//        System.out.println("======================");
//        System.out.println(catalog.text());
//        System.out.println("======================");

        embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        embeddingStore = new InMemoryEmbeddingStore<>();
        
        splitter = new DocumentByLineSplitter(1024, 0, (TokenCountEstimator)null);
        ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(catalog);

        jsonSplitter = new CustomJsonSplitter(512, 0, (TokenCountEstimator)null);
        urlIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(jsonSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        Document remote = UrlDocumentLoader.load(remoteUrl, new TextDocumentParser());
        Metadata metadata = remote.metadata();
        metadata.put("query", "");
        metadata.put("db", "ARTICULOSWEB");
        urlIngestor.ingest(remote);

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
                .maxResults(10000)
                .minScore(0.2)
                .build();

        retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        this.assistant = AiServices.builder(DbAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    
}