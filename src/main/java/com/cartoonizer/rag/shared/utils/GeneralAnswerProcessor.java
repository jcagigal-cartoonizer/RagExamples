package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUT;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUTS;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIXES;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GeneralAnswerProcessor extends ReadFile {
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (!PREFIX.equals(ONLY_THIS)) {
                continue;
            }
//            FragmentAnswerProcessor.processAll();
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            ReadFile reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            reader.load();
        }
    }

    public static String ONLY_THIS = "LoginUser";
    public static String FILE_NAME = PREFIX + "Fragment.txt";
    public static IGeneralBlocks iface;
    private boolean printAlways;
    private String processedAnswerPath;
    private PrintWriter writer;
    public HashMap<String, String> blockFiles = new HashMap<>();
    public HashMap<String, String> blocks = new HashMap<>();
    public String[] blockFilesList = new String[0];
    public String[] packagesArray = new String[0];
    public String[] blocksList = new String[0];
    public String[] packagesList = new String[0];
    private HashMap<String, String> importsMap = new HashMap<>();
    private HashMap<String, String> lines = new HashMap<>();
    private HashMap<Integer, String> orderedLines = new HashMap<>();
    private int numBlock = 0;
    private int numLines = 0;
    private boolean firstImport = false;
    private boolean hasImports = true;
    private String fileNameToUse = "";
    private String previousLine = "";
    private String blockLine = "";

    public GeneralAnswerProcessor(String pathOrigen, String processedAnswerPath) {
        super(pathOrigen);
        this.processedAnswerPath = processedAnswerPath;
        if (processedAnswerPath != null && !processedAnswerPath.isEmpty()) {
            try {
                writer = new PrintWriter(processedAnswerPath);
            } catch (Exception ex) {
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
        numBlock = 0;
        numLines = 0;
        hasImports = true;
        importsMap = new HashMap<>();
        lines = new HashMap<>();
        blocks = new HashMap<>();
        orderedLines = new HashMap<>();
        blockFiles = new HashMap<>();
        firstImport = false;
        fileNameToUse = "";
        blockLine = "";
    }

    public HashMap<String, String> getBlocks() {
        return blocks;
    }

    public HashMap<String, String> getBlockFiles() {
        return blockFiles;
    }

    @Override
    public void processLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (line.trim().startsWith("package ")) {
            fileNameToUse = "";
        }
        if (line.trim().startsWith("- ") || line.trim().startsWith("```") || line.trim().startsWith("---")
                || line.trim().startsWith("package ") || line.trim().startsWith("This ") || line.trim().startsWith("Use ")) {
            return;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")
                || line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
        if (line.trim().startsWith("import ")) {
            if (line.contains("import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus")) {
                System.out.println("*** firstImport = " + firstImport + " block = " + blockLine + " importsMap.get(line) = " + importsMap.get(line));
            }
            if (!firstImport) {
                firstImport = true;
                hasImports = true;
                numBlock++;
                blockLine = "// # Block " + numBlock + ": " + line;
                System.out.println("*** blockLine " + blockLine);
                numLines++;
                orderedLines.put(numLines, blockLine);
                lines.put(blockLine, blockLine);
                blocks.put(blockLine, blockLine);
            }
            importsMap.put(line, line);
        } else {
            if (!hasImports) {
                System.out.println("*** NO IMPORTS IN " + processedAnswerPath);
                hasImports = true;
                numBlock++;
                blockLine = "// # Block " + numBlock + ": " + line;
                System.out.println("*** blockLine " + blockLine);
                numLines++;
                orderedLines.put(numLines, blockLine);
                lines.put(blockLine, blockLine);
                blocks.put(blockLine, blockLine);
            }
            if (fileNameToUse.isEmpty()) {
                if (line.contains("class ")) {
                    fileNameToUse = extractFileNameFromClass(line);
                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                } else if ("@Composable".equals(previousLine) && line.startsWith("fun ")) {
                    fileNameToUse = extractFileNameFromFun(line);
                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                } else if (line.trim().startsWith("object ")) {
                    fileNameToUse = extractFileNameFromObject(line);
                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                }
            }
            firstImport = false;
        }
        numLines++;
        orderedLines.put(numLines, line);
        lines.put(line, line);
        previousLine = line;
    }
    private void doProcessLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (line.trim().startsWith("- ") || line.trim().startsWith("```") || line.trim().startsWith("---")
                || line.trim().startsWith("package ")|| line.trim().startsWith("This ") || line.trim().startsWith("Use ")) {
            return;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")
                || line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
        if (line.contains("# ")) {
            line = "// " + line;
            printAlways = true;
        }
        if (!printAlways) {
            return;
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
//            System.out.println(line);
            if (writer != null) {
                writer.println(line);
                writer.flush();
            }
        }
    }

    @Override
    public void begin() {
        super.begin(); 
        System.out.println("*** OPEN FILE " + FILE_NAME);
    }

    @Override
    public void end() {
        super.end();
        System.out.println("*** END FILE " + FILE_NAME + " BLOCKS:");
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            System.out.println("    " + entry.getKey());
        }
        System.out.println("*** FILES:");
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            System.out.println("    " + entry.getKey() + " -> " + entry.getValue());
        }
        for (String line : orderedLines.values()) {
            doProcessLine(line);
        }
        if (writer != null) {
            writer.close();
        }
        blocksList = new String[blocks.size()];
        int idx = 0;
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            blocksList[idx] = entry.getKey();
            idx++;
        }
        blockFilesList = new String[blockFiles.size()];
        idx = 0;
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            String path = "./generated-files/ui/screen/components/";
            String fileName = entry.getValue();
            if (fileName.contains("Screen.kt")) {
                path = "./generated-files/ui/screen/";
            } else if (fileName.contains("ViewModel.kt")) {
                path = "./generated-files/compose/viewmodel/";
            }
            blockFilesList[idx] = path + fileName;
            idx++;
        }
        idx = 0;
        packagesArray = new String[blockFiles.size()];
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            if (entry.getKey().contains("Screen.kt")) {
                packagesArray[idx] = "ifac.td.taxi.ui.screen";
            } else if (entry.getKey().contains("ViewModel.kt")) {
                packagesArray[idx] = "ifac.td.taxi.compose.viewmodel";
            } else {
                packagesArray[idx] = "ifac.td.taxi.ui.screen.components";
            }
            idx++;
        }
    }

    private String extractFileNameFromClass(String line) {
        return line.trim().replaceAll("data class ", "").replaceAll("class ", "").replaceAll(Pattern.quote("("), "").replaceAll(" ", "").trim() + ".kt";
    }

    private String extractFileNameFromFun(String line) {
// fun ContactCentralScreen(
        return line.replaceAll("fun ", "").replaceAll(Pattern.quote("("), "").replaceAll(" ", "").trim() + ".kt";
    }
    private String extractFileNameFromObject(String line) {
// object ComposeCustomButtonDefaults
        return line.replaceAll("object ", "").replaceAll(Pattern.quote("{"), "").replaceAll(" ", "").trim() + ".kt";
    }
}
