package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.PREFIXES;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ReviewNavigationFragments extends ReadFile {
    public static void main(String[] args) {
        String sourceFolder = "/Cagi/Portfolio/Compose/smarttdv3Compose/app/src/main/java/ifac/td/taxi/viewmodel/";
        String destinationFolder = "./copy-to-app/compose/navigation/";
        reviewAllFragments(sourceFolder, destinationFolder);
    }
    public static String createFromFunction(ArrayList<String> params) {
        StringBuilder function = new StringBuilder(
            "    companion object {\n");
            function.append("        fun fromViewModel(viewModel: ").append(PREFIX).append("ViewModel): ").append(PREFIX).append("ComposeViewModel {\n");
            function.append("            val composeViewModel = ").append(PREFIX).append("ComposeViewModel(\n");
            for (String param : params) {
                function.append("                viewModel.").append(param).append(",\n");
            }
            function.append("            )");
            function.append("            return composeViewModel");
            function.append("        }\n"); 
        return function.toString();
    }
    public static void reviewAllFragments(String sourceFolder, String destinationFolder) {
        int totalFiles = 0;
        int processedFiles = 0;
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            totalFiles++;
            if ((!ONLY_THIS.isEmpty() && !PREFIX.equals(ONLY_THIS)) || (ONLY_THIS.isEmpty() && FIRST_PREFIX >= 0 && (i < FIRST_PREFIX || i > LAST_PREFIX))) {
                continue;
            }
            FILE_NAME = PREFIX + "ViewModel.kt";
            String sourcePath = sourceFolder + FILE_NAME;
            File sourceFile = new File(sourcePath);
            String destinationPath = destinationFolder + FILE_NAME;
            File destinationFile = new File(destinationPath);
            if (!sourceFile.exists()) {
                System.out.println("==> ReviewNavigationFragments MISSING " + sourceFile.getAbsolutePath());
                continue;
            }
            processedFiles++;
            System.out.println("*** ReviewNavigationFragments sourceFile: " + sourceFile.getAbsolutePath() + " -> " + destinationFile.getAbsolutePath());
            ReviewNavigationFragments reviewImports = new ReviewNavigationFragments(sourceFile, destinationFile, PREFIX);
            reviewImports.load();
        }
        System.out.println("*** ReviewNavigationFragments processedFiles: " + processedFiles + " of " + totalFiles);
    }

    public File destinationFile;
    public PrintWriter writer;
    public String prefix;
    public HashMap<Integer, String> numberedLines = new HashMap<>();
    public HashMap<String, String> lines = new HashMap<>();
    public HashMap<Integer, String> outputLines = new HashMap<>();
    public HashMap<String, String> parameterNames = new HashMap<>();
    public int numLines = 0;

    public ReviewNavigationFragments(File sourceFile, File destinationFile, String prefix) {
        super(sourceFile.getAbsolutePath());
        this.destinationFile = destinationFile;
        this.prefix = prefix;
    }

    @Override
    public void processLine(String line) {
        numberedLines.put(numLines, line);
        lines.put(line, line);
        numLines++;
    }
    public void saveViewsFragment() {
        
    }
    /*
    - Change private val inside constructor
    */
    public void saveViewModel() {
        numLines = 0;
        boolean constructorFound = false;
        if (!numberedLines.isEmpty()) {
            try {
                
                int readLines = numberedLines.size();
                for (int i = 0; i < readLines; i++) {
                    String line = numberedLines.get(i);
                    if (line.trim().startsWith("class " + PREFIX + "ViewModel(")) {
                        constructorFound = true;
                    }
                    if (line.contains(")") && !line.contains("(") && constructorFound) {
                        constructorFound = false;
                    }
                    if (constructorFound) {
                        if (!line.trim().startsWith("class ")) {
                            if (!line.contains(")")) {
                                line = line.replaceAll("private ", "");
                                // private val bluetoothLocalUseCase: BluetoothLocalUseCase
                                String param = line.replaceAll("private val ", "").replaceAll("val ", "");
                                int idx = param.indexOf(":");
                                if (idx > 0) {
                                    param = param.substring(0, idx);
                                }
                                parameterNames.put(param, param);
                            }
                        } else { // when ( and ) are in the same line: class ChangeDriverPinViewModel(context: Application) 
                            if (line.contains("(") && line.contains(")")) {
                                int idx = line.indexOf("(");
                                String param = line.substring(idx + 1);
                                idx = param.indexOf(")");
                                if (idx > 0) {
                                    param = param.substring(0, idx);
                                    idx = param.indexOf(":");
                                    if (idx > 0) {
                                        param = param.substring(0, idx);
                                    }
                                }
                                parameterNames.put(param, param);
                                constructorFound = false;
                            }
                        }
                    }
                    if (line.contains(")") && constructorFound) {
                        constructorFound = false;
                    }
                    outputLines.put(numLines, line);
                    numLines++;
                }
                System.out.println("*** ReviewNavigationFragments " + this.origen.getAbsolutePath());
//                for (String param : parameterNames.keySet()) {
//                    System.out.println("    ->" + param);
//                }
                for (String value : outputLines.values()) {
                    if (writer == null) {
                        writer = new PrintWriter(origen);
                    }
                    writer.println(value);
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION ReviewImports: " + e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    @Override
    public void end() {
        super.end();
        saveViewModel();
        saveViewsFragment();
    }

    @Override
    public void begin() {
        super.begin();
        numberedLines = new HashMap<>();
        lines = new HashMap<>();
        outputLines = new HashMap<>();
        numLines = 0;
    }

}
