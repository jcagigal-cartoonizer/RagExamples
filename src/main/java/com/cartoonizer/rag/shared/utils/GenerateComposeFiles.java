package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.getIface;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.shouldIgnore;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class GenerateComposeFiles {

    //Revisar: solo procesa Home
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (!ONLY_THIS.isEmpty() && !PREFIX.equals(ONLY_THIS)) {
                continue;
            }
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            GeneralAnswerProcessor answerProcessor = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            answerProcessor.load();

            GenerateFiles reader = new GenerateFiles(answerProcessor, processedAnswerPath, PREFIX);
            reader.load();
        }
        System.out.println("GenerarComposeFiles FILES OPEN: " + GenerateFiles.openFiles.size());
        for (Entry<String, Integer> entry : GenerateFiles.openFiles.entrySet()) {
            System.out.println("GenerarComposeFiles OPEN " + entry.getKey() + " " + entry.getValue());
        }
        System.out.println("GenerarComposeFiles FILES CLOSED: " + GenerateFiles.closedFiles.size());
        for (Entry<String, Integer> entry : GenerateFiles.closedFiles.entrySet()) {
            System.out.println("GenerarComposeFiles CLOSED " + entry.getKey() + " " + entry.getValue());
        }
        RemoveFilesWithoutContent purge = new RemoveFilesWithoutContent(new File("./generated-files"), true);
        purge.process();
        System.out.println("*** filesWithContent = " + RemoveFilesWithoutContent.filesWithContent + " filesWithoutContent = " + RemoveFilesWithoutContent.filesWithoutContent);
    }

    public static class GenerateFiles extends ReadFile {

        private boolean printAlways;
        private String packageName;
        private PrintWriter writer;
        private SecondPass secondPass;
        private String outputPath;
        private boolean insideDialogStateLet;
        private boolean canOpenPendingTrips;
        private IGeneralBlocks iface;
        private GeneralAnswerProcessor answerProcessor;

        public GenerateFiles(GeneralAnswerProcessor answerProcessor, String processedAnswerPath, String prefix) {
            super(processedAnswerPath);
            this.answerProcessor = answerProcessor;
        }
        public HashMap<String, String> imports = new HashMap<>();
        public HashMap<String, String> lines = new HashMap<>();
        public static HashMap<String, Integer> openFiles = new HashMap<>();
        public static HashMap<String, Integer> closedFiles = new HashMap<>();
        public ArrayList<String> orderedLines = new ArrayList<>();
        public boolean doPrint = true;
        private String previousLine = "";

        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("```")) {
                return;
            }
            if (line.trim().startsWith("---")) {
                return;
            }
//            if (shouldIgnore(line)) {
//                return;
//            }
            if (iface == null) {
                iface = getIface();
            }
//            if (line.trim().startsWith("package ")) {
//                packageFound = true;
//                line = "";
//            }
//            if (packageFound) {
//                printAlways = true;
//            }
//            System.out.println("==> processLine printAlways = " + printAlways + " in " + super.origen.getName() + " " + line);
            line = processBlocks(iface, line);
            boolean isEnd = iface.isEndTag(line.trim());
            if (isEnd) {
                System.out.println("*** GenerateComposeFiles isEndTag line = " + line);
                printAlways = false;
            }
            if (!isEnd && outputPath != null) {
                doPrint = true;
                printAlways = true;
            }
//            System.out.println("*** currentFile = " + currentFile);
//            System.out.println("=== outputPath = " + outputPath);
            if (printAlways) {
// R.string. navigate -> btn_navegar
                if (line.contains("R.string.btn_notifications")) {
                    line = line.replaceAll(Pattern.quote("R.string.btn_notifications"), "R.string.btn_avisos");
                }
                if (line.contains("R.string.no_client")) {
                    line = line.replaceAll(Pattern.quote("R.string.no_client"), "R.string.no_clients");
                }
                if (line.contains("R.string.return_dispatch")) {
                    line = line.replaceAll(Pattern.quote("R.string.return_dispatch"), "R.string.btn_devolver");
                }
                if (line.contains("R.string.print")) {
                    line = line.replaceAll(Pattern.quote("R.string.print"), "R.string.btn_print");
                }
                if (line.contains("R.string.navigate")) {
                    line = line.replaceAll(Pattern.quote("R.string.navigate"), "R.string.btn_navegar");
                }
                if (line.contains("CustomDialog(") && !line.contains(PREFIX + "CustomDialog")) {
                    line = line.replaceAll(Pattern.quote("CustomDialog"), PREFIX + "CustomDialog");
                }
                if (line.contains(PREFIX + "Effect") && !line.contains(PREFIX + "Effect")) {
                    line = line.replaceAll(Pattern.quote(PREFIX + "Effect"), PREFIX + "UiEffect");
                }
                if (line.trim().startsWith("fun composeButtonColors(")) {
                    line = "@Composable\n" + line;
                }
                    if (outputPath != null && outputPath.contains(PREFIX + "ComposeViewModel")) {
                        if (line.trim().startsWith("fun canOpenPendingTrips()")) {
                            canOpenPendingTrips = true;
                        }
                        if (canOpenPendingTrips) {
                            doPrint = false;
                        }
                        if (line.contains("fun onPendingClick() = canOpenPendingTrips()")) {
                            line = line.replaceAll(Pattern.quote("fun onPendingClick() = canOpenPendingTrips()"), "fun onPendingClick() = emitNav(HomeUiEvent.OpenPendingTrips)");
                        }
                }
                if (outputPath != null && outputPath.contains("DialogState.")) {
                        if (line.contains("dialogState?.let")) {
                            insideDialogStateLet = true;
                        }
                    if (insideDialogStateLet && (line.contains("dialogState?.let") || line.contains("CustomDialog(") || 
                            line.contains("state = dialog,") || line.contains("onDismiss = { dialogState") || 
                            line.trim().equals(")") || line.trim().equals("}"))
                            ) {
                            doPrint = false;
                        }
                }
                if (outputPath != null && outputPath.contains(PREFIX + "ComposeViewModel")) {
                        if (canOpenPendingTrips && line.trim().startsWith("fun changeStateHiredManual")) {
                            canOpenPendingTrips = false;
                            doPrint = true;
                        }
                    }
                if (outputPath != null && outputPath.contains("DialogState.") && line.trim().equals("}")) {
                    doPrint = true;
                    insideDialogStateLet = false;
                }
                print(line);
            }
            previousLine = line;

