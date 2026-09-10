package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.migration.prompters.PrompterViewToCompose;
import com.cartoonizer.rag.shared.Assistant;
import com.cartoonizer.rag.shared.AssistantWithInfo;
import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.time.Duration;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.joining;

public class OpenAiChat {

    public int maxResults;
    public DocumentParser documentParser = new TextDocumentParser();
    public EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
    public EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    public List<Document> documents;
    public DocumentSplitter splitter;
    public ChatModel chatModel;
    public AssistantWithInfo assistant;
    public String theApiKey = ApiKeys.OPENAI_API_KEY;
    public String theModelName = ModelNames.CHAT_GPT_MINI;
    public String theFilesPath;
    public String answerPath;
    public String processedAnswerPath;
    public List<EmbeddingMatch<TextSegment>> relevantEmbeddings;
    
    public String askQuestion(String question, int maxResults, double minScore) {

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();
        
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        relevantEmbeddings = embeddingStore.search(embeddingSearchRequest).matches();
//        PrompterViewToCompose prompter = new PrompterViewToCompose(relevantEmbeddings, chatModel, documents, answerPath, processedAnswerPath);
//        String answer = prompter.getPrompt(question);
        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().text())
                .collect(joining("\n\n"));
        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                + "\n"
                + "Question:\n"
                + "{{question}}\n"
                + "\n"
                + "Base your answer on the following information:\n"
                + "{{information}}");
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("information", information);

        PrompterViewToCompose prompter = new PrompterViewToCompose(relevantEmbeddings, chatModel, documents, answerPath, processedAnswerPath);
//        AiMessage aiMessage = chatModel.chat(prompt.toUserMessage()).aiMessage();
//        String answer = aiMessage.text();
        
        System.out.println("===============================");
        System.out.println("INFO:");
        System.out.println(information);
        System.out.println("===============================");
//        String answer = assistant.answerWithInfo(prompt.toUserMessage().singleText(), information);
        String answer = prompter.getPrompt(question);
        return answer;
    }

    private OpenAiChat() {

    }

    public OpenAiChat(String apiKey, String modelName, String documentsPath, String answerPath, String processedAnswerPath) {
        theFilesPath = documentsPath;
        theApiKey = apiKey;
        theModelName = modelName;
        this.answerPath = answerPath;
        this.processedAnswerPath = processedAnswerPath;
    }

    protected DocumentSplitter createSplitter() {
//        DocumentByLineSplitter subSplitter = new DocumentByLineSplitter(
//                1024,
//                1024,
//                new OpenAiTokenCountEstimator(theModelName)
//        );
//        DocumentByLineSplitter splitter = new DocumentByLineSplitter(
//                1024,
//                1024,
//                new OpenAiTokenCountEstimator(theModelName),
//                subSplitter
//        );
        DocumentSplitter splitter = new DocumentByParagraphSplitter(
                512,
                0,
                new OpenAiTokenCountEstimator(theModelName)
        );
        return splitter;
    }

    protected void processTextSegments(Document document, List<TextSegment> segments) {
        Metadata docMetadata = document.metadata();
        String docFileName = docMetadata.getString("file_name");
        String docPath = docMetadata.getString("absolute_directory_path") + "/" + docFileName;
        if (!docPath.contains("strings.xml")) {
            System.out.println("========================================================================");
            System.out.println("Doc metadata " + docPath + " " + docMetadata);
            for (TextSegment segment : segments) {
                System.out.println("    segment metadata " + segment.metadata() + " text " + segment.text());
            }
        }

        // Embed segments (convert them into vectors that represent the meaning) using embedding model        
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // Store embeddings into embedding store for further search / retrieval
        embeddingStore.addAll(embeddings, segments);

    }

    public void setup() {
        // Load the document that includes the information you'd like to "chat" about with the model.
        documents = FileSystemDocumentLoader.loadDocuments(theFilesPath, documentParser);
        splitter = createSplitter();
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        // Each retrieved segment should include "file_name" and "index" metadata values in the prompt
        ContentInjector contentInjector = DefaultContentInjector.builder()
                // .promptTemplate(...) // Formatting can also be changed
                .metadataKeysToInclude(asList("file_name", "index"))
                .build();
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentInjector(contentInjector)
                .build();

        chatModel = OpenAiChatModel.builder()
                .apiKey(theApiKey)
                .modelName(theModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
        assistant = AiServices.builder(AssistantWithInfo.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        for (Document document : documents) {
            processTextSegments(document, splitter.split(document));
        }
    }

}
