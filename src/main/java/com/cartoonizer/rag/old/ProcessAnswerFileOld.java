package com.cartoonizer.rag.old;

import com.cartoonizer.rag.shared.utils.ContactCentralBlocks;
import com.cartoonizer.rag.shared.utils.DashboardBlocks;
import com.cartoonizer.rag.shared.utils.GeneralBlocks;
import com.cartoonizer.rag.shared.utils.InfoDispatchBlocks;
import com.cartoonizer.rag.shared.utils.ReadFile;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import com.cartoonizer.rag.shared.utils.IGeneralBlocks;
import com.cartoonizer.rag.shared.utils.LoginUserBlocks;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProcessAnswerFileOld {

    public static String ONLY_THIS = "InfoDispatch";
    public static String FILE_NAME = PREFIX + "Fragment.txt";
    public static IGeneralBlocks iface;

//    public static void main(String[] args) {
//        for (int i = 0; i < LAYOUTS.length; i++) {
//            LAYOUT = LAYOUTS[i];
//            PREFIX = PREFIXES[i];
//            if (!PREFIX.equals(ONLY_THIS)) {
//                continue;
//            }
//            FragmentAnswerProcessor.processAll();
//        }
//    }

    public static class FragmentAnswerProcessor {

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
                } else if ("LoginUser".equals(PREFIX)) {
                    iface = new LoginUserBlocks();
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
                if (line.trim().startsWith("- ") || line.trim().startsWith("```") || line.trim().startsWith("---")
                        || line.trim().startsWith("package ")) {
                    return;
                }
                if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState") ||
                        line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
                    return;
                }
                if (line.contains("# ")) {
                    line = "// " + line;
                    printAlways = true;
                }
                if (line.trim().contains(iface.getEndTag())) {
                    line = "// " + line;
                }
                if ((line.contains("class " + PREFIX + "ViewModel") || line.contains(PREFIX + "ViewModel,")) && !line.contains(PREFIX + "ViewModelCompose") && !line.contains(PREFIX + "ComposeViewModel")) {
                    line = line.replaceAll(Pattern.quote(PREFIX + "ViewModel"), PREFIX + "ComposeViewModel");
                } else if (line.contains(PREFIX + "ViewModelCompose")) {
                    line = line.replaceAll(Pattern.quote("ViewModelCompose"), "ComposeViewModel");
                }
                if (line.contains("private fun ")) {
                    line = line.replaceAll(Pattern.quote("private fun "), "fun ");
                }
                if (line.contains("private data class ")) {
                    line = line.replaceAll(Pattern.quote("private "), "");
                }
                if (line.contains("R.string.abrevZone")) {
                    line = line.replaceAll(Pattern.quote("R.string.abrevZone"), "R.string.abrevUbZona");
                }
                if (line.contains("R.string.abrevStop")) {
                    line = line.replaceAll(Pattern.quote("R.string.abrevStop"), "R.string.abrevUbParada");
                }
                    if (line.contains(".zone.name")) {
                        line = line.replaceAll(Pattern.quote(".zone.name"), ".zone.nombreZone");
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
}
