package com.cartoonizer.rag.shared.utils;

import com.cartoonizer.rag.shared.utils.ReadFile;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenerateComposeFiles {

    public static void main(String[] args) {
        String processedAnswerPath = "./processed-files/processed-HomeFragment.txt";
        String prefix = "Home";
        ReadFile reader = new GenerateFiles(processedAnswerPath, prefix);
        reader.load();
    }

    public static class GenerateFiles extends ReadFile {

        private boolean beginRoutes;
        private boolean beginUiState;
        private boolean beginDialogSpecs;
        private boolean beginFullUiState;
        private boolean beginViewModel;
        private boolean beginScreen;
        private boolean beginFullScreen;
        private boolean beginButton;
        private boolean beginDialogs;
        private boolean endProcessing;
        private boolean printAlways;
        private String packageName;
        private PrintWriter writer;
        private String outputPath;
        private String currentFile;

        public GenerateFiles(String processedAnswerPath, String prefix) {
            super(processedAnswerPath);
        }
        private boolean[] BOOLEANS = {
            beginRoutes,
            beginUiState,
            beginDialogSpecs,
            beginFullUiState,
            beginViewModel,
            beginScreen,
            beginFullScreen,
            beginButton,
            beginDialogs,
            endProcessing,};
        private static String[] BLOCKS = {
            "// ## 1) Routes + effects",
            "// ## 2) UI state + button state",
            "// ## 3) Dialog specs",
            "// ## 4) Full Compose-friendly `HomeUiState`",
            "// ## 5) Refactored `HomeViewModel`",
            "// ## 6) Compose screen with lifecycle collection and dialog handling",
            "// ## 7) Screen UI matching the XML grid",
            "// ## 8) Button composable",
            "// ## 9) Dialogs",};
        private static String[] FILES = {
            "./generated-files/compose/routes/HomeRoute.kt",
            "./generated-files/ui/screen/state/HomeButtonsState.kt",
            "./generated-files/ui/screen/components/HomeDialogSpec.kt",
            "./generated-files/ui/screen/state/HomeUiState.kt",
            "./generated-files/compose/viewmodel/HomeViewModel.kt",
            "./generated-files/ui/screen/HomeScreen1.kt",
            "./generated-files/ui/screen/HomeScreen.kt",
            "./generated-files/ui/screen/components/HomeTile.kt",
            "./generated-files/ui/screen/components/HomeDialogs.kt",};
        private static String[] PACKAGES = {
            "ifac.td.taxi.compose.routes",
            "ifac.td.taxi.ui.screen.state",
            "ifac.td.taxi.ui.screen.components",
            "ifac.td.taxi.ui.screen.state",
            "ifac.td.taxi.compose.viewmodel",
            "ifac.td.taxi.ui.screen",
            "ifac.td.taxi.ui.screen",
            "ifac.td.taxi.ui.screen.components",
            "ifac.td.taxi.ui.screen.components",};
        public HashMap<String, String> imports = new HashMap<>();

        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("```")) {
                return;
            }
            if (line.trim().startsWith("---")) {
                return;
            }
            for (int i = 0; i < BLOCKS.length; i++) {
                if (line.trim().startsWith("This is the replacement")) {
                    line = "// " + line;
                }
                if (line.trim().startsWith("This version removes fragment")) {
                    line = "// " + line;
                }
                if (line.trim().startsWith("This is the `onResume` replacement")) {
                    line = "// " + line;
                }
                if (line.trim().startsWith(BLOCKS[i])) {
                    line = "// " + line;
                    BOOLEANS[i] = true;
                    printAlways = true;
                    if (outputPath != null && !outputPath.isEmpty()) {
                        closeFile();
                    }
                    outputPath = FILES[i];
                    imports.put("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable");
                    openFile(outputPath, PACKAGES[i]);
                }
            }
            if (line.trim().startsWith("import ")) {
                if (imports.get(line) == null && !line.trim().equals("import androidx.compose.runtime.Composable") &&
                        !line.trim().equals("import ifac.td.taxi.R")) {
                    System.out.println("*** " + currentFile + " add to imports: " + line);
                    imports.put(line, line);
                } else {
                    line = "// " + line;
                }
            }
            if (printAlways) {
//                    System.out.println(line);
                if (!line.contains("import ifac.td.taxi.R")) {
                    if (writer != null) {
                        writer.println(line);
                        writer.flush();
                    }
                }
            }

// ## 10) Notes on behavior mapping
// End processing
            if (line.trim().startsWith("// ## 10) Notes on behavior mapping")) {
                closeFile();
                printAlways = false;
            }
        }

        @Override
        public void end() {
            super.end();
            if (writer != null) {
                writer.close();
            }
        }

        private void closeFile() {
            if (writer != null) {
                writer.close();
            }
        }

        private void openFile(String path, String packageName) {
            if (path != null && !path.isEmpty()) {
                try {
                    writer = new PrintWriter(path);
                    writer.println("package " + packageName);
                    System.out.println("*** " + currentFile + " is first line -> print " + imports.size() + " imports: ");
                    for (String imp : imports.keySet()) {
                        writer.println(imp);
                    }
                    imports.clear();
                    this.packageName = packageName;
                    this.currentFile = path;
                } catch (Exception ex) {
                    writer = null;
                }
            }
        }
    }
}
