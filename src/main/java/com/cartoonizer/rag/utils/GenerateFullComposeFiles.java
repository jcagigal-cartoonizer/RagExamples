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
        private PrintWriter writer;
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
            if (line.trim().startsWith("// FOURTH ANSWER")) {
                startFound = true;
            }
            if (!startFound) {
                return;
            }
            if (line.startsWith("package ")) {
                printAlways = true;
                return;
            }
            boolean isEnd = line.trim().startsWith("## Notes") || line.trim().startsWith("## What changed");
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
            if (writer != null) {
                closeFile();
            } else {
                String answerPath = this.origen.getName();
                System.out.println("*** DEBUG!!! GenerateFullComposeFiles writer IS NULL in " + answerPath);
            }
        }

        @Override
        public void begin() {
            errorFound = false;
            printAlways = false;
        }

        private void closeFile() {
            String answerPath = this.origen.getName();
            if (outputPath != null && writer != null) {
                writer.close();
            } else {
                System.out.println("*** GenerateFullComposeFiles CLOSE FILE outputPath = " + (outputPath == null ? "NULL" : outputPath) + " writer = " + (writer == null ? "NULL" : "NOT NULL"));
            }
            outputPath = null;
            writer = null;
        }

        private void openFile() {
               try {
                    System.out.println("*** GenerateFullComposeFiles OPEN FILE orderedLines size = " + orderedLines.size() + " " + outputPath);
                    if (writer != null) {
                        writer.close();
                    }
                    writer = new PrintWriter(outputPath);
                    print("package " + packageName);
                } catch (Exception ex) {
                    System.out.println("*** EXCEPTION openFile " + outputPath + ": " + ex);
                    writer = null;
                }
        }
        private int numLines = 0;

        private void print(String line) {
            if (printAlways) {
                numLines++;
                orderedLines.add(line);
                lines.put(line, line);
                try {
                    if (writer == null) {
                        openFile();
                    }
                    writer.println(line);
                } catch (Exception e) {
                }
            }
        }
        private boolean errorFound = false;
    }
}
