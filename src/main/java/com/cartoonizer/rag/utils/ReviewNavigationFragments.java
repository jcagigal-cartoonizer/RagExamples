package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FILE_NAME;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.FIRST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FILES_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAST_PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.LAYOUTS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ONLY_THIS;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIX;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.PREFIXES;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.shouldSkip;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ReviewNavigationFragments extends ReadFile {
    public static void main(String[] args) {
        reviewAllFragments();
    }
    public static void reviewAllFragments() {
        String sourceFolder = "/Cagi/Portfolio/Compose/smarttdv3Compose/app/src/main/java/ifac/td/taxi/viewmodel/";
        String destinationFolder = GENERATED_FILES_WITH_ASSISTANT + "/compose/navigation/";
        int totalFiles = 0;
        int processedFiles = 0;
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            totalFiles++;
            if (shouldSkip(i)) {
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
            ReviewNavigationFragments reviewNavigation = new ReviewNavigationFragments(sourceFile, destinationFile, PREFIX);
            reviewNavigation.load();
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
        public void createFromFunction(HashMap<Integer, String> numbered) {
            ArrayList<String> list = new ArrayList<>();
            list.add("    fun fromViewModel(viewModel: " + prefix + "ViewModel): " + prefix + "ComposeViewModel {");
            list.add("        val composeViewModel = " + prefix + "ComposeViewModel(");
            for (String parameterName : parameterNames.values()) {
                String param = parameterName.trim();
                list.add("            " + param + " = viewModel." + param + ","); 
            }
            list.add("        )"); 
            list.add("        return composeViewModel"); 
            list.add("    }"); 
            list.add("");
            for (String string : list) {
                numbered.put(numLines, string);
                numLines++;
            }
        }
        public boolean fromFunctionAdded = false;
        @Override
        public void processLine(String line) {
            numberedLines.put(numLines, line);
            numLines++;
        }

        @Override
        public void end() {
            super.end();
            fromFunctionAdded = false;
            numLines = 0;
            fromFunctionAdded = false;
            for (String line : numberedLines.values()) {
                if ((line.contains("fun fromViewModel(viewModel:")) && !fromFunctionAdded) {
                    System.out.println("==> SET fromFunctionAdded = true");
                    fromFunctionAdded = true;
                }
                numLines++;
            }
            System.out.println("==> AFTER fromFunctionAdded = " + fromFunctionAdded);
            HashMap<Integer, String> newNumberedLines = new HashMap<>();
            numLines = 0;
            for (String line : numberedLines.values()) {
                newNumberedLines.put(numLines, line);
                numLines++;
                if ((line.contains(" : BaseViewModel(") || line.contains(") : ViewModel() {")) && !fromFunctionAdded) {
                    System.out.println("==> fromFunctionAdded = false -> createFromFunction");
                    createFromFunction(newNumberedLines);
                }
            }
            for (String line : newNumberedLines.values()) {
                try {
                    if (writer == null) {
                        writer = new PrintWriter(this.destinationFile);
                    }
                    writer.println(line);
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
        String src = GENERATED_FILES_WITH_ASSISTANT + "/compose/viewmodel/" + PREFIX + "ComposeViewModel.kt";
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
