package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.openai.OpenAiChat;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.SUFFIX;
import java.io.File;

public class NavigateQuestionsProcessor {
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if ((!ONLY_THIS.isEmpty() && !PREFIX.equals(ONLY_THIS)) || (ONLY_THIS.isEmpty() && FIRST_PREFIX >= 0 && (i < FIRST_PREFIX || i > LAST_PREFIX))) {
                continue;
            }
            new NavigateQuestionsProcessor("./generated-files/ui/screen").askQuestions();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }
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
        String answerPath = "./output-files/navigation/" + fileToGenerate + ".txt";
        String processedAnswerPath = "./processed-files/navigation/processed-" + fileToGenerate + ".txt";        
        System.out.println("Generate " + processedAnswerPath + " from " + pathForComposable);
        savedComposable = Utils.readFullFile(pathForComposable);
        File f = new File(documentsPath);
        if (f.exists()) {
            
            OpenAiChat chat = new OpenAiChat(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, documentsPath, answerPath, processedAnswerPath);
            chat.setup();

            String[] questions = getQuestions(savedComposable);
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
