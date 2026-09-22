package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.openai.OpenAiChat;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.SUFFIX;
import java.io.File;

public class FragmentQuestionsProcessor {
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (!ONLY_THIS.isEmpty() && !PREFIX.equals(ONLY_THIS)) {
                continue;
            }
            new FragmentQuestionsProcessor("./example-files").askQuestions();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        }
    }
    public String savedViewModel;
    public String savedLayout;
    public String savedViewClass;
    public String documentsPath;
    public String pathForViewClass; // the Jetpack Views class
    public String pathViewModel; // the Jetpack Views
    public String fileToGenerate;
    public FragmentQuestionsProcessor(String documentsPath) {
        this.documentsPath = documentsPath;
        fileToGenerate = PREFIX + SUFFIX;
        pathForViewClass = documentsPath  + "/" + fileToGenerate + ".kt";
        pathViewModel = documentsPath  + "/" + PREFIX + "ViewModel.kt";
    }
    public void askQuestions() {
        String answerPath = "./output-files/" + fileToGenerate + ".txt";
        String processedAnswerPath = "./processed-files/processed-" + fileToGenerate + ".txt";
        
        String pathLayout = documentsPath  + "/" + LAYOUT;
        
        savedViewClass = Utils.readFullFile(pathForViewClass);
        savedViewModel = Utils.readFullFile(pathViewModel);
        savedLayout = Utils.readFullFile(pathLayout);
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();
            String[] questions = getQuestions(savedViewModel, savedViewClass, savedLayout);
//            - provide Jetpack Compose Modifier extensions that implement the properties of the android xml styles file styles.xml
//            - provide a jetpack compose composable function that implements the following layout xml file used in Jetpack Views using those Compose Theme and Modifiers
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
    protected String[] getQuestions(String savedViewModel, String savedViewClass, String xmlLayout) {
            String[] questions = {
            "1. provide a jetpack Compose composable that implements the following jetpack views class \n" +
            savedViewClass +
            "2. provide a Compose viewModel to be used by the composable based on the following Jetpack Views viewModel, exposing a Compose-friendly `UiState + UiEvent` architecture \n" + savedViewModel +
            "3. When implementing the composable and viewModel preserve Jetpack Views navigation  \n" +
            "4. Use dialog state, lifecycle collection of state/events for dialog handling \n" +
            "5. Use state holders/data classes to fully replace the fragment button logic. \n" +
            "6. provide a full `" + PREFIX + "ButtonsState` with exact button coloring/visibility matching the XML behavior and using `SharedFlow<" + PREFIX + "UiEffect>` instead of multiple event types\n" +
            "7. Provide a compose CustomDialog implementation based on the custom_dialog.xml file and the dialog implemented in the CustomDialog.kt file " +
            "8. Provide also a `" + PREFIX + "ButtonsState` with exact Compose button styling helpers matching the custom button component more closely" +
            "\nUse only android and jetpack compose references"

            };
            return questions;
    }
    
}
