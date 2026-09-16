package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.openai.OpenAiChat;
import java.io.File;

public class SharedViewModelQuestionsProcessor {
    public static void askQuestions() {
        String documentsPath = "./example-files";
        String fileToGenerate = "ComposeSharedViewModel";
        String answerPath = "./output-files/" + fileToGenerate + ".txt";
        String processedAnswerPath = "./processed-files/processed-" + fileToGenerate + ".txt";
        
        String pathSharedViewModel = documentsPath  + "/MainActivityViewModel.kt";
        String savedSharedViewModel = Utils.readFullFile(pathSharedViewModel);
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();
            String[] questions = {
            "provide a jetpack Compose viewModel that implements the following jetpack views shared viewModel \n" +
            savedSharedViewModel +
            "When implementing the compose viewModel implement all the functions and vars in the model and preserve Jetpack Views navigation  \n" +
            "\nIgnore other viewmmodels found in the documents" +
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
