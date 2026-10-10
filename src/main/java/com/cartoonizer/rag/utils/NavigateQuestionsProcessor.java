package com.cartoonizer.rag.utils;

import com.cartoonizer.rag.openai.OpenAiChat;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FILES_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ANSWER_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PROCESSED_FOLDER_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.shouldSkip;
import java.io.File;

public class NavigateQuestionsProcessor {
    public static void main(String[] args) {
        int totalFiles = 0;
        int processedFiles = 0;
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            totalFiles++;
            if (shouldSkip(i)) {
                continue;
            }
            new NavigateQuestionsProcessor(GENERATED_FILES_WITH_ASSISTANT + "/ui/screen").askQuestions();
            processedFiles++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("===> processedFiles = " + processedFiles + " of " + totalFiles);
    }
    public String savedComposable;
    public String documentsPath;
    public String pathForComposable;
    public String fileToGenerate;
    public NavigateQuestionsProcessor(String documentsPath) {
        this.documentsPath = documentsPath;
        fileToGenerate = PREFIX + "NavigationFragment";
        pathForComposable = documentsPath  + "/" + PREFIX + "Screen.kt";
    }

    public void askQuestions() {
        String answerPath = ANSWER_FOLDER_WITH_ASSISTANT + "/navigation/" + fileToGenerate + ".txt";
        String processedAnswerPath = PROCESSED_FOLDER_WITH_ASSISTANT + "/navigation/processed-" + fileToGenerate + ".txt";        
        System.out.println("Generate " + processedAnswerPath + " from " + pathForComposable);
        if (!new File(pathForComposable).exists()) {
            return;
        }
        savedComposable = Utils.readFullFile(pathForComposable);
        File f = new File(documentsPath);
        if (f.exists()) {
            
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();

//            String[] questions = getQuestions(savedComposable);
            
            String pathOriginalFragment = "./example-files/" + PREFIX + "Fragment.kt";
            String pathNavigation = "./example-files/navigation/full_navigation.xml";
            String savedOriginalFragment = Utils.readFullFile(pathOriginalFragment);
            String savedNavigation = Utils.readFullFile(pathNavigation);
            String[] questions = getSimpleQuestions(savedComposable, savedOriginalFragment, savedNavigation);
            
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
                System.out.println("QUESTION: " + question);
                answers[i] = chat.askQuestion(question, 3, 0.7);
                System.out.println("ANSWER: " + answers[i]);
            }
            System.out.println("\n\n****************************************************************************************");
        }
    }
    protected String[] getSimpleQuestions(String savedComposable, String savedOriginalFragment, String savedNavigation) {
        String composableFullName = "ifac.td.taxi.ui.screen." + PREFIX + "Screen";
        String fragmentFullName = "ifac.td.taxi.ui.screen." + PREFIX + "Fragment";
            String[] questions = {
            "- I have this JetPack Compose composable screen " + composableFullName + " \n" +
            savedComposable +
            "- But I'm using JetPack Views navigation with this navigation xml file: \n" +
            savedNavigation +
            "\n- Provide a Fragment that can be used instead of " + fragmentFullName + " as a navigation destination but using the composable function instead of this fragment:\n" +
            savedOriginalFragment +
            "\n- Name the fragment ifac.td.taxi.ui.screen." + PREFIX + "NavigationFragment\n" +
            "\n- Use viewModels() instead of compose.viewModel() to initialize the viewModel\n" +
            "Use only android and jetpack compose references\n"

            };
            return questions;
    }
    protected String[] getQuestions(String savedComposable) {
            String[] questions = {
            "- provide a plain Android `View` factory function that contains a ComposeView that instanciates the following jetpack compose composable inside the setContent function. The parameters values for the constructor must be taken from the composable code \n" +
            savedComposable +
            "\n- provide also an example of how to use this view in Jetpack Views navigation\n" +
            "Use only android and jetpack compose references\n"

            };
            return questions;
    }
    
}
