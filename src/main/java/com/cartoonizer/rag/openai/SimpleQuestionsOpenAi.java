package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import java.io.File;

public class SimpleQuestionsOpenAi {

    public static void main(String[] args) {
        String documentsPath = "./example-files";
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, null, null);
            chat.setup();
            String[] questions = {
                "Provide a full Compose recreation of the entire fragment_home.xml file and the HomeFragment.kt file, keeping all the imports and components as is and using jetpack views navigation instead of Compose navigation"
            };
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
                if (!question.isEmpty()) {
                     System.out.println("QUESTION: " + question);
                    answers[i] = chat.askQuestion(question, 5, 0.7);
                    System.out.println("ANSWER: " + answers[i]);
                }
            }
            System.out.println("\n\n****************************************************************************************");
        }
    }

}
