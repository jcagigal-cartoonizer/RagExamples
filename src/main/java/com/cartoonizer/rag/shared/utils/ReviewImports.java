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
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

public class ReviewImports extends ReadFile {

    public static void main(String[] args) {
        String sourceFolder = "./generated-files/compose/navigation/";
        String destinationFolder = "./copy-to-app/compose/navigation/";
        reviewAllImports(sourceFolder, destinationFolder);
    }

    public static void reviewAllImports(String sourceFolder, String destinationFolder) {
        int totalFiles = 0;
        int processedFiles = 0;
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            totalFiles++;
            if (shouldSkip(i)) {
                continue;
            }
            FILE_NAME = PREFIX + "NavigationFragment.kt";
            String sourcePath = sourceFolder + FILE_NAME;
            File sourceFile = new File(sourcePath);
            String destinationPath = destinationFolder + FILE_NAME;
            File destinationFile = new File(destinationPath);
            if (!sourceFile.exists()) {
                System.out.println("==> GenerateNavigationFiles MISSING " + sourceFile.getAbsolutePath());
                continue;
            }
            processedFiles++;
            System.out.println("*** reviewAllImports sourceFile: " + sourceFile.getAbsolutePath() + " -> " + destinationFile.getAbsolutePath());
            ReviewImports reviewImports = new ReviewImports(sourceFile, destinationFile, PREFIX);
            reviewImports.load();
        }
        System.out.println("*** reviewAllImports processedFiles: " + processedFiles + " of " + totalFiles);
    }

    public File destinationFile;
    public PrintWriter writer;
    public String prefix;
    public String[] IMPORTS = new String[]{};
    public HashMap<Integer, String> numberedLines = new HashMap<>();
    public HashMap<String, String> lines = new HashMap<>();
    public HashMap<Integer, String> outputLines = new HashMap<>();
    public HashMap<String, String> importLines = new HashMap<>();
    public int numLines = 0;

    public ReviewImports(File sourceFile, File destinationFile, String prefix) {
        super(sourceFile.getAbsolutePath());
        this.destinationFile = destinationFile;
        this.prefix = prefix;
        IMPORTS = new String[]{
            "import android.content.Intent",
            "import androidx.fragment.app.Fragment",
            "import androidx.fragment.app.viewModels",
            "import android.os.Bundle",
            "import android.view.LayoutInflater",
            "import android.view.View",
            "import android.view.ViewGroup",
            "import ifac.td.taxi.ui.screen." + prefix + "Screen",
            "import ifac.td.taxi.compose.viewModel." + prefix + "ComposeViewModel",
            "import ifac.td.taxi.viewModel." + prefix + "ViewModel",
            "import androidx.compose.material3.MaterialTheme",
            "import androidx.compose.runtime.Composable",
            "import androidx.compose.ui.platform.ComposeView",
            "import androidx.compose.ui.platform.ViewCompositionStrategy",
            "import androidx.navigation.fragment.findNavController",
        };
    }

    @Override
    public void processLine(String line) {
        numberedLines.put(numLines, line);
        lines.put(line, line);
        numLines++;
    }

    public void saveImports() {
        if (!numberedLines.isEmpty()) {
            int readLines = numberedLines.size();
            for (int i = 0; i < readLines; i++) {
                String line = numberedLines.get(i);
                if (line.trim().startsWith("import ")) {
                    if (importLines.get(line) == null) {
                        importLines.put(line, line);
                    }
                }
            }
        }
        for (int j = 0; j < IMPORTS.length; j++) {
            if (importLines.get(IMPORTS[j]) == null) {
                importLines.put(IMPORTS[j], IMPORTS[j]);
            }
        }
    }

    public void saveFile() {
        numLines = 0;
        if (!numberedLines.isEmpty()) {
            try {
                writer = new PrintWriter(destinationFile);
                int readLines = numberedLines.size();
                for (int i = 0; i < readLines; i++) {
                    String line = numberedLines.get(i);
                    if (line.trim().startsWith("package ")) {
                        outputLines.put(numLines, line);
                        numLines++;
                        for (int j = 0; j < IMPORTS.length; j++) {
                            outputLines.put(numLines, IMPORTS[j]);
                            numLines++;
                        }
                    } else if (line.trim().startsWith("import ")) {

                    } else {
                        outputLines.put(numLines, line);
                        numLines++;
                    }
                }
                for (String value : outputLines.values()) {
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
        saveImports();
        saveFile();
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
