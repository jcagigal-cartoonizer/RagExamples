package com.cartoonizer.rag.questions;

import com.cartoonizer.rag.openai.OpenAiChat;
import com.cartoonizer.rag.openai.OpenAiChat;
import com.cartoonizer.rag.utils.ApiKeys;
import com.cartoonizer.rag.utils.ModelNames;
import com.cartoonizer.utils.Utils;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.GENERATED_FILES;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.ANSWER_FOLDER;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.GENERATED_FULL_FILES;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.PROCESSED_FOLDER;
import static com.cartoonizer.rag.answers.GeneralAnswerProcessor.shouldSkip;
import com.cartoonizer.rag.openai.OpenAiChatNavigation;
import com.cartoonizer.rag.openai.OpenAiChatWithAugmentor;
import java.io.File;
import java.util.HashMap;

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
            String outputPath = GENERATED_FULL_FILES + "/ui/screen/navigation";
            new NavigateQuestionsProcessor(outputPath).askQuestions();
            processedFiles++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("===> processedFiles = " + processedFiles + " of " + totalFiles);
    }
    public String savedComposable;
    public String savedViewModel;
    public String outputPath;
    public String pathForComposable;
    public String fileToGenerate;
    public NavigateQuestionsProcessor(String outputPath) {
        this.outputPath = outputPath;
        fileToGenerate = PREFIX + "NavigationFragment";
        // /Cagi/Portfolio/RagUtils/RagExamples/files/generated-full-files/ui/screen/compose/
        pathForComposable = GENERATED_FULL_FILES  + "/ui/screen/compose/" + PREFIX + "ComposeImplementation.kt";
    }

    public void askQuestions() {
        String answerPath = ANSWER_FOLDER + "/navigation/" + fileToGenerate + ".txt";
        String processedAnswerPath = PROCESSED_FOLDER + "/navigation/processed-" + fileToGenerate + ".txt";        
        System.out.println("Generate " + outputPath + "/" + fileToGenerate + " from " + pathForComposable);
        if (!new File(pathForComposable).exists()) {
            System.out.println("ERROR composable " + pathForComposable + " MISSING");
            return;
        }
        String fullContent = Utils.readFullFile(pathForComposable);
        savedComposable = processFileForScreen(fullContent);
        savedViewModel = processFileForViewModel(fullContent);
//        System.out.println("======================= savedComposable =======================");
//        System.out.println(savedComposable);
//        System.out.println("======================= savedViewModel =======================");
//        System.out.println(savedViewModel);
//        System.out.println("===============================================================");
        System.out.println("*** outputPath = " + outputPath);
        File f = new File(outputPath);
        boolean doIt = true;
        if (!doIt) {
            return;
        }
        if (f.exists()) {
            
            OpenAiChatNavigation chat = new OpenAiChatNavigation(ApiKeys.OPENAI_API_KEY, ModelNames.CHAT_GPT_MINI, outputPath, answerPath, processedAnswerPath);
            chat.init();

//            String[] questions = getQuestions(savedComposable);
            
            String pathOriginalFragment = "./files/example-files/" + PREFIX + "Fragment.kt";
            String savedOriginalFragment = Utils.readFullFile(pathOriginalFragment);
            String[] questions = getSimpleQuestions(savedComposable, savedOriginalFragment, savedViewModel);
            
            String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                System.out.println("\n\n****************************************************************************************");
                String question = questions[i];
                System.out.println("QUESTION: " + question);
                answers[i] = chat.askQuestion(question, 3, 0.7, false);
                System.out.println("ANSWER: " + answers[i]);
            }
            System.out.println("\n\n****************************************************************************************");
        } else {
            System.out.println("ERROR outputPath " + outputPath + " MISSING");
        }
    }
    protected String[] getSimpleQuestions(String savedComposable, String savedOriginalFragment, String savedViewModel) {
        String composableFullName = "ifac.td.taxi.ui.screen." + PREFIX + "Screen";
        String fragmentFullName = "ifac.td.taxi.ui.screen." + PREFIX + "Fragment";
            String[] questions = {
            "- I have this JetPack Compose composable screen " + composableFullName + " \n" +
            savedComposable +
            "\nThe viewModel for this composable is this one:\n" +
            savedViewModel +
            "\nPlease provide a Jetpack views fragment with a ComposeView that hosts the Compose composable screen.\n" +
            "\nThe name of the fragment must be SplashScreenNavigationFragment and the package name should be ifac.td.taxi.ui.screen.navigation\n",

            };
            return questions;
    }

    private String processFileForScreen(String fullContent) {
        int numLines = 0;
        boolean startScreen = false;
        boolean endScreen = false;
        HashMap<Integer, String> numberedLines = new HashMap<>();
        HashMap<Integer, String> extractedLines = new HashMap<>();
        String lines[] = fullContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("fun " + PREFIX + "Route(")) {
                line = line.replaceAll("Route", "Screen");
            }
            numberedLines.put(numLines, line);
            numLines++;
        }
        int numLinesFinal = 0;
        for (String line : numberedLines.values()) {
            if (line.contains("fun " + PREFIX + "Screen(")) {
                startScreen = true;
                extractedLines.put(numLinesFinal, "@Composable");
                numLinesFinal++;
                extractedLines.put(numLinesFinal, line);
                numLinesFinal++;
            } else {
                if (startScreen && (line.contains("@Composable") || line.contains("class "))) {
                    startScreen = false;
                    endScreen = true;
                }
                if (startScreen && !endScreen) {
                    extractedLines.put(numLinesFinal, line);
                    numLinesFinal++;
                }
            }
        }
        StringBuilder newContent = new StringBuilder();
        for (String line : extractedLines.values()) {
            newContent.append(line).append("\n");
        }
        return newContent.toString();
    }

    private String processFileForViewModel(String fullContent) {
        int numLines = 0;
        boolean startViewModel = false;
        boolean endViewModel = false;
        HashMap<Integer, String> numberedLines = new HashMap<>();
        HashMap<Integer, String> extractedLines = new HashMap<>();
        String lines[] = fullContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("" + PREFIX + "ViewModel")) {
                line = line.replaceAll("ViewModel", "ComposeViewModel");
            }
            numberedLines.put(numLines, line);
            numLines++;
        }
        int numLinesFinal = 0;
        for (String line : numberedLines.values()) {
            if (line.contains("class " + PREFIX + "ComposeViewModel(") || line.contains("class " + PREFIX + "ViewModel(")) {
                startViewModel = true;
                extractedLines.put(numLinesFinal, line);
                numLinesFinal++;
            } else {
                if (startViewModel && (line.contains("@Composable") || line.contains("class "))) {
                    startViewModel = false;
                    endViewModel = true;
                }
                if (startViewModel && !endViewModel) {
                    extractedLines.put(numLinesFinal, line);
                    numLinesFinal++;
                }
            }
        }
        StringBuilder newContent = new StringBuilder();
        for (String line : extractedLines.values()) {
            newContent.append(line).append("\n");
        }
        return newContent.toString();
    }
    
}
