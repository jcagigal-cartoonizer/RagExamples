package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.getIface;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.shouldIgnore;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.shouldSkip;
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
        int filesProcessed = 0;
        int totalFiles = 0;
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            totalFiles++;
            if (shouldSkip(i)) {
                continue;
            }
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            if (!new File(answerPath).exists() || !new File(processedAnswerPath).exists()) {
                System.out.println("==> GenerateNavigationFiles MISSING " + answerPath + " or " + processedAnswerPath);
                continue;
            }
            filesProcessed++;
            GeneralAnswerProcessor answerProcessor = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            answerProcessor.load();

            GenerateFiles reader = new GenerateFiles(answerProcessor, processedAnswerPath, PREFIX);
            reader.load();
        }
        System.out.println("GenerateComposeFiles FILES OPEN: " + GenerateFiles.openFiles.size());
        for (Entry<String, Integer> entry : GenerateFiles.openFiles.entrySet()) {
            System.out.println("GenerateComposeFiles OPEN " + entry.getKey() + " " + entry.getValue());
        }
        System.out.println("GenerateComposeFiles FILES CLOSED: " + GenerateFiles.closedFiles.size());
        for (Entry<String, Integer> entry : GenerateFiles.closedFiles.entrySet()) {
            System.out.println("GenerateComposeFiles CLOSED " + entry.getKey() + " " + entry.getValue());
        }
        RemoveFilesWithoutContent purge = new RemoveFilesWithoutContent(new File("./generated-files"), true);
        purge.process();
        System.out.println("*** GenerateComposeFiles filesWithContent = " + RemoveFilesWithoutContent.filesWithContent + " filesWithoutContent = " + RemoveFilesWithoutContent.filesWithoutContent);
        System.out.println("*** fGenerateComposeFiles ilesProcessed = " + filesProcessed + " of " + totalFiles);
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
                    if (insideDialogStateLet && (line.contains("dialogState?.let") || line.contains("CustomDialog(")
                            || line.contains("state = dialog,") || line.contains("onDismiss = { dialogState")
                            || line.trim().equals(")") || line.trim().equals("}"))) {
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
            if (errorFound) {
                System.out.println("ERROR found in " + outputPath + " blocks " + answerProcessor.blocks.size() + " blockFiles " + answerProcessor.blockFiles.size());

            }
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
            errorFound = false;
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
            print("import  ifac.td.taxi.R");
            print("import ifac.td.taxi.repository.connections.service.model.*");
            print("import androidx.annotation.StringRes");
            print("import androidx.compose.material3.*");
            print("import androidx.compose.runtime.*");
            print("import androidx.compose.ui.res.stringResource");
            print("import ifac.td.taxi.compose.viewmodel.*");
            print("import androidx.navigation.NavController");
            print("import ifac.td.taxi.viewmodel.MainActivityViewModel");
            print("import androidx.compose.foundation.*");
            print("import androidx.compose.foundation.interaction.*");
            print("import androidx.compose.foundation.layout.*");
            print("import androidx.compose.foundation.shape.*");
            print("import androidx.compose.ui.*");
            print("import androidx.compose.ui.draw.*");
            print("import androidx.compose.ui.graphics.*");
            print("import androidx.compose.ui.text.style.*");
            print("import androidx.compose.ui.unit.*");
            print("import androidx.lifecycle.compose.LocalLifecycleOwner");
            print("import androidx.compose.ui.platform.*");
            print("import androidx.core.net.toUri");
            print("import androidx.navigation.*");
            print("import ifac.td.taxi.ui.screen.components.*");
            print("import androidx.compose.ui.window.Dialog");
            print("import androidx.lifecycle.*");
            print("import com.interfacom.sdk.taximeter.bravocomm.*");
            print("import ifac.td.taxi.domain.model.*");
            print("import ifac.td.taxi.domain.usecase.*");
            print("import ifac.td.taxi.framework.sdk.bravocentral.usecase.*");
            print("import ifac.td.taxi.framework.sdk.usecase.*");
            print("import ifac.td.taxi.repository.room.entities.*");
            print("import ifac.td.taxi.repository.room.entities.countdown.*");
            print("import ifac.td.taxi.repository.room.entities.message.*");
            print("import ifac.td.taxi.viewmodel.model.*");
            print("import kotlinx.coroutines.*");
            print("import kotlinx.coroutines.flow.*");
            print("import androidx.lifecycle.compose.collectAsStateWithLifecycle");
            if (path.contains("CustomDialog")) {
                print("import  androidx.compose.ui.window.Dialog");
            }
            print("import ifac.td.taxi.ui.custom.button.ButtonType");
            print("import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView");
            print("import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule");
            print("import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter");
            print("import androidx.compose.ui.text.input.PasswordVisualTransformation");
            print("import android.app.Application");
            print("import android.content.Intent");
            print("import android.content.Context");
            print("import android.content.ActivityNotFoundException");
            print("import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule");
            print("import android.media.ToneGenerator");
            print("import androidx.compose.ui.text.input.KeyboardType");
            print("import androidx.compose.foundation.text.KeyboardOptions");
            print("import androidx.annotation.RawRes");
            print("import android.net.Uri");
            print("import ifac.td.taxi.viewmodel.BaseViewModel");
            print("import ifac.td.taxi.framework.util.Logs");
            print("import android.provider.Settings");
            print("import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase");
            print("import ifac.td.taxi.framework.sdk.ExternalBridgeInterface");
            print("import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable");
            print("import android.widget.Toast");
            print("import ifac.td.taxi.ui.screen." + PREFIX + "Screen");
        }

        private void openFile(String path, String packageName) {
            if (path != null && !path.isEmpty()) {
                if (path.contains(PREFIX + "ViewModel")) {
                    path = path.replaceAll(PREFIX + "ViewModel", PREFIX + "ComposeViewModel");
                }
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
                        print("import  android.app.Application");
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
        private boolean errorFound = false;

        private String processBlocks(IGeneralBlocks iface, String line) {
            if (iface == null) {
                iface = getIface();
            }
            if (answerProcessor.blockFilesList.length == answerProcessor.blocksList.length) {
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
            } else {
                errorFound = true;
//                System.out.println("ERROR: GenerateComposeFiles processBlocks " + answerProcessor.origen.getName() + " -> blocksList.length " + answerProcessor.blocksList.length + " != " + answerProcessor.blockFilesList.length);
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
            for (String original : orderedLines) {
                String line = original.trim();
                if (line.startsWith("import ") && line.contains("ComposeViewModel") && !generateFiles.outputPath.contains("ComposeViewModel")) {
                    line = "// " + line;
                }
                if (line.contains(".kt")) {
                    continue;
                }
                if (line.startsWith("import ") || line.startsWith("//")) {
                    continue;
                }
                if (line.contains(PREFIX + "Ui") || line.contains(PREFIX + "Button") || line.contains(PREFIX + "Ui")
                        || line.contains(PREFIX + "Custom") || line.contains(PREFIX + "Screen")) {
                    int idx = line.indexOf(PREFIX);
                    if (idx > 0) {
                        String name = line.substring(idx);
//                            System.out.println("GenerateComposeFiles addImportForInternalVars BEFORE name = " + name);
//                        if (!name.contains(".")) {
                        name = name.replaceAll("`", "");
                        idx = name.indexOf("(");
                        if (idx < 0) {
                            idx = name.indexOf(")");
                            if (idx < 0) {
                                idx = name.indexOf("?");
                                if (idx < 0) {
                                    idx = name.indexOf(">");
                                    if (idx < 0) {
                                        idx = name.indexOf("<");
                                    }
                                }
                            }
//                            }
                        }
                        if (idx > 0) {
                            name = name.substring(0, idx).replaceAll(">", "").replaceAll(Pattern.quote(")"), "");
                            idx = name.indexOf(".");
                            if (idx > 0) {
                                name = name.substring(0, idx);
                                idx = name.indexOf(" ");
                                if (idx > 0) {
                                    name = name.substring(0, idx);
                                }
                            }
                            String lineImport = "import ifac.td.taxi.ui.screen.components." + name;
//                            System.out.println("GenerateComposeFiles addImportForInternalVars AFTER lineImport = " + lineImport);
                            internalImports.put(lineImport, lineImport);
                        }
                    }
                }
                if ((line.contains(PREFIX + "ComposeViewModel") && !generateFiles.outputPath.contains("ViewModel"))) {
                    String lineImport = "import ifac.td.taxi.compose.viewmodel." + PREFIX + "ComposeViewModel";
//                        System.out.println("addImport --> " + lineImport + " in\n    " + outputPath);
                    internalImports.put(lineImport, lineImport);
                }
            }
        }

        private void printAll(HashMap<String, String> lines, ArrayList<String> orderedLines) {
//            addImportForInternalVars(lines, orderedLines);
            System.out.println("*** printAll " + outputPath + " orderedLines  " + orderedLines.size());
            int linesPrinted = 0;
            boolean importsFound = false;
            boolean importsEnded = false;
            for (String line : orderedLines) {
                if (line.trim().startsWith("import ") && importsEnded) {
                    continue;
                }
                if (line.trim().startsWith("import ")) {
                    importsFound = true;
                    String val = internalImports.get(line);
                    String added = addedImports.get(line);
                    if (val == null && added == null) {
                        print(line);
                        linesPrinted++;
                        if (added == null) {
                            addedImports.put(line, line);
                        }
                    } else {
//                        System.out.println("*** printAll internal " + val + " added  " + added);
                        if (added == null) {
                            addedImports.put(line, line);
                        }
                    }
                } else {
                    if (importsFound) {
                        importsEnded = true;
                    }
                    print(line);
                    linesPrinted++;
                    if (line.startsWith("package ") && !importsEnded) {
                        for (Entry<String, String> entry : internalImports.entrySet()) {
                            print(entry.getKey());
                            linesPrinted++;
                        }
                    }
                }
            }
            System.out.println("*** printAll END " + " linesPrinted =  " + linesPrinted + " in " + outputPath);
            internalImports.clear();
            addedImports.clear();
        }
    }
}
