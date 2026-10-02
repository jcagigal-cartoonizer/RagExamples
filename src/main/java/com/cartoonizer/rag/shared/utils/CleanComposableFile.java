package com.cartoonizer.rag.shared.utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CleanComposableFile extends ProcessFolder {
    public static String PATTERN = "/ui/";
    public static String REPLACEMENT = "/clean-ui/";
    public static void main(String[] args) {
//        REPLACEMENT = "/clean-ui/";
//        PATTERN = "/ui/";
//        File path = new File("./generated-files/ui/screen");
        File path = new File("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/compose/navigation");
        REPLACEMENT = "/clean-ui/navigation/";
        PATTERN = "compose/navigation";
        CleanComposableFile extract = new CleanComposableFile(path);
        extract.process();
        System.out.println("==> CleanComposableFile filesProcessed " + extract.filesProcessed);
    }
    public int filesProcessed = 0;
    public CleanComposableFile(File rootFolder) {
        super(rootFolder);
    }

    @Override
    public boolean processFile(File fitxer) {
//        System.out.println("*** CleanComposableFile " + fitxer.getAbsolutePath());
        filesProcessed++;
        CleanFile extract = new CleanFile(fitxer.getAbsolutePath());
        extract.load();
        return true;
    }
    class CleanFile extends ReadFile {
        public File file;
        public String path;
        public String outputPath;
        public int numLines;
        public HashMap<String, String> lines = new HashMap<>();
        public HashMap<Integer, String> numberedLines = new HashMap<>();
        public HashMap<Integer, String> cleanLines = new HashMap<>();
        public PrintWriter writer;
        public CleanFile(String pathOrigen) {
            super(pathOrigen);
            this.path = pathOrigen;
            this.file = new File(pathOrigen);
            String parentPath = this.file.getParent();
            this.outputPath = (parentPath.replaceAll(Pattern.quote(PATTERN), REPLACEMENT) + "/" + file.getName()).replaceAll("//", "/");
            System.out.println("*** CleanComposableFile outputPath = " + outputPath );
            try {
                this.writer = new PrintWriter(this.outputPath);
            } catch (Exception e) {
                System.out.println("*** CleanComposableFile ERROR in " + outputPath + ": " + e);
            }
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
            cleanLines = new HashMap<>();
            int totalLines = numberedLines.size();
            int lastValidLine = totalLines - 1;
            for (int i = totalLines - 1; i >= 0; i--) {
                String line = numberedLines.get(i);
                if (line != null && line.trim().equals("}")) {
                    lastValidLine = i;
                    break;
                }
            }
            for (Map.Entry<Integer, String> entry : numberedLines.entrySet()) {
                int index = entry.getKey();
                String line = entry.getValue();
                if (index <= lastValidLine) {
                    cleanLines.put(index, line);
                }
            }
            saveCleanLines();
        }
        public void saveCleanLines() {
            if (this.writer == null) {
                System.out.println("ERROR writer == null");
                return;
            }
            int totalLines = cleanLines.size();
            for (int i = 0; i < totalLines; i++) {
                String line = cleanLines.get(i);
                writer.println(line);
            }
            writer.close();
        }
    }
    
}
