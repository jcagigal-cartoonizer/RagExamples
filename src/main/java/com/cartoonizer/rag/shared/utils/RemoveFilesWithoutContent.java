package com.cartoonizer.rag.shared.utils;

import java.io.File;

public class RemoveFilesWithoutContent extends ProcessFolder {
    public int filesWithContent = 0;
    public int filesWithoutContent = 0;
    public boolean remove = false;
    public static void main(String[] args) {
        String path = "/Cagi/Portfolio/RagUtils/RagExamples/generated-files";
        RemoveFilesWithoutContent reader = new RemoveFilesWithoutContent(new File(path));
        reader.process();
        System.out.println("*** filesWithContent = " + reader.filesWithContent + " filesWithoutContent = " + reader.filesWithoutContent);
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
