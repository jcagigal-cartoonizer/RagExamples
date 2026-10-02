package com.cartoonizer.rag.shared.utils;

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
    public static void main(String[] args) {
//        String source = "./generated-files/ui";
//        String destination = "./copy-to-app/ui";
//        System.out.println("source = " + source + " destination = " + destination);
//        copyFiles(source, destination);

//        runAll();
    }
    public static void runAll() {
        // Run GenerateComposeFiles -> input example-files, output generated-files
        GenerateComposeFiles.main(new String[0]);
        
        // Run ExtractScreenFromFile -> input generated-files/ui, output generated-files/clean-ui
        ExtractScreenFromFile.main(new String[0]);
        // COPY from generated-files/clean-ui to generated-files/ui
        copyFiles("./generated-files/clean-ui", "./generated-files/ui");
        
        // Run GenerateNavigationFragment -> input output-files/navigation and processed-files/navigation, output generated-files/compose/navigation
        GenerateNavigationFragment.main(new String[0]);
        
        // Run CleanComposableFile -> input generated-files/compose/navigation, output generated-files/clean-ui/navigation
        CleanComposableFile.main(new String[0]);
        // COPY from generated-files/clean-ui/navigation to generated-files/compose/navigation
        copyFiles("./generated-files/clean-ui/navigation", "./generated-files/compose/navigation");
        
        // Run ReviewImports
        ReviewImports.main(new String[0]);
        copyFiles("./generated-files/ui", "./copy-to-app/ui");
        copyFiles("./generated-files/compose", "./copy-to-app/compose");
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
    
}
