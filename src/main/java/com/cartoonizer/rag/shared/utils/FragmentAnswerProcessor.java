package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import static com.cartoonizer.rag.shared.utils.ProcessAnswerFile.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.ProcessAnswerFile.iface;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FragmentAnswerProcessor {
        public static void processAll() {
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            ReadFile reader = new AnswerProcessor(answerPath, processedAnswerPath);
            reader.load();
        }
    public static class AnswerProcessor extends ReadFile {
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
            if ("InfoDispatch".equals(PREFIX)) {
                iface = new InfoDispatchBlocks();
            } else if ("ContactCentral".equals(PREFIX)) {
                iface = new ContactCentralBlocks();
            } else if ("Dashboard".equals(PREFIX)) {
                iface = new DashboardBlocks();
            } else {
                iface = new GeneralBlocks();
            }
        }
        private List<String> importsList = new ArrayList();

        @Override
        public void processLine(String line) {
            if (iface == null) {
            }
            if (line.trim().startsWith("import ")) {
                importsList.add(line);
            }
            if (line.trim().startsWith("- ") || line.trim().startsWith("```")|| line.trim().startsWith("---") ||
                    line.trim().startsWith("package ")) {
                return;
            }
            if (line.contains("# ")) {
                line = "// " + line;
                printAlways = true;
            }
            if (line.trim().contains(iface.getEndTag())) {
                line = "// " + line;
            }
            if (line.contains("ViewModelCompose")) {
                line = line.replaceAll(Pattern.quote("ViewModelCompose"), "ComposeViewModel");
            }
            if (printAlways) {
                if (line.contains("ifac.td.taxi.ui.screen.compose.dialog.")) {
                    line = line.replaceAll(Pattern.quote("ifac.td.taxi.ui.screen.compose.dialog."), "ifac.td.taxi.ui.screen.state.");
                } else if (line.contains("ifac.td.taxi.ui.screen.compose.components.")) {
                    line = line.replaceAll(Pattern.quote("ifac.td.taxi.ui.screen.compose.components."), "ifac.td.taxi.ui.screen.components.");
                } else if (line.contains("ifac.td.taxi.viewmodel.compose.")) {
                    if (line.contains("ViewModel")) {
                        line = line.replaceAll(Pattern.quote("ifac.td.taxi.viewmodel.compose."), "ifac.td.taxi.compose.viewmodel.");
                    } else {
                        line = line.replaceAll(Pattern.quote("ifac.td.taxi.viewmodel.compose."), "ifac.td.taxi.ui.screen.state.");
                    }
                }
                if (line.contains(".zone.name")) {
                    line = line.replaceAll(Pattern.quote(".zone.name"), ".zone.nombreZone");
                }
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
