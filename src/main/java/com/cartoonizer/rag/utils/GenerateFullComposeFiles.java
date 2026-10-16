package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ANSWER_FOLDER;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FULL_FILES;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.shouldSkip;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import static com.cartoonizer.rag.openai.OpenAiChatWithAugmentor.PREFIXES_AUGMENTOR;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FILE_NAME;
import java.util.Map;
import java.util.regex.Pattern;

public class GenerateFullComposeFiles {

    //Revisar: solo procesa Home
    public static void main(String[] args) {
        int filesProcessed = 0;
        int totalFiles = 0;
        for (int i = 0; i < PREFIXES_AUGMENTOR.length; i++) {
            PREFIX = PREFIXES_AUGMENTOR[i];
            totalFiles++;
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = ANSWER_FOLDER + "/" + FILE_NAME;
            if (!new File(answerPath).exists()) {
                System.out.println("==> GenerateFullComposeFiles MISSING " + answerPath);
                continue;
            }
            filesProcessed++;
            GenerateFilesFromOutput reader = new GenerateFilesFromOutput(answerPath, PREFIX);
            reader.load();
        }
    }

    public static class GenerateFilesFromOutput extends ReadFile {

        private boolean printAlways;
        private String packageName;
        private String outputPath;
        private String prefix;

