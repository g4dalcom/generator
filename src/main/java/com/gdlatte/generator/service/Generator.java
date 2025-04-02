package com.gdlatte.generator.service;

import com.gdlatte.generator.entity.vo.FileExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class Generator {
    //
    private static final List<String> SKIP_DIRS = List.of(".gradle", ".idea", ".studio", "build", "node_modules", ".storybook", "storybook", "storybook-static", "public");
    private static final String SOURCE_DIR = "E:\\project\\nextree\\garden\\edge-wise\\edge-wise-app";
    private static final String OBSIDIAN_BASE_DIR = "E:\\dev_vault\\Code\\projects\\";

    public static void main(String[] args) {
        //
        generate();
    }

    public static void generate() {
        //
        String[] parts = SOURCE_DIR.split("\\\\");
        String projectName = parts[parts.length - 1];
        String obsidianFolder = OBSIDIAN_BASE_DIR + projectName;

        try {
            processFolder(Paths.get(SOURCE_DIR), Paths.get(obsidianFolder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processFolder(Path sourceDir, Path obsidianDir) throws IOException {
        //
        deleteFolder(obsidianDir);

        if (Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    //
                    String fileName = file.toString().toLowerCase();
                    if (FileExtension.hasValidExtension(fileName)) {
                        String extension = getFileExtensionFromFileName(fileName);
                        FileExtension fileExtension = FileExtension.fromExtension(extension);
                        Path relativePath = sourceDir.relativize(file);
                        Path obsidianFile = obsidianDir.resolve(changeExtensionToMd(relativePath.toString()));

                        Path parentDir = obsidianFile.getParent();
                        if (!Files.exists(parentDir)) {
                            Files.createDirectories(parentDir);
                        }

                        copyFileToObsidian(file, obsidianFile, fileExtension);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (SKIP_DIRS.contains(dir.getFileName().toString().toLowerCase())) {
                        System.out.println("skip dir: " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            System.out.println("wrong dir");
        }
    }

    private static void copyFileToObsidian(Path sourceFile, Path obsidianFile, FileExtension fileExtension) throws IOException {
        //
        List<String> content = Files.readAllLines(sourceFile);
        String identifier = FileExtension.getLanguageIdentifier(fileExtension);

        try (BufferedWriter writer = Files.newBufferedWriter(obsidianFile)) {
            writer.write("```" + identifier + "\n");
            for (String line : content) {
                writer.write(line + "\n");
            }
            writer.write("```\n");
        }

        System.out.println("success: " + obsidianFile);
    }

    private static String getFileExtensionFromFileName(String fileName) {
        //
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private static String changeExtensionToMd(String fileName) {
        //
        return fileName + ".md";
    }

    private static void deleteFolder(Path targetPath) {
        //
        if (Files.exists(targetPath)) {
            try (Stream<Path> walk = Files.walk(targetPath)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                System.out.println("deleted: " + path);
                            } catch (IOException e) {
                                throw new RuntimeException("delete failed: " + path, e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
