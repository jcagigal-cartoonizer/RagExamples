package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUT;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.LAYOUTS;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIX;
import static com.cartoonizer.rag.openai.SimpleExampleOpenAi.PREFIXES;
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
            if (!PREFIX.equals("InfoDispatch")) {
                continue;
            }
            String processedAnswerPath = "./processed-files/processed-" + PREFIX + "Fragment.txt";
//            System.out.println("GenerateFiles from " + processedAnswerPath);
            GenerateFiles reader = new GenerateFiles(processedAnswerPath, PREFIX);
            reader.load();
        }
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

        public GenerateFiles(String processedAnswerPath, String prefix) {
            super(processedAnswerPath);
        }
        public HashMap<String, String> imports = new HashMap<>();
        public HashMap<String, String> lines = new HashMap<>();
        public ArrayList<String> orderedLines = new ArrayList<>();
        public boolean doPrint = true;

        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("```")) {
                return;
            }
            if (line.trim().startsWith("---")) {
                return;
            }
            if (iface == null) {
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
            line = processBlocks(iface, line);
//            System.out.println("*** currentFile = " + currentFile);
//            System.out.println("=== outputPath = " + outputPath);
            if (printAlways) {
//                    System.out.println(line);
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
                if (line.contains("CustomDialog(")) {
                    line = line.replaceAll(Pattern.quote("CustomDialog"), PREFIX + "CustomDialog");
                }
                if (line.contains(PREFIX + "Effect")) {
                    line = line.replaceAll(Pattern.quote(PREFIX + "Effect"), PREFIX + "UiEffect");
                }
                if (line.trim().startsWith("fun composeButtonColors(")) {
                    line = "@Composable\n" + line;
                }
                if (outputPath.contains(PREFIX + "ComposeViewModel")) {
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
                if (outputPath.contains("DialogState.")) {
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
                if (outputPath.contains(PREFIX + "ComposeViewModel")) {
                    if (canOpenPendingTrips && line.trim().startsWith("fun changeStateHiredManual")) {
                        canOpenPendingTrips = false;
                        doPrint = true;
                    }
                }
                print(line);
                if (outputPath.contains("DialogState.") && line.trim().equals("}")) {
                    doPrint = true;
                    insideDialogStateLet = false;
                }
            }

// End processing
            if (line.trim().contains(iface.getEndTag())) {
                printAlways = false;
            }
        }

        @Override
        public void end() {
            super.end();
            if (writer != null) {
                closeFile();
            }
        }

        private void closeFile() {
            System.out.println("*** closeFile outputPath " + outputPath + " writer " + (writer == null ? " IS NULL" : " NOT NULL"));
            if (secondPass != null && writer != null) {
                secondPass.printAll(lines, orderedLines);
            }
            if (writer != null) {
                writer.close();
            }
        }

        private void printOtherImports(String path) {
            print("import androidx.compose.runtime.getValue");
            print("import androidx.compose.runtime.setValue");
            print("import androidx.compose.runtime.mutableStateOf");
            print("import androidx.compose.runtime.remember");
            print("import ifac.td.taxi.R");
            if (path.contains("ViewModel")) {
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiEvent");
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiEffect");
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiState");
                print("import ifac.td.taxi.domain.usecase.PendingTripsUseCaseImpl");
                print("import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip");
                if (PREFIX.equals("Dashboard")) {
                    print("import ifac.td.taxi.ui.screen.state.DashboardDialogState");
                    print("import ifac.td.taxi.ui.screen.state.DashboardButtonsState");
                } else {
//                    print("import ifac.td.taxi.ui.screen.state.ComposeButtonState");
//                    print("import ifac.td.taxi.ui.screen.state.MessageUiState");
                }
            }
            if (path.endsWith("Screen.kt")) {
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiEffect");
                print("import ifac.td.taxi.compose.viewmodel." + PREFIX + "ComposeViewModel");
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiEvent");
                print("import ifac.td.taxi.ui.screen.state." + PREFIX + "UiState");
                print("import ifac.td.taxi.ui.screen.state.ActionButtonState");
                print("import ifac.td.taxi.ui.screen.state.ButtonBackground");
                if (path.contains("Dashboard")) {
                    print("import ifac.td.taxi.ui.screen.state." + PREFIX + "DialogState");
                    print("import ifac.td.taxi.ui.screen.state." + PREFIX + "HeaderState");
                    print("import ifac.td.taxi.ui.screen.state." + PREFIX + "ButtonsState");
                    print("import ifac.td.taxi.ui.screen.state." + PREFIX + "ButtonConfig");
                }
            }
            if (path.contains("DialogState")) {
                print("import ifac.td.taxi.compose.viewmodel." + PREFIX + "ComposeViewModel");
                print("import ifac.td.taxi.ui.screen." + PREFIX + "Screen");
                print("import ifac.td.taxi.ui.screen.ComposeButtonBackground");
                print("import ifac.td.taxi.ui.screen.ComposeButtonConfig");
            }
            if (path.contains("CustomDialog")) {
                print("import androidx.compose.ui.window.Dialog");
            }
                
        }

        private void openFile(String path, String packageName) {
            System.out.println("*** openFile " + path);
            if (path != null && !path.isEmpty()) {
                try {
                    outputPath = path;
                    if (writer != null) {
                        closeFile();
                    }
                    writer = new PrintWriter(outputPath);
                    secondPass = new SecondPass(writer, outputPath);
                    lines.clear();
                    orderedLines.clear();
                    print("package " + packageName);
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
                } catch (Exception ex) {
                    System.out.println("*** EXCEPTION openFile " + path + ": " + ex);
                    writer = null;
                }
            }
        }
        private int numLines = 0;
        private void print(String line) {
//            System.out.println("*** doPrint = " + doPrint + " " + line);
            if (doPrint) {
                numLines++;
                orderedLines.add(line);
                lines.put(line, line);
            }
        }

        private String processBlocks(IGeneralBlocks iface, String line) {
            for (int i = 0; i < iface.getBLOCKS().length; i++) {
                if (line.trim().startsWith("This ") || line.trim().startsWith("You ") || line.trim().startsWith("Single ") || 
                        line.contains("Note: ") || line.trim().startsWith("- ") || line.trim().startsWith("Only one ")) {
                    line = "// " + line;
                }
                if (line.trim().contains(iface.getBLOCKS()[i])) {
                    line = "// " + line;
                    printAlways = true;
                    if (outputPath != null && !outputPath.isEmpty()) {
                        closeFile();
                    }
                    outputPath = iface.getFILES()[i];
                    System.out.println("*** processBlocks call openFile " + outputPath + " block = " + iface.getBLOCKS()[i]);
                    openFile(outputPath, iface.getPACKAGES()[i]);
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
        private SecondPass() {
            
        }
        public SecondPass(PrintWriter writer, String outputPath) {
            this.writer = writer;
            this.outputPath = outputPath;
        }
        private void print(String line) {
            // print in second pass
                if (writer != null) {
                    writer.println(line);
                    writer.flush();
                } else {
                    System.out.println("*** print writer IS NULL " );
                }
        }

        private void addImportForInternalVars(HashMap<String, String> lines, ArrayList<String> orderedLines) {
//            System.out.println("==> addImportForInternalVars " + outputPath + " orderedLines  " + orderedLines.size());
            for (String line : orderedLines) {
                if (line.contains(PREFIX + "ComposeViewModel") && !line.startsWith("import ") && !outputPath.contains("ComposeViewModel")) {
                        String lineImport = "import ifac.td.taxi.compose.viewmodel." + PREFIX + "ComposeViewModel";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                } else if (line.contains(PREFIX + "CustomDialog") && !line.startsWith("import ")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.components." + PREFIX + "CustomDialog";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                } else if (line.contains(PREFIX + "Screen") && !line.startsWith("import ")) {
                        String lineImport = "import ifac.td.taxi.ui.screen." + PREFIX + "Screen";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                } else if ((line.contains(PREFIX + "DialogState") || line.contains(PREFIX + "DialogType") || line.contains(PREFIX + "ButtonsState") || line.contains(PREFIX + "Buttons")) &&
                        !line.startsWith("import ")) {
                    if (line.contains(PREFIX + "DialogState")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "DialogState";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "DialogType")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "DialogType";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "ButtonsState")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "ButtonsState";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                    } else if (line.contains(PREFIX + "Buttons")) {
                        String lineImport = "import ifac.td.taxi.ui.screen.state." + PREFIX + "Buttons";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                        internalImports.put(lineImport, lineImport);
                    }
                }
            }
        }

        private void printAll(HashMap<String, String> lines, ArrayList<String> orderedLines) {
            addImportForInternalVars(lines, orderedLines);
//            System.out.println("*** printAll " + outputPath + " orderedLines  " + orderedLines.size());
            for (String line : orderedLines) {
                if (line.trim().startsWith("import ")) {
                    String val = internalImports.get(line);
                    String added = addedImports.get(line);
                    if (val == null && added == null) {
                        print(line);
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
                    if (line.startsWith("package ")) {
                        for (Entry<String, String> entry : internalImports.entrySet()) {
                            print(entry.getKey());
                        }
                    }
                }
            }
        }
    }
}
