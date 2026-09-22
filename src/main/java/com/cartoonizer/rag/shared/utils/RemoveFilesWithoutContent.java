package com.cartoonizer.rag.shared.utils;

import java.io.File;

public class RemoveFilesWithoutContent extends ProcessFolder {
    public static int filesWithContent = 0;
    public static int filesWithoutContent = 0;
    public boolean remove = false;
    public static void main(String[] args) {
        String path = "/Cagi/Portfolio/RagUtils/RagExamples/generated-files";
        RemoveFilesWithoutContent reader = new RemoveFilesWithoutContent(new File(path));
        reader.process();
        System.out.println("*** filesWithContent = " + RemoveFilesWithoutContent.filesWithContent + " filesWithoutContent = " + RemoveFilesWithoutContent.filesWithoutContent);
    }

    public RemoveFilesWithoutContent(File origen) {
        this(origen, false);
    }
    public RemoveFilesWithoutContent(File origen, boolean remove) {
        super(origen);
        this.remove = remove;
    }

    @Override
    public boolean processFile(File fitxer) {
        String path = fitxer.getAbsolutePath();
        FileReader fileReader = new FileReader(path);
        fileReader.load();
        if (fileReader.numLines < 2) {
            filesWithoutContent++;
            if (remove) {
                System.out.println("*** DELETED because NO CONTENT IN " + path);
                fitxer.delete();
            }
        } else {
            filesWithContent++;
        }
        return true;
    }

    public class FileReader extends ReadFile{

        @Override
        public void end() {
            super.end();
        }

        @Override
        public void begin() {
            super.begin();
            numLines = 0;
        }

        public int numLines;

        public FileReader(String pathOrigen) {
            super(pathOrigen);
        }
        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("// ") || line.trim().startsWith("package ") || line.trim().startsWith("import ")) {
                return;
            }
            numLines++;
        }
    }
    
}
