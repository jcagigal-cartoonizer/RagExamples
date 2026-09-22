package com.cartoonizer.rag.shared.utils;

import java.io.File;

public abstract class ProcessFolder {

    public static int numFitxers;
    protected File rootFolder;
    protected String pathFolder;
    public static String currentFolder;
    public ProcessFolder(File rootFolder) {
        this.rootFolder = rootFolder;
        pathFolder = rootFolder.getAbsolutePath();
    }
    public final void process() {
        begin();
        processFolder(rootFolder);
        end();
        
    }
    protected void begin() {
        
    }
    protected void end() {
        
    }
    public abstract boolean processFile(File fitxer);
    protected boolean processFolder(File carpeta) {
        if (!carpeta.isDirectory() || !carpeta.exists()) {
            return false;
        }
        currentFolder = carpeta.getAbsolutePath();
        File[] fitxers = carpeta.listFiles();
//        System.out.println("*** processarCarpeta " + carpeta.getAbsolutePath() + " fitxers = " + (fitxers == null ? 0 : fitxers.length));
        for (int i = 0; fitxers != null && i < fitxers.length; i++) {
            File fitxer = fitxers[i];
//            System.out.println("processant " + fitxer.getAbsolutePath() + " isFile = " + fitxer.isFile() + " class = " + this.getClass().getSimpleName());
            boolean ignore = ignore(fitxer);
            if (fitxer.isFile()) {
                if (!ignore) {
                    processFile(fitxer);
                }
            } else if (fitxer.isDirectory()) {
                processFolder(fitxer);
            }
        }
        return true;
    }
    public boolean ignore(File carpeta) {
        return false;
    }
    
}
