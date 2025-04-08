package com.gdlatte.generator.service;

import com.gdlatte.generator.entity.vo.FileExtension;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
@Service
public class Generator {
    //
    private static final List<String> SKIP_DIRS = List.of(".gradle", ".idea", ".studio", "build", "node_modules", ".storybook", "storybook", "storybook-static", "public");

    private long totalCount = 0L;

    @PostConstruct
    public void init() {

    }

    public void generate(String sourceDirStr, String obsidianBaseDir, Consumer<String> logCallback,BiConsumer<Double,Double> progressCallback) {
        String[] parts = sourceDirStr.split("\\\\");
        String projectName = parts[parts.length - 1];
        Path sourceDir = Paths.get(sourceDirStr);
        Path obsidianDir = Paths.get(obsidianBaseDir, projectName);

        try {
            totalCount = Files.walk(sourceDir)
                    .filter(Files :: isRegularFile)
                    .filter(path -> !isInSkippedDir(path))
                    .filter(path -> FileExtension.hasValidExtension(path.toString()))
                    .count();
        log.info("Total count: {}", totalCount);

            long[] currentCount = {0};
            processFolder(sourceDir, obsidianDir, logCallback , ()->{
                currentCount[0]++;
                progressCallback.accept((double)currentCount[0] , (double)totalCount);
            });
        } catch (IOException e) {
            logCallback.accept("처리 중 오류 발생: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void processFolder(Path sourceDir, Path obsidianDir , Consumer<String> logCallback,Runnable onFileProcessed ) throws IOException {
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

                        copyFileToObsidian(file, obsidianFile, fileExtension , logCallback);
                        onFileProcessed.run();
                        log.info("복사됨 ! -------------");
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (SKIP_DIRS.contains(dir.getFileName().toString().toLowerCase())) {
                        System.out.println("skip dir: " + dir);
                        logCallback.accept(">>skip dir: " + dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            System.out.println("wrong dir");
            logCallback.accept("경로 오류: " + sourceDir);
        }
    }

    private void copyFileToObsidian(Path sourceFile, Path obsidianFile, FileExtension fileExtension , Consumer<String> logCallback) throws IOException {
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
        logCallback.accept("copied: " + obsidianFile);

        //딜레이 테스트...
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
    }

    private String getFileExtensionFromFileName(String fileName) {
        //
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private String changeExtensionToMd(String fileName) {
        //
        return fileName + ".md";
    }

    private void deleteFolder(Path targetPath) {
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

    private boolean isInSkippedDir(Path path) {
        for (Path part : path) {
            if (SKIP_DIRS.contains(part.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
