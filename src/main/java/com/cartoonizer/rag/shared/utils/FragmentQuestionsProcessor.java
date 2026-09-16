package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.openai.OpenAiChat;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUT;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import java.io.File;

public class FragmentQuestionsProcessor {
    public static void askQuestions() {
        String documentsPath = "./example-files";
        String fileToGenerate = PREFIX + "Fragment";
        String answerPath = "./output-files/" + fileToGenerate + ".txt";
        String processedAnswerPath = "./processed-files/processed-" + fileToGenerate + ".txt";
        
        String pathForViewClass = documentsPath  + "/" + fileToGenerate + ".kt";
        String pathViewModel = documentsPath  + "/" + PREFIX + "ViewModel.kt";
        File viewModel = new File(pathViewModel);
        String pathLayout = documentsPath  + "/" + LAYOUT;
        String pathSharedViewModel = documentsPath  + "/MainActivityViewModel.kt";
        
        String savedViewClass = Utils.readFullFile(pathForViewClass);
        String savedViewModel = Utils.readFullFile(pathViewModel);
        String savedLayout = Utils.readFullFile(pathLayout);
        String savedSharedViewModel = Utils.readFullFile(pathSharedViewModel);
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();
            String[] questions = {
            "1. provide a jetpack Compose composable that implements the following jetpack views class class \n" +
            savedViewClass +
            (viewModel.exists() ? "2. provide a Compose viewModel to be used by the composable based on the following Jetpack Views viewModel, exposing a Compose-friendly `UiState + UiEvent` architecture \n" + savedViewModel :
                    "") +
            "3. When implementing the composable and viewModel preserve Jetpack Views navigation  \n" +
            "4. Use dialog state, lifecycle collection of state/events for dialog handling \n" +
            "5. Use state holders/data classes to fully replace the fragment button logic. \n" +
            "6. provide a full `" + PREFIX + "ButtonsState` with exact button coloring/visibility matching the XML behavior and using `SharedFlow<" + PREFIX + "UiEffect>` instead of multiple event types\n" +
            "7. Provide a compose CustomDialog implementation based on the custom_dialog.xml file and the dialog implemented in the CustomDialog.kt file " +
            "8. Provide also a `" + PREFIX + "ButtonsState` with exact Compose button styling helpers matching the custom button component more closely" +
            "\nUse only android and jetpack compose references"

            };
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
    
}
