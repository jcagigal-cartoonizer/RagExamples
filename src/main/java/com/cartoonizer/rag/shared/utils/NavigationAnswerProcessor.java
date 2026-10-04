package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.shouldSkip;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NavigationAnswerProcessor extends ReadFile {
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (shouldSkip(i)) {
                continue;
            }
//            FragmentAnswerProcessor.processAll();
            FILE_NAME = PREFIX + "NavigationFragment.txt";
            String answerPath = "./output-files/navigation/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/navigation/processed-" + FILE_NAME;
            ReadFile reader = new NavigationAnswerProcessor(answerPath, processedAnswerPath);
            reader.load();
        }
    }

    private boolean printAlways;
    private String processedAnswerPath;
    private PrintWriter writer;
    public HashMap<String, String> blockFiles = new HashMap<>();
    public HashMap<String, String> blocks = new HashMap<>();
    public String[] blockFilesList = new String[0];
    public String[] blockFileNamesList = new String[0];
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
    public NavigationAnswerProcessor(String pathOrigen, String processedAnswerPath) {
        super(pathOrigen);
        System.out.println("*** NavigationAnswerProcessor pathOrigen = " + pathOrigen);
        this.processedAnswerPath = processedAnswerPath;
        if (processedAnswerPath != null && !processedAnswerPath.isEmpty()) {
            try {
                writer = new PrintWriter(processedAnswerPath);
            } catch (Exception ex) {
            }
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
    public static boolean shouldIgnore(String line) {
        line = line.trim();
        return (line.startsWith("These ") || line.startsWith("This ") || line.startsWith("You ") || line.startsWith("If you") || 
                line.startsWith("with ") || line.startsWith("Single ") || line.contains("Note: ") || line.startsWith("- ") || 
                line.startsWith("Only one ") || line.startsWith("Use ") || line.startsWith("It keeps") || 
                line.startsWith("One flow") || line.startsWith("It’s ") || line.startsWith("Helper") ||
                line.startsWith("Since ") || line.startsWith("The screen") || line.startsWith("Because ") || line.startsWith("Example ") ||
                line.startsWith("For navigation") || line.startsWith("import ifac.td.taxi.ui.screen." + PREFIX.toLowerCase()) ||
                line.startsWith("Below is a") || line.startsWith("I’m ") || line.startsWith("It ") || line.startsWith("And ") ||
                line.startsWith("The navigation callback") || line.startsWith("| ") || line.startsWith("The ") || line.startsWith("Using "));

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
            printAlways = true;
//            return;
        }
        if (line.trim().startsWith("import ")) {
            printAlways = true;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")) {
            line = line.replaceAll(Pattern.quote("ifac.td.taxi.ui.screen.state.ComposeButtonState"), "ifac.td.taxi.ui.screen.components." + PREFIX + "ComposeButtonState");
        }
        if (line.contains("ComposeButtonState") && !line.contains(PREFIX + "ComposeButtonState")) {
            line = line.replaceAll(Pattern.quote("ComposeButtonState"), PREFIX + "ComposeButtonState");
        }
        if (line.trim().startsWith("import ")) {
            if (!firstImport) {
                firstImport = true;
                hasImports = true;
                numBlock++;
                blockLine = "// # Block " + numLines + "-" + numBlock + ": " + line;
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
                blockLine = "// # Block " + numLines + "-" + numBlock + ": " + line;
                System.out.println("*** blockLine " + blockLine);
                numLines++;
                orderedLines.put(numLines, blockLine);
                lines.put(blockLine, blockLine);
                blocks.put(blockLine, blockLine);
            }
                if (fileNameToUse.isEmpty()) {
                    if (line.contains("class ")) {
                        fileNameToUse = extractFileNameFromClass(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                        blockFiles.put(blockLine, fileNameToUse);
                    } else if ("@Composable".equals(previousLine) && line.startsWith("fun ")) {
                        fileNameToUse = extractFileNameFromFun(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                        blockFiles.put(blockLine, fileNameToUse);
                    } else if (line.trim().startsWith("object ")) {
                        fileNameToUse = extractFileNameFromObject(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                        blockFiles.put(blockLine, fileNameToUse);
                    } else if (line.trim().startsWith("fun ")) {
                        fileNameToUse = extractFileNameFromFun(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                        blockFiles.put(blockLine, fileNameToUse);
                    } else {
//                        System.out.println("*** fileNameToUse isEmpty but no name found blockLine = " + blockLine);
                    }
                }
                if(!fileNameToUse.isEmpty() && !fileNameToUse.startsWith(PREFIX)) {
                    fileNameToUse = PREFIX + fileNameToUse;
                }
            firstImport = false;
        }
        if (!shouldIgnore(line)) {
            numLines++;
            orderedLines.put(numLines, line);
            lines.put(line, line);
        }
        previousLine = line;
    }
    private void doProcessLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (shouldIgnore(line)) {
            return;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")
                || line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
        if (line.trim().startsWith("import ") || line.trim().startsWith("#")) {
            printAlways = true;
        }
        if (line.trim().startsWith("// ## Important notes") || line.trim().startsWith("## Navigation XML change")) {
            printAlways = false;
        }
//        if (!printAlways) {
//            return;
//        }
        String fileNameToUseNoExtension = fileNameToUse.replaceAll(Pattern.quote(".kt"), "");

        if (line.contains("private fun ")) {
            line = line.replaceAll(Pattern.quote("private fun "), "fun ");
        }
        if (line.contains("private data class ")) {
            line = line.replaceAll(Pattern.quote("private "), "");
        }
        if (line.contains(PREFIX + PREFIX)) {
            line = line.replaceAll(Pattern.quote(PREFIX + PREFIX), PREFIX);
        }
        if (printAlways) {
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
        System.out.println("*** NavigationAnswerProcessor OPEN FILE " + FILE_NAME + " -> " + processedAnswerPath);
    }

    @Override
    public void end() {
        super.end();
//        System.out.println("*** END FILE " + FILE_NAME + " BLOCKS:");
//        for (Map.Entry<String, String> entry : blocks.entrySet()) {
//            System.out.println("    " + entry.getKey());
//        }
//        System.out.println("*** FILES:");
//        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
//            System.out.println("    " + entry.getKey() + " -> " + entry.getValue());
//        }
        for (String line : orderedLines.values()) {
            doProcessLine(line);
        }
        if (writer != null) {
            writer.close();
        }
        System.out.println("*** NavigationAnswerProcessor CLOSE FILE " + FILE_NAME + " lines = " + orderedLines.size() + " -> " + processedAnswerPath);
        if (orderedLines.isEmpty()) {
            System.out.println("======> NavigationAnswerProcessor CLOSE FILE " + FILE_NAME + " NO LINES!!!");
        }
        blocksList = new String[blocks.size()];
        int idx = 0;
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            blocksList[idx] = entry.getKey();
            idx++;
        }
        blockFileNamesList = new String[blocks.size()];
        idx = 0;
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            blockFileNamesList[idx] = entry.getValue();
            idx++;
        }
        blockFilesList = new String[blockFiles.size()];
        idx = 0;
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            String path = "./generated-files/compose/navigation/";
            String fileName = entry.getValue();
            fileName = normalizeFileName(fileName);
            blockFilesList[idx] = path + fileName;
            idx++;
        }
        idx = 0;
        packagesArray = new String[blockFiles.size()];
        String thePackage = "ifac.td.taxi.compose.navigation";
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            packagesArray[idx] = thePackage;
            idx++;
        }
        for (int i = 0; i < blocksList.length; i++) {
            String block = blocksList[i];
            String fileName = blockFileNamesList[i];
            
        }
    }

    private String extractFileNameFromClass(String line) {
        String className = line.trim().replaceAll("data class ", "").replaceAll("enum class ", "").replaceAll("class ", "").replaceAll(" ", "").trim(); 
        int idx = className.indexOf(":");
        if (idx < 0) {
            idx = className.indexOf("(");
        }
        if (idx > 0) {
            className = className.substring(0, idx);
        }
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        return className + ".kt";
    }

    private String extractFileNameFromFun(String line) {
// fun MainActivityViewModel.homeExternalStateFlow(): Flow<HomeExternalState> {
        String funName = line.trim().replaceAll("fun ", "").replaceAll(" ", "").trim(); 
//        System.out.println("extractFileNameFromFun BEFORE " + funName);
        int idx = funName.indexOf("(");
        if (idx > 0) {
            funName = funName.substring(0, 1).toUpperCase() + funName.substring(1, idx);
            idx = funName.indexOf(".");
            if (idx > 0) {
                funName = funName.substring(idx + 1, idx + 2).toUpperCase() + funName.substring(idx + 2);
            }
        }
        funName = funName.substring(0, 1).toUpperCase() + funName.substring(1);
        return funName + ".kt";
    }
    private String extractFileNameFromObject(String line) {
// object ComposeCustomButtonDefaults
        String funName = line.replaceAll("object ", "").replaceAll(Pattern.quote("{"), "").replaceAll(" ", "").trim() + ".kt";
        funName = funName.substring(0, 1).toUpperCase() + funName.substring(1);
        return funName + ".kt";
    }

    private String normalizeFileName(String original) {
        String fileName = original.replace(':', '_').replace('(', '_').replace(')', '_').replace('{', '_').replace('}', '_');
//        System.out.println("=== fileName = " + fileName);
        fileName = fileName.replaceAll("__", "_");
        int idx = fileName.indexOf("_");
        if (idx > 0) {
            fileName = fileName.substring(0, idx);
        }
        if (fileName.equals(".kt")) {
            fileName = original;
        }
        if (!fileName.endsWith(".kt")) {
            fileName = fileName + ".kt";
        }
//        System.out.println("=== fileName normalized = " + fileName);
        return fileName;
    }
}
