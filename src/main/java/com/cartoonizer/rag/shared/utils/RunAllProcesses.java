package com.cartoonizer.rag.shared.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.BasicFileAttributes;

public class RunAllProcesses {
    public static String[] ONLY_THIS_ARRAY = new String[]{
        "Home",
        "LoginUser",
        "ChangeDriverPin",
        "ChangeUserPassword",
        "SplashScreen",
        "Welcome",
    };
    public static void main(String[] args) {
        if (ONLY_THIS_ARRAY.length > 0) {
            for (int i = 0; i < ONLY_THIS_ARRAY.length; i++) {
                String arg = ONLY_THIS_ARRAY[i];
                GeneralAnswerProcessor.ONLY_THIS = arg;
                runAll();
            }
        } else {
            runAll();
        }
    }

    public static void runAll() {
        // Run GenerateComposeFiles -> input example-files, output generated-files
        GenerateComposeFiles.main(new String[0]);

        // Run ExtractScreenFromFile -> input generated-files/ui, output generated-files/clean-ui
        ExtractScreenFromFile.main(new String[0]);
        // COPY from generated-files/clean-ui to generated-files/ui
        copyNeededFiles("./generated-files/clean-ui", "./generated-files/ui");

        // Run GenerateNavigationFragment -> input output-files/navigation and processed-files/navigation, output generated-files/compose/navigation
        GenerateNavigationFragment.main(new String[0]);

        // Run CleanComposableFile -> input generated-files/compose/navigation, output generated-files/clean-ui/navigation
        CleanComposableFile.main(new String[0]);
        // COPY from generated-files/clean-ui/navigation to generated-files/compose/navigation
        copyNeededFiles("./generated-files/clean-ui/navigation", "./generated-files/compose/navigation");

        ReviewNavigationFragments.reviewAllFragments();

        // Run ReviewImports
        ReviewImports.main(new String[0]);
        
        DeleteComposeInNavigation deleteFiles = new DeleteComposeInNavigation(new File("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/compose/navigation"));
        deleteFiles.process();
        deleteFiles = new DeleteComposeInNavigation(new File("/Cagi/Portfolio/RagUtils/RagExamples/copy-to-app/compose/viewmodel"));
        deleteFiles.process();
        
        copyNeededFiles("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/ui/screen", "/Cagi/Portfolio/RagUtils/RagExamples/copy-to-app/ui/screen");
        copyNeededFiles("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/ui/screen/components", "/Cagi/Portfolio/RagUtils/RagExamples/copy-to-app/ui/screen/components");
        copyNeededFiles("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/compose/navigation", "/Cagi/Portfolio/RagUtils/RagExamples/copy-to-app/compose/navigation");
        copyNeededFiles("/Cagi/Portfolio/RagUtils/RagExamples/generated-files/compose/viewmodel", "/Cagi/Portfolio/RagUtils/RagExamples/copy-to-app/compose/viewmodel");
    
    }

    private static void copyNeededFiles(String source, String destination) {
        File pathSource = new File(source);
        File pathDestination = new File(destination);
        CopyFolder copyFolder = new CopyFolder(pathSource, pathDestination);
        copyFolder.process();
    }

    public static class CopyFolder extends ProcessFolder {

        private File destinationFolder;

        public CopyFolder(File sourceFolder, File destinationFolder) {
            super(sourceFolder);
            this.destinationFolder = destinationFolder;
        }

        @Override
        public boolean processFile(File fitxer) {
            try {
                String sourceFolder = pathFolder;
                String destinationFolder = this.destinationFolder.getAbsolutePath();
                Path pathSource = Paths.get(sourceFolder + "/" + fitxer.getName());
                Path pathDestination = Paths.get(destinationFolder + "/" + fitxer.getName());
                System.out.println("*** processFile copy " + fitxer.getAbsolutePath() + " to " + destinationFolder + "/" + fitxer.getName());
                Files.copy(pathSource, pathDestination, REPLACE_EXISTING);
            } catch (Exception e) {
                System.out.println("ERROR processFile " + fitxer.getAbsolutePath() + ": " + e);
            }
            return true;
        }

    }

    private static void copyFiles(String source, String destination) {
        try {
            Path sourcePath = Paths.get(source);
            Path target = Paths.get(destination);
            System.out.println("*** copyFiles " + source + " to " + destination);
            copyFolder(sourcePath, target, REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("ERROR copyFiles " + source + " to " + destination + ": " + e);
        }
    }

    public static void copyFolder(Path source, Path target, CopyOption... options)
            throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Path destination = target.resolve(source.relativize(file).toString());
                Files.copy(file, destination, options);
//                System.out.println("    " + file + " to " + destination);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    public static class DeleteComposeInNavigation extends ProcessFolder {

        public DeleteComposeInNavigation(File rootFolder) {
            super(rootFolder);
        }

        @Override
        public boolean processFile(File fitxer) {
            if (fitxer.getName().endsWith("ComposeFragment.kt")) {
                fitxer.delete();
            }
            return true;
        }
        
    }
}
