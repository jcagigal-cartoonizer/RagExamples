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
    private HashMap<Integer, String> numberedLines = new HashMap<>();
    private HashMap<String, String> lines = new HashMap<>();
    private HashMap<Integer, String> outputLines = new HashMap<>();
    public HashMap<String, String> parameterNames = new HashMap<>();
    private int numLines = 0;

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
    /*
    - Add companion object to ComposeViewModel
    */
    public void saveComposeViewModel(String sourcePath, String destinationPath) {
        System.out.println("*** ReviewNavigationFragments saveComposeViewModel " + sourcePath);
        SaveComposeViewModel save = new SaveComposeViewModel(sourcePath, destinationPath, parameterNames);
        save.load();
    }
    class SaveComposeViewModel extends ReadFile {
        private HashMap<Integer, String> numberedLines = new HashMap<>();
        private HashMap<String, String> parameterNames = new HashMap<>();
        private File destinationFile;
        private PrintWriter writer;
        private int numLines;
        public SaveComposeViewModel(String sourcePath, String destinationPath, HashMap<String, String> parameterNames) {
            super(sourcePath);
            this.parameterNames = parameterNames;
            this.destinationFile = new File(destinationPath);
            numLines = 0;
        }
        public void createFromFunction() {
            ArrayList<String> list = new ArrayList<>();
            list.add("    companion object {"); 
            list.add("        fun fromViewModel(viewModel: " + prefix + "ViewModel): " + prefix + "ComposeViewModel {");
            list.add("            val composeViewModel = " + prefix + "ComposeViewModel(");
            list.add("    // for every param in ViewModel:"); 
            list.add("                viewModel.tripUseCase,"); 
            list.add("                viewModel.licensingUseCase,"); 
            list.add("                viewModel.bluetoothLocalUseCase,"); 
            list.add("                viewModel.userPreferencesUseCase,"); 
            list.add("                viewModel.ttsUseCase,"); 
            list.add("                viewModel.context"); 
            list.add("            )"); 
            list.add("            return composeViewModel"); 
            list.add("        }"); 
            list.add("");
            for (String string : list) {
                numberedLines.put(numLines, string);
                numLines++;
            }
        }

        @Override
        public void processLine(String line) {
            numberedLines.put(numLines, line);
            numLines++;
            if (line.contains(" : BaseViewModel(")) {
                createFromFunction();
            }
        }

        @Override
        public void end() {
            super.end();
            for (String value : numberedLines.values()) {
                try {
                    if (writer == null) {
                        writer = new PrintWriter(this.destinationFile);
                    }
                    writer.println(value);
                    writer.flush();
                } catch (Exception e) {
                    System.out.println("EXCEPTION ReviewNavigationFragments: " + e);
                }
            }
        }
        
    }
    /*
    - Change private val inside ViewModel constructor
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
                    writer.flush();
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION ReviewNavigationFragments: " + e);
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
        String src = "/Cagi/Portfolio/Compose/smarttdv3Compose/app/src/main/java/ifac/td/taxi/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt";
        String dst = src;
        saveComposeViewModel(src, dst);
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