// End processing
            if (isEnd) {
                printAlways = false;
                doPrint = false;
            }
        }

        @Override
        public void end() {
            super.end();
            if (writer != null) {
                closeFile();
            } else {
                String processedAnswerPath = this.answerProcessor.origen.getName();
                System.out.println("*** DEBUG!!! GenerateComposeFiles writer IS NULL in " + processedAnswerPath);
            }
        }

        @Override
        public void begin() {
            super.begin();
            System.out.println("*** " + super.origen.getName() + " BLOCKS:");
            for (String entry : answerProcessor.blocksList) {
                System.out.println("    " + entry);
            }
            System.out.println("*** FILES:");
            for (Map.Entry<String, String> entry : answerProcessor.blockFiles.entrySet()) {
                System.out.println("    " + entry.getKey() + " -> " + entry.getValue());
            }
        }

        private void closeFile() {
            String processedAnswerPath = this.answerProcessor.origen.getName();
            if (outputPath != null && writer != null) {
                if (closedFiles.get(outputPath) == null) {
                    closedFiles.put(outputPath, orderedLines.size());
                    System.out.println("*** generateComposeFile CLOSE FILE outputPath " + outputPath + " orderedLines = " + orderedLines.size());
                } else {
                    System.out.println("*** generateComposeFile CLOSE FILE outputPath " + outputPath + " ALREADY ADDED new orderedLines = " + orderedLines.size());
                }
                if (secondPass != null && writer != null) {
                    secondPass.printAll(lines, orderedLines);
                } else {
                    System.out.println("*** generateComposeFile NOT CALLING printAll outputPath " + outputPath + " orderedLines = " + orderedLines.size());
                }
                if (writer != null) {
                    writer.close();
                }
            } else {
                System.out.println("*** generateComposeFile CLOSE FILE outputPath = " + (outputPath == null ? "NULL" : outputPath) + " writer = " + (writer == null ? "NULL" : "NOT NULL"));
            }
            outputPath = null;
            writer = null;
        }

        private void printOtherImports(String path) {
            print("import androidx.compose.runtime.getValue");
            print("import androidx.compose.runtime.setValue");
            print("import androidx.compose.runtime.mutableStateOf");
            print("import androidx.compose.runtime.remember");
            print("import ifac.td.taxi.R");
//            if (path.contains("ViewModel")) {
//                print("import ifac.td.taxi.domain.usecase.PendingTripsUseCaseImpl");
//                print("import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip");
//            }
            if (path.contains("CustomDialog")) {
                print("import androidx.compose.ui.window.Dialog");
            }

        }

        private void openFile(String path, String packageName) {
            if (path != null && !path.isEmpty()) {
                try {
                    if (path.contains("CustomDialog") && !path.contains(PREFIX + "CustomDialog")) {
                        path = path.replaceAll("CustomDialog", PREFIX + "CustomDialog");
                    }
                    if (openFiles.get(path) == null) {
                        openFiles.put(path, orderedLines.size());
                    } else {
                        return;
                    }
                    outputPath = path;
                    System.out.println("*** GenerarComposeFiles OPEN FILE orderedLines size = " + orderedLines.size() + " " + path);
                    if (writer != null) {
                        writer.close();
                    }
                    writer = new PrintWriter(outputPath);
                    secondPass = new SecondPass(this, writer, outputPath);
                    lines.clear();
                    orderedLines.clear();
                    print("package " + packageName);
                    if (path.endsWith("ViewModel.kt")) {
                        print("import android.app.Application");
                    }
                    for (Map.Entry<String, String> entry : secondPass.internalImports.entrySet()) {
                        print(entry.getKey());
                    }
                    printOtherImports(path);
                    for (String imp : imports.keySet()) {
                        print(imp);
                    }
                    imports.clear();
                    this.packageName = packageName;
                    doPrint = true;
                    printAlways = true;
                } catch (Exception ex) {
                    System.out.println("*** EXCEPTION openFile " + path + ": " + ex);
                    writer = null;
                }
            }
        }
        private int numLines = 0;

        private void print(String line) {
            if (doPrint) {
                numLines++;
                orderedLines.add(line);
                lines.put(line, line);
            }
        }

        private String processBlocks(IGeneralBlocks iface, String line) {
            if (iface == null) {
                iface = getIface();
            }
            for (int i = 0; i < answerProcessor.blocksList.length; i++) {
//                if (shouldIgnore(line)) {
//                    line = "// " + line;
//                }
                if (answerProcessor.blocksList[i].trim().equals(line.trim())) {
//                    line = "// " + line;
                    if (answerProcessor.blockFilesList[i] != null && openFiles.get(answerProcessor.blockFilesList[i]) != null) {
                        continue;
                    }
                    if (outputPath != null && !outputPath.isEmpty() && !answerProcessor.blockFilesList[i].equals(outputPath)) {
                        closeFile();
                    }
                    printAlways = true;
                    doPrint = true;
                    outputPath = answerProcessor.blockFilesList[i];
                    System.out.println("*** processBlocks line = " + numLines + " call openFile " + outputPath + " block = " + answerProcessor.blocksList[i] + " line = " + line);
                    openFile(outputPath, answerProcessor.packagesArray[i]);
                    break;
                }
            }
            return line;
        }
    }
    public static class SecondPass {
        private PrintWriter writer;
        private String outputPath;
        private HashMap<String, String> addedImports = new HashMap<>();
        private HashMap<String, String> internalImports = new HashMap<>();
        private GenerateFiles generateFiles;
        public boolean packageSet = false;
        private SecondPass() {

        }
        public SecondPass(GenerateFiles generateFiles, PrintWriter writer, String outputPath) {
            this.writer = writer;
            this.outputPath = outputPath;
            this.generateFiles = generateFiles;
        }

        private void print(String line) {
            // print in second pass
            if (shouldIgnore(line) || line.trim().startsWith("#") || (line.trim().startsWith("package ") && packageSet)) {
                return;
            }
            if (line.trim().startsWith("package ")) {
                packageSet = true;
            }
            if (writer != null) {
                writer.println(line);
                writer.flush();
            } else {
                System.out.println("*** print writer IS NULL ");
            }
        }

        private void addImportForInternalVars(HashMap<String, String> lines, ArrayList<String> orderedLines) {
//            System.out.println("==> addImportForInternalVars " + outputPath + " orderedLines  " + orderedLines.size());
            for (String line : orderedLines) {
                if (line.contains(PREFIX + "ComposeViewModel") && !line.startsWith("import ") && !outputPath.contains("ComposeViewModel")) {
                    String lineImport = "import ifac.td.taxi.compose.viewmodel." + PREFIX + "ComposeViewModel";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                    // internalImports.put(lineImport, lineImport);
                } else if (line.contains(PREFIX + "CustomDialog") && !line.startsWith("import ")) {
                    String lineImport = "import ifac.td.taxi.ui.screen.components." + PREFIX + "CustomDialog";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                    // internalImports.put(lineImport, lineImport);
                } else if (line.contains(PREFIX + "Screen") && !line.startsWith("import ")) {
                    String lineImport = "import ifac.td.taxi.ui.screen." + PREFIX + "Screen";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                    // internalImports.put(lineImport, lineImport);
                } else if ((line.contains(PREFIX + "DialogState") || line.contains(PREFIX + "DialogType") || line.contains(PREFIX + "ButtonsState") || line.contains(PREFIX + "Buttons"))
                        && !line.startsWith("import ")) {
                    if (line.contains(PREFIX + "DialogState")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "DialogState";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        // internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "DialogType")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "DialogType";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        // internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "ButtonsState")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "ButtonsState";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        // internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "Buttons")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "Buttons";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        // internalImports.put(lineImport, lineImport);
                    }
                }
            }
        }

        private void printAll(HashMap<String, String> lines, ArrayList<String> orderedLines) {
//            addImportForInternalVars(lines, orderedLines);
            System.out.println("*** printAll " + outputPath + " orderedLines  " + orderedLines.size());
            int linesPrinted = 0;
            for (String line : orderedLines) {
                if (line.trim().startsWith("import ")) {
                    String val = internalImports.get(line);
                    String added = addedImports.get(line);
                    if (val == null && added == null) {
                        print(line);
                        linesPrinted++;
                        if (added == null) {
                            addedImports.put(line, line);
                        }
                    } else {
//                        System.out.println("*** printAll interrnal " + val + " added  " + added);
                        if (added == null) {
                            addedImports.put(line, line);
                        }
                    }
                } else {
                    print(line);
                    linesPrinted++;
                    if (line.startsWith("package ")) {
                        for (Entry<String, String> entry : internalImports.entrySet()) {
                            print(entry.getKey());
                            linesPrinted++;
                        }
                    }
                }
            }
            System.out.println("*** printAll END " + " linesPrinted =  " + linesPrinted + " in " + outputPath);
        }
    }
}
