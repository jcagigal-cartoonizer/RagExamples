package com.cartoonizer.rag.utils;

import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.COPY_TO_APP_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.GENERATED_FILES_WITH_ASSISTANT;
import static com.cartoonizer.rag.utils.GeneralAnswerProcessor.ONLY_THIS;
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
//                "MeetingSign",
//                "MessageDetail",
//                "Messages",
//                "OfflineInvoice",
//                "OnlineInvoice",
//                "OnTrip",
    };

    public static void main(String[] args) {
        if (ONLY_THIS.isEmpty() && ONLY_THIS_ARRAY.length > 0) {
            for (int i = 0; i < ONLY_THIS_ARRAY.length; i++) {
                GeneralAnswerProcessor.ONLY_THIS = ONLY_THIS_ARRAY[i];
                runAll();
            }
        } else {
            runAll();
        }
    }

    public static void runAll() {
        System.out.println("=== RunAllProcesses " + GeneralAnswerProcessor.ONLY_THIS);
        // Run GenerateComposeFiles -> input example-files, output generated-files
        GenerateComposeFiles.main(new String[0]);

        // Run ExtractScreenFromFile -> input generated-files/ui, output generated-files/clean-ui
        ExtractScreenFromFile.main(new String[0]);
        // COPY from generated-files/clean-ui to generated-files/ui
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/extracted/ui/screen", GENERATED_FILES_WITH_ASSISTANT + "/ui/screen", GeneralAnswerProcessor.ONLY_THIS);
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/extracted/ui/screen/components", GENERATED_FILES_WITH_ASSISTANT + "/ui/screen/components", GeneralAnswerProcessor.ONLY_THIS);

        // Run GenerateNavigationFragment -> input output-files/navigation and processed-files/navigation, output generated-files/compose/navigation
        GenerateNavigationFragment.main(new String[0]);

        // Run CleanComposableFile -> input generated-files/compose/navigation, output generated-files/clean-ui/navigation
        CleanComposableFile.main(new String[0]);
        // COPY from generated-files/clean-ui/navigation to generated-files/compose/navigation
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/clean-ui/navigation", GENERATED_FILES_WITH_ASSISTANT + "/compose/navigation", GeneralAnswerProcessor.ONLY_THIS);

        ReviewNavigationFragments.reviewAllFragments();

        // Run ReviewImports
        ReviewImports.main(new String[0]);

        DeleteComposeInNavigation deleteFiles = new DeleteComposeInNavigation(new File(GENERATED_FILES_WITH_ASSISTANT + "/compose/navigation"));
        deleteFiles.process();
        deleteFiles = new DeleteComposeInNavigation(new File(COPY_TO_APP_WITH_ASSISTANT + "/compose/viewmodel"));
        deleteFiles.process();

        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/ui/screen", COPY_TO_APP_WITH_ASSISTANT + "/ui/screen", GeneralAnswerProcessor.ONLY_THIS);
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/ui/screen/components", COPY_TO_APP_WITH_ASSISTANT + "/ui/screen/components", GeneralAnswerProcessor.ONLY_THIS);
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/compose/navigation", COPY_TO_APP_WITH_ASSISTANT + "/compose/navigation", GeneralAnswerProcessor.ONLY_THIS);
        copyNeededFiles(GENERATED_FILES_WITH_ASSISTANT + "/compose/viewmodel", COPY_TO_APP_WITH_ASSISTANT + "/compose/viewmodel", GeneralAnswerProcessor.ONLY_THIS);

    }

    private static void copyNeededFiles(String source, String destination, String prefix) {
        File pathSource = new File(source);
        File pathDestination = new File(destination);
        CopyFolder copyFolder = new CopyFolder(pathSource, pathDestination, prefix);
        copyFolder.process();
    }

    public static class CopyFolder extends ProcessFolder {

        private File destinationFolder;
        private String prefix;

        public CopyFolder(File sourceFolder, File destinationFolder, String prefix) {
            super(sourceFolder);
            this.destinationFolder = destinationFolder;
            this.prefix = prefix;
        }

        @Override
        public boolean processFile(File fitxer) {
            if (fitxer.getName().contains(prefix)) {
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
