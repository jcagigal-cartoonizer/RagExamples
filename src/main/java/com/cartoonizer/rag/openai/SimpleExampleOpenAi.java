package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.ApiKeys;
import com.cartoonizer.rag.shared.utils.ModelNames;
import java.io.File;

public class SimpleExampleOpenAi {

    public static void main(String[] args) {
        String documentsPath = "./example-files";
        File f = new File(documentsPath);
        if (f.exists()) {
            System.out.println("Before setup ");
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath);
            chat.setup();
//            String question = "Who is Juan Cagigal";
            String[] questions = {
//                "Who is Juan Cagigal",
//                "Give me some reasons tu use RAG software",
//                "How to use RAG to migrate from Jetpack Views to Jetpack Compose with langchain4j ",
//                "provide a **complete langchain4j sample project structure** for migration from Jetpack Views to Jetpack Compose with langchain4j. retrieve **only relevant Android/Compose documents**, not unrelated text",
//                "Based on the previous answer, provide a screen-by-screen migration example from XML to Compose",
//                "Who is Juan Cagigal",
//                "Give me some reasons tu use RAG software",
//                "How to use RAG to migrate from Jetpack Views to Jetpack Compose with langchain4j ",
//                "provide a **complete langchain4j sample project structure** for migration from Jetpack Views to Jetpack Compose with langchain4j. retrieve **only relevant Android/Compose documents**, not unrelated text",
//                "Based on the previous answer, provide a screen-by-screen migration example from XML to Compose",
            """
                       
            """
            };
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
