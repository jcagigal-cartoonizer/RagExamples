package com.cartoonizer.rag.openai;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.utils.ApiKeys;
import static com.cartoonizer.rag.utils.ApiKeys.OPENAI_API_KEY;
import com.cartoonizer.rag.utils.ModelNames;
import com.cartoonizer.rag.utils.Utils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
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

public class RagWithDatabases implements AiChatInterface {
    private String theModelName;
    public RagWithDatabases(String theModelName) {
        this.theModelName = theModelName;
    }


    /**
     * Please refer to {@link Naive_RAG_Example} for a basic context.
     * <p>
     * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
     * <p>
     * This example demonstrates how to use SQL database content retriever.
     * <p>
     * WARNING! Although fun and exciting, {@link SqlDatabaseContentRetriever} is dangerous to use!
     * Do not ever use it in production! The database user must have very limited READ-ONLY permissions!
     * Although the generated SQL is somewhat validated (to ensure that the SQL is a SELECT statement),
     * there is no guarantee that it is harmless. Use it at your own risk!
     * <p>
     * In this example we will use an in-memory H2 database with 3 tables: customers, products and orders.
     * See "resources/sql" directory for more details.
     * <p>
     * This example requires "langchain4j-experimental-sql" dependency.
     */

    public static void main(String[] args) {
        String documentsPath = "";
        String theModelName = ModelNames.CHAT_GPT_MINI;
        RagWithDatabases rag = new RagWithDatabases(theModelName);
        rag.askQuestion("How many customers do we have?");
    }


    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private DocumentByLineSplitter splitter;
    private EmbeddingStoreIngestor ingestor;
    private ChatModel chatModel;
    private RetrievalAugmentor retrievalAugmentor;
    private Assistant assistant;
    private DataSource dataSource;
    private static String URL_DB = "";
    private static String USER_DB = "";
    private static String PWD_DB = "";
    private static DataSource createDataSource() {

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(URL_DB);
        dataSource.setUser(USER_DB);
        dataSource.setPassword(PWD_DB);

        return dataSource;
    }

    @Override
    public String askQuestion(String question) {
        String answer = assistant.answer(question);
        return answer;
    }
    private void embedQueryProducts() {
        String sql = "select a.id, a.descripcion, a.ampliacion, a.precio, a.imagen, a.sinstock, aut.nombre as autor from articulosweb a left join autores aut on a.autor = aut.id where publicar = 1 order by descripcion limit 0, 2000";
        try {
            Connection connection = dataSource.getConnection(); 
            Statement statement = connection.createStatement();
            boolean ok = statement.execute(sql.trim());
            if (ok) {
                ResultSet res = statement.getResultSet();
                ResultSetMetaData metadata = res.getMetaData();
                int columnCount = metadata.getColumnCount();
                while (res.next()) {
                    StringBuilder sb = new StringBuilder();
                    String descripcion = res.getObject("descripcion").toString();
                    String ampliacion = res.getObject("ampliacion").toString();
                    String precio = res.getObject("precio").toString();
                    String autor = res.getObject("autor").toString();
                    String sinstock = res.getObject("sinstock").toString().equals("1") ? "EN STOCK" : "AGOTADO";
                    String id = res.getObject("id").toString();
                    sb.append(id).append(" ").append(descripcion).append(" ").append(ampliacion).append(" ")
                        .append(precio).append(" ");
                    if (autor != null && !autor.isBlank()) {
                        sb.append(" Autor: ").append(autor);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void init() {

        embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        embeddingStore = new InMemoryEmbeddingStore<>();
        splitter = new DocumentByLineSplitter(1024, 0, new OpenAiTokenCountEstimator(theModelName));
        ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .temperature(0.2)
                .logRequests(false)
                .logResponses(false)
                .modelName(ModelNames.CHAT_GPT_MINI)
                .build();

        // We will create a CompressingQueryTransformer, which is responsible for compressing
        // the user's query and the preceding conversation into a single, stand-alone query.
        // This should significantly improve the quality of the retrieval process.
        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
        // It can be configured to customize the RAG behavior according to your requirements.
        // In subsequent examples, we will explore more customizations.
        retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        dataSource = createDataSource();
        
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}