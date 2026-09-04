
package com.cartoonizer.rag.ollama;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import com.cartoonizer.rag.shared.Assistant;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class OllamaChatWithAugmentor {
    public static void main(String[] args) {

        Assistant assistant = createAssistant("./example-files/story-about-joan-cagigal.txt");

        String questions[] = {
//            "provide a complete langchain4j sample project structure for migration from Jetpack Views to Jetpack Compose using langchain4j with ollama model. Use kotlin instead of groovy for dependencies. Don't repeat lines in output. Include code for langchain4j initialization",
//            "provide a complete langchain4j sample project structure for migration from Jetpack Views to Jetpack Compose using langchain4j with ollama model. Use kotlin instead of groovy for dependencies. Don't repeat lines in output. Include code for langchain4j initialization",
        "How to convert a android Jetpack Views xml layout file to a Jetpack Compose composable function that's visually equal",
//        """
//        provide a Jetpack Compose composable function source code that implements the following xml layout used with Jetpack Views:
//        <?xml version="1.0" encoding="utf-8"?>
//        <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
//            xmlns:app="http://schemas.android.com/apk/res-auto"
//            xmlns:tools="http://schemas.android.com/tools"
//            android:id="@+id/rootContainer"
//            android:layout_width="match_parent"
//            android:layout_height="match_parent">
//        
//            <ScrollView
//                android:layout_width="0dp"
//                android:layout_height="0dp"
//                android:fillViewport="true"
//                app:layout_constraintBottom_toTopOf="@+id/lytContainerFlowMenu"
//                app:layout_constraintEnd_toEndOf="parent"
//                app:layout_constraintHorizontal_bias="0.0"
//                app:layout_constraintStart_toStartOf="parent"
//                app:layout_constraintTop_toTopOf="parent"
//                app:layout_constraintVertical_bias="0.0">
//        
//                <androidx.constraintlayout.widget.ConstraintLayout
//                    android:id="@+id/container"
//                    android:layout_width="match_parent"
//                    android:layout_height="match_parent">
//        
//                    <androidx.constraintlayout.widget.ConstraintLayout
//                        android:id="@+id/lytPbDownload"
//                        android:layout_width="0dp"
//                        android:layout_height="0dp"
//                        android:background="@color/dark_background"
//                        android:focusable="true"
//                        android:focusableInTouchMode="true"
//                        android:visibility="gone"
//                        app:layout_constraintBottom_toBottomOf="parent"
//                        app:layout_constraintEnd_toEndOf="parent"
//                        app:layout_constraintStart_toStartOf="parent"
//                        app:layout_constraintTop_toTopOf="parent">
//        
//                        <TextView
//                            android:id="@+id/tvTitlePbDownload"
//                            android:layout_width="wrap_content"
//                            android:layout_height="wrap_content"
//                            android:text="@string/descargando_configuracion"
//                            android:gravity="center_horizontal"
//                            app:layout_constraintBottom_toTopOf="@+id/pbDownload"
//                            app:layout_constraintEnd_toEndOf="parent"
//                            app:layout_constraintHorizontal_bias="0.5"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toTopOf="parent"
//                            app:layout_constraintVertical_chainStyle="packed" />
//        
//                        <ProgressBar
//                            android:id="@+id/pbDownload"
//                            style="?android:attr/progressBarStyleHorizontal"
//                            android:layout_width="0dp"
//                            android:layout_height="wrap_content"
//                            android:layout_centerInParent="true"
//                            android:layout_marginStart="32dp"
//                            android:layout_marginEnd="32dp"
//                            android:max="4"
//                            android:progress="0"
//                            app:layout_constraintBottom_toBottomOf="parent"
//                            app:layout_constraintEnd_toEndOf="parent"
//                            app:layout_constraintHorizontal_bias="0.5"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toBottomOf="@+id/tvTitlePbDownload" />
//                    </androidx.constraintlayout.widget.ConstraintLayout>
//        
//                    <androidx.constraintlayout.widget.ConstraintLayout
//                        android:id="@+id/rootLayout"
//                        android:layout_width="match_parent"
//                        android:layout_height="0dp"
//                        android:padding="20dp"
//                        app:layout_constraintEnd_toEndOf="parent"
//                        app:layout_constraintStart_toStartOf="parent"
//                        app:layout_constraintTop_toTopOf="parent">
//        
//                        <TextView
//                            android:id="@+id/tvTitleUser"
//                            android:layout_width="wrap_content"
//                            android:layout_height="wrap_content"
//                            android:text="@string/usuario"
//                            app:layout_constraintBottom_toTopOf="@+id/edUser"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toTopOf="parent"
//                            app:layout_constraintVertical_chainStyle="packed" />
//        
//                        <ifac.td.taxi.ui.custom.fields.CustomEditText
//                            android:id="@+id/edUser"
//                            style="@style/Theme.SmartTD.CustomEditText"
//                            android:layout_width="0dp"
//                            android:layout_height="48dp"
//                            android:layout_marginBottom="16dp"
//                            android:ems="10"
//                            android:hint="@string/usuario_hint"
//                            android:importantForAutofill="no"
//                            android:inputType="text"
//                            android:text="@string/usuario_example"
//                            app:layout_constraintBottom_toTopOf="@+id/tvTitlePassword"
//                            app:layout_constraintEnd_toEndOf="parent"
//                            app:layout_constraintHorizontal_bias="0.5"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toBottomOf="@+id/tvTitleUser" />
//        
//                        <TextView
//                            android:id="@+id/tvTitlePassword"
//                            android:layout_width="wrap_content"
//                            android:layout_height="wrap_content"
//                            android:text="@string/contrasena"
//                            app:layout_constraintBottom_toTopOf="@+id/edPassword"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toBottomOf="@+id/edUser" />
//        
//                        <ifac.td.taxi.ui.custom.fields.CustomEditText
//                            android:id="@+id/edPassword"
//                            style="@style/Theme.SmartTD.CustomEditText"
//                            android:layout_width="0dp"
//                            android:layout_height="48dp"
//                            android:layout_marginBottom="16dp"
//                            android:ems="10"
//                            android:hint="@string/contrasena_hint"
//                            android:importantForAutofill="no"
//                            android:inputType="textPassword"
//                            android:text=""
//                            app:layout_constraintBottom_toTopOf="@+id/tvChangePassword"
//                            app:layout_constraintEnd_toEndOf="parent"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toBottomOf="@+id/tvTitlePassword" />
//        
//                        <TextView
//                            android:id="@+id/tvChangePassword"
//                            style="@style/Theme.SmartTD.TextView"
//                            android:layout_width="wrap_content"
//                            android:layout_height="wrap_content"
//                            android:text="@string/change_password"
//                            android:visibility="visible"
//                            app:layout_constraintBottom_toBottomOf="parent"
//                            app:layout_constraintStart_toStartOf="parent"
//                            app:layout_constraintTop_toBottomOf="@+id/edPassword" />
//                    </androidx.constraintlayout.widget.ConstraintLayout>
//        
//                </androidx.constraintlayout.widget.ConstraintLayout>
//            </ScrollView>
//        
//            <GridLayout
//                android:id="@+id/lytContainerFlowMenu"
//                android:layout_width="match_parent"
//                android:layout_height="0dp"
//                android:background="@color/black"
//                android:columnCount="2"
//                android:horizontalSpacing="6dp"
//                android:rowCount="1"
//                android:verticalSpacing="6dp"
//                app:layout_constraintBottom_toBottomOf="parent"
//                app:layout_constraintDimensionRatio="5:2"
//                app:layout_constraintEnd_toEndOf="parent"
//                app:layout_constraintStart_toStartOf="parent">
//        
//        
//                <ifac.td.taxi.ui.custom.button.CustomButton
//                    android:id="@+id/btnCancel"
//                    android:layout_width="0dp"
//                    android:layout_height="0dp"
//                    android:layout_row="0"
//                    android:layout_rowWeight="1"
//                    android:layout_column="0"
//                    android:layout_columnWeight="1"
//                    android:layout_gravity="fill"
//                    android:layout_margin="1dp"
//                    app:customFunction="CANCEL" />
//        
//        
//                <ifac.td.taxi.ui.custom.button.CustomButton
//                    android:id="@+id/btnAccept"
//                    android:layout_width="0dp"
//                    android:layout_height="0dp"
//                    android:layout_row="0"
//                    android:layout_rowWeight="1"
//                    android:layout_column="1"
//                    android:layout_columnWeight="1"
//                    android:layout_gravity="fill"
//                    android:layout_margin="1dp"
//                    app:customFunction="ACCEPT" />
//        
//            </GridLayout>
//        </androidx.constraintlayout.widget.ConstraintLayout>"""
        };
        String answers[] = new String[questions.length];
        for (int i = 0; i < questions.length; i++) {
            answers[i] = assistant.answer(questions[i]);            
        }
        for (int i = 0; i < questions.length; i++) {
            System.out.println("\n************************************************************************************");
            System.out.println("Answer to " + questions[i] + " is:");        
            System.out.println("    " + answers[i]);        
        }
        System.out.println("\n==========================================================================================");
    }

    private static Assistant createAssistant(String documentPath) {

        Document document = FileSystemDocumentLoader.loadDocument(documentPath, new TextDocumentParser());

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(1024, 0);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);

        ChatModel chatModel = OllamaChatModel.builder()
                .baseUrl(OllamaAppConf.getOllamaEndpoint())
                .temperature(0.2)
                .logRequests(false)
                .logResponses(false)
                .modelName(OllamaWithRetrievalAugmentor.TINY_DOLPHIN_MODEL)
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
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    
}
