package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import com.cartoonizer.rag.shared.utils.Utils;
import java.io.File;

public class SimpleExampleOpenAi {

    public static void main(String[] args) {
        String documentsPath = "./example-files";
        String fileToGenerate = "HomeFragment";
        String answerPath = "./output-files/" + fileToGenerate + ".txt";
        String processedAnswerPath = "./processed-files/processed-" + fileToGenerate + ".txt";
        
        String pathForComposable = documentsPath  + "/" + fileToGenerate + ".kt";
        String pathViewModel = documentsPath  + "/HomeViewModel.kt";
        String pathLayout = documentsPath  + "/fragment_home.xml";
        String pathSharedViewModel = documentsPath  + "/MainActivityViewModel.kt";
        
        String savedComposable = Utils.readFullFile(pathForComposable);
        String savedViewModel = Utils.readFullFile(pathViewModel);
        String savedLayout = Utils.readFullFile(pathLayout);
        String savedSharedViewModel = Utils.readFullFile(pathSharedViewModel);
        
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();
            String[] questions = {
            "- provide a jetpack Compose composable function with viewModel that implements the following class \n" +
            savedComposable +
            "- Use Compose Navigation and NavController instead of Jetpack Views navigation \n" +
            "- Use dialog state, lifecycle collection of state/events for dialog handling \n" +
            "- Replace navigateTo calls with route-based navigation events\n" +
            "- Use state holders/data classes to fully replace the fragment button logic. This is the viewModel:\n" +
            "- provide a fully refactored ViewModel version based in the following ViewModel, removing all fragment navigation references and exposing a Compose-friendly `UiState + UiEvent` architecture\n" +
            savedViewModel +
            "- Use this shared view model:\n" +
            savedSharedViewModel +
            "- Use this layout file for the composable:\n" +
            savedLayout +
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
