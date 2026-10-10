package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FILES_WITH_ASSISTANT;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ExtractScreenFromFile extends ProcessFolder {

    public static void main(String[] args) {
        File path = new File(GENERATED_FILES_WITH_ASSISTANT + "/ui/screen");
        ExtractScreenFromFile extract = new ExtractScreenFromFile(path);
        extract.process();
        System.out.println("==> ExtractScreenFromFile filesProcessed " + extract.filesProcessed);
    }
    public int filesProcessed = 0;

    public ExtractScreenFromFile(File rootFolder) {
        super(rootFolder);
    }

    @Override
    public boolean processFile(File fitxer) {
        if (fitxer.getName().endsWith("Screen.kt")) {
            return true;
        }
        System.out.println("*** ExtractScreenFromFile processFile " + fitxer.getAbsolutePath());
        filesProcessed++;
        ExtractScreen extract = new ExtractScreen(fitxer.getAbsolutePath());
        extract.load();
        return true;
    }

    class ExtractScreen extends ReadFile {

        public File file;
        public String path;
        public String screenOutputPath;
        public String componentOutputPath;
        public int numLines;
        public HashMap<String, String> lines = new HashMap<>();
        public HashMap<Integer, String> numberedLines = new HashMap<>();
        public HashMap<Integer, String> screenLines = new HashMap<>();
        public HashMap<Integer, String> componentLines = new HashMap<>();
        public PrintWriter writerScreen;
        public PrintWriter writerComponent;
        public boolean screenFound;
        public String screenName;

        public ExtractScreen(String pathOrigen) {
            // pathOrigen = generated-files/ui/screen/components/AboutCustomDialog.kt
            super(pathOrigen);
            screenFound = false;
            screenName = null;
            this.path = pathOrigen;
            this.file = new File(pathOrigen);
            String parentPath = this.file.getParent();
            String prefix = getPrefixFromName(file.getName());
            if (parentPath.contains("/ui/screen/components")) {
                this.screenOutputPath = parentPath.replaceAll(Pattern.quote("/ui/screen/components"), "/extracted/ui/screen") + "/" + prefix + "Screen.kt";
                this.componentOutputPath = parentPath.replaceAll(Pattern.quote("/ui/screen/components"), "/extracted/ui/screen/components") + "/" + file.getName();
            } else {
                this.screenOutputPath = parentPath.replaceAll(Pattern.quote("/ui/screen"), "/extracted/ui/screen") + "/" + prefix + "Screen.kt";
                this.componentOutputPath = parentPath.replaceAll(Pattern.quote("/ui/screen/components"), "/extracted/ui/screen/components") + "/" + file.getName();
            }
            System.out.println("*** ExtractScreenFromFile screenOutputPath = " + screenOutputPath);
            System.out.println("*** ExtractScreenFromFile componentOutputPath = " + componentOutputPath);
        }

        @Override
        public void processLine(String line) {
            lines.put(line, line);
            numberedLines.put(numLines, line);
            numLines++;
        }

        @Override
        public void end() {
            super.end();
            screenLines = new HashMap<>();
            componentLines = new HashMap<>();
            int totalLines = numberedLines.size();
            int idxComponents = 0;
            int idxScreen = 0;
            for (int i = 0; i < totalLines; i++) {
                String line = numberedLines.get(i);
                if (line.trim().startsWith("@Composable")) {
                    String next = numberedLines.get(i + 1);
                    if (next.trim().startsWith("fun ") && next.contains("Screen(")) {
                        screenFound = true;
                        screenName = next.trim().replaceAll(Pattern.quote("fun "), "").replaceAll(Pattern.quote("("), "").trim();
                        System.out.println("==> ExtractScreenFromFile screenName = " + screenName + " in " + screenOutputPath);
                    }
                }
                if (screenFound) {
                    screenLines.put(idxScreen, line);
                    idxScreen++;
                } else {
                    componentLines.put(idxComponents, line);
                    idxComponents++;
                }
            }
            saveScreenLines();
            saveComponentLines();
        }

        public void saveScreenLines() {
            if (screenLines.entrySet().isEmpty()) {
                return;
            }
            if (screenName != null && !screenName.isEmpty()) {
                String parent = new File(screenOutputPath).getParent();
                screenOutputPath = parent + "/" + screenName + ".kt";
            }
            try {
                this.writerScreen = new PrintWriter(this.screenOutputPath);
                System.out.println("*** ExtractScreenFromFile SAVED " + screenOutputPath);
            } catch (Exception e) {
                System.out.println("*** ExtractScreenFromFile ERROR SAVING " + screenOutputPath + ": " + e);
            }
            if (this.writerScreen == null) {
                System.out.println("ERROR writer == null");
                return;
            }
            for (Map.Entry<Integer, String> entry : numberedLines.entrySet()) {
                String value = entry.getValue();
                int num = entry.getKey();
                if (value.trim().startsWith("package ") || value.trim().startsWith("import ")) {
                    if (value.trim().startsWith("package ")) {
                        value = value.replaceAll(Pattern.quote("package ifac.td.taxi.ui.screen.components"), "package ifac.td.taxi.ui.screen");
                    }
                    writerScreen.println(value);
                }
            }
            for (Map.Entry<Integer, String> entry : screenLines.entrySet()) {
                String line = entry.getValue();
                writerScreen.println(line);
            }
            writerScreen.close();
        }

        public void saveComponentLines() {
            if (componentLines.entrySet().isEmpty()) {
                return;
            }
            try {
                this.writerComponent = new PrintWriter(this.componentOutputPath);
            } catch (Exception e) {
                System.out.println("*** ExtractScreenFromFile ERROR creating writer for " + componentOutputPath + ": " + e);
            }
            if (this.writerComponent == null) {
                System.out.println("ERROR writer == null");
                return;
            }
            for (Map.Entry<Integer, String> entry : componentLines.entrySet()) {
                String line = entry.getValue();
                writerComponent.println(line);
            }
            writerComponent.close();
        }

        public static String getPrefixFromName(String name) {
            for (int i = 0; i < GeneralAnswerProcessor.PREFIXES.length; i++) {
                if (name.startsWith(GeneralAnswerProcessor.PREFIXES[i])) {
                    return GeneralAnswerProcessor.PREFIXES[i];
                }

            }
            return "";
        }
    }

}
