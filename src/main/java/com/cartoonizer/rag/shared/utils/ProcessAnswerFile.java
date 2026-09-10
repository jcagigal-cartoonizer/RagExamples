package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.shared.utils.ReadFile;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ProcessAnswerFile {

    public static void main(String[] args) {
//        String answerPath = "./output-files/smartTDtheme.txt";
//        String processedAnswerPath = "./processed-files/processed-smartTDtheme.txt";
//        ReadFile reader = new AnswerProcessor(answerPath, processedAnswerPath);
//        reader.load();
    }

    public static class AnswerProcessor extends ReadFile {
        private boolean beginStepOne;
        private boolean beginStepTwo;
        private boolean beginStepThree;
        private boolean beginStepFour;
        private boolean beginStepFour2;
        private boolean printAlways;
        private String processedAnswerPath;
        private PrintWriter writer;
        public AnswerProcessor(String pathOrigen, String processedAnswerPath) {
            super(pathOrigen);
            this.processedAnswerPath = processedAnswerPath;
            if (processedAnswerPath != null && !processedAnswerPath.isEmpty()) {
                try {
                    writer = new PrintWriter(processedAnswerPath);
                } catch (Exception ex) {
                    writer = null;
                }
            }
        }
        private List<String> importsList = new ArrayList();
        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("import ")) {
                importsList.add(line);
            }
            if (line.trim().startsWith("```")) {
                return;
            }
            if (line.trim().startsWith("---")) {
                return;
            }
            if (line.trim().startsWith("package ")) {
                return;
            }
            if (line.trim().startsWith("##")) {
                line = "// " + line;
                printAlways = true;           
            }
            if ((line.trim().contains("## 3") || line.trim().contains("## 4")) && !printAlways) {
                System.out.println("===> beginStepThree = " + beginStepThree + " beginStepFour = " + beginStepFour + " -> " + line);
            }
            if (line.trim().startsWith("## 1)") && !printAlways) {
                line = "// " + line;
                beginStepOne = true;           
            }
            if (line.trim().startsWith("## 2)") && !printAlways) {
                line = "// " + line;
                beginStepTwo = true;
                beginStepOne = false;
            }
            if (line.trim().startsWith("## 3)") && !printAlways) {
                line = "// " + line;
                beginStepThree = true;
                beginStepTwo = false;
                beginStepOne = false;
            }
            // ## 4)
            if (line.trim().startsWith("## 4)") && !printAlways) {
                line = "// " + line;
                beginStepFour = true;
                beginStepThree = false;
                beginStepTwo = false;
                beginStepOne = false;
            }
            if (line.trim().startsWith("### 4.2") && !printAlways) {
                line = "// " + line;
                beginStepFour2 = true;
                beginStepThree = false;
                beginStepTwo = false;
                beginStepOne = false;
                beginStepFour = false;
            }
            if (line.trim().startsWith("## 5)") && !printAlways) {
                line = "// " + line;
                beginStepThree = false;
                beginStepTwo = false;
                beginStepOne = false;
                beginStepFour = false;
                beginStepFour2 = false;
            }
            if (beginStepOne || beginStepTwo || beginStepThree || beginStepFour || beginStepFour2 || printAlways) {
                System.out.println(line);
                if (writer != null) {
                    writer.println(line);
                    writer.flush();
                }
            }
        }

        @Override
        public void end() {
            super.end(); 
            if (writer != null) {
                writer.close();
            }
        }
    }
}