        public GenerateFilesFromOutput(String answerPath, String prefix) {
            super(answerPath);
            this.prefix = prefix;
            this.packageName = "ifac.td.taxi.ui.screen.compose";
            this.outputPath = GENERATED_FULL_FILES + "/ui/screen/compose/" + this.prefix + "ComposeImplementation.kt";
        }
        public HashMap<String, String> lines = new HashMap<>();
        public ArrayList<String> orderedLines = new ArrayList<>();
        private String previousLine = "";
        private boolean startFound = false;

        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("```")) {
                return;
            }
            if (line.trim().startsWith("---")) {
                return;
            }
            if (line.trim().startsWith("// SECOND ANSWER")) {
                startFound = true;
            }
            if (!startFound) {
                return;
            }
            if (line.startsWith("package ")) {
                printAlways = true;
                return;
            }
            boolean isEnd = (line.contains("## ") && line.contains("Notes")) || line.trim().startsWith("## What changed") ||
                    line.contains("// THIRD ANSWER");
            if (isEnd) {
                System.out.println("*** GenerateComposeFiles isEndTag line = " + line);
                printAlways = false;
            }
            if (printAlways) {
                print(line);
            }
            previousLine = line;
        }

        @Override
        public void end() {
            super.end();
            if (errorFound) {
                System.out.println("ERROR found in " + outputPath);

            }
            PostProcess postProcess = new PostProcess(outputPath, lines, orderedLines);
            postProcess.postProcess();
        }

        @Override
        public void begin() {
            errorFound = false;
            printAlways = false;
        }

        private int numLines = 0;

        private void print(String line) {
            if (printAlways) {
                numLines++;
                orderedLines.add(line);
                lines.put(line, line);
            }
        }
        private boolean errorFound = false;
    }

    public static class PostProcess {
// Remove duplicate imports
// Replace import androidx.compose.runtime.collectAsStateWithLifecycle with import androidx.lifecycle.compose.collectAsStateWithLifecycle  

        public HashMap<String, String> lines = new HashMap<>();
        public HashMap<Integer, String> processedLines = new HashMap<>();
        public HashMap<String, String> imports = new HashMap<>();
        public HashMap<String, String> processedImports = new HashMap<>();
        public ArrayList<String> orderedLines = new ArrayList<>();

        public String packageName;
        public String outputPath;
        private PrintWriter writer;
        int numLines;

        public PostProcess(String outputPath, HashMap<String, String> lines, ArrayList<String> orderedLines) {
            this.lines = lines;
            this.orderedLines = orderedLines;
            this.outputPath = outputPath;
            this.packageName = "ifac.td.taxi.ui.screen.compose";
        }

        public void postProcess() {
            for (String line : lines.values()) {
                if (line.trim().startsWith("import ")) {
                    imports.put(line, line);
                }
            }
            processedLines = new HashMap<>();
            numLines = 0;
            processedLines.put(numLines, "package " + packageName);
            numLines++;
            for (String line : imports.values()) {
                if (line.contains("import androidx.compose.runtime.collectAsStateWithLifecycle")) {
                    line = line.replaceAll(Pattern.quote("import androidx.compose.runtime.collectAsStateWithLifecycle"), "import androidx.lifecycle.compose.collectAsStateWithLifecycle");
                }
                processedLines.put(numLines, line);
                numLines++;
            }
            /*
            No copiar entre
            // Shared activity VM adapter (replace placeholder with real VM)
            i
            // UI state / events / effects

            */
            boolean doCopy = true;
            for (String line : orderedLines) {
                if (line == null || line.trim().equals("null") || line.startsWith("package ")
                        || line.trim().startsWith("package ") || line.trim().startsWith("import ")) {
                    continue;
                }
                // data class -> @Composable fun ChangeUserPasswordButtonsState + ChangeUserPasswordUiState + add body
                if (line.contains("ButtonUiState") && !line.contains(PREFIX + "ButtonUiState")) {
                    line = line.replaceAll("ButtonUiState", PREFIX + "ButtonUiState");
//                } else if (line.contains("CustomButton") && !line.contains(PREFIX + "CustomButton") &&
//                        !line.contains("CustomButton.StyleButton")) {
//                    line = line.replaceAll("CustomButton", PREFIX + "CustomButton");
//                    System.out.println("*** CustomButton line = " + line);
                } else if (line.contains("sharedVm.showHeader") || line.contains("sharedVm.showToast")) {
                    line = "// " + line;
                } else if (line.contains("navigate(")) {
                    line = line.replaceAll(Pattern.quote("navigate("), "navigateTo(");
                } else if (line.contains("openDialog(dialog, callback, fragmentManager)")) {
                    line = line.replaceAll(Pattern.quote(", fragmentManager"), "");
                } else if (line.contains("context.getString(")) {
//                    line = line.replaceAll(Pattern.quote("context.getString("), "stringResource(");
                } else if (line.contains("private val ")) {
                    line = line.replaceAll(Pattern.quote("private "), "");
                }
                if (line.contains("// Shared activity VM adapter")) {
                    doCopy = false;
                }
                if (line.contains("// UI state / events / effects")) {
                    doCopy = true;
                }
                if (line.contains("### ") && line.contains("otes")) {
                    doCopy = false;
                }
                if (doCopy) {
                    processedLines.put(numLines, line);
                    numLines++;
                }
            }
            for (Map.Entry<Integer, String> entry : processedLines.entrySet()) {
                int num = entry.getKey();
                String line = entry.getValue();
//                System.out.println("*** " + num + " -> " + line);
                print(line);
            }
            closeFile();
        }

        private void print(String line) {
            try {
                if (writer == null) {
                    openFile();
                }
                writer.println(line);
            } catch (Exception e) {
            }
        }

        private void openFile() {
            try {
//                System.out.println("*** GenerateFullComposeFiles OPEN FILE lines size = " + lines.size() + " " + outputPath);
                if (writer != null) {
                    writer.close();
                }
                writer = new PrintWriter(outputPath);
            } catch (Exception ex) {
                System.out.println("*** EXCEPTION openFile " + outputPath + ": " + ex);
                writer = null;
            }
        }

        private void closeFile() {
            if (outputPath != null && writer != null) {
                writer.close();
            } else {
                System.out.println("*** GenerateFullComposeFiles CLOSE FILE outputPath = " + (outputPath == null ? "NULL" : outputPath) + " writer = " + (writer == null ? "NULL" : "NOT NULL"));
            }
            outputPath = null;
            writer = null;
        }
    }
}
