package com.gdlatte.generator.controller;

import com.gdlatte.generator.service.Generator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
@RequiredArgsConstructor
public class MainController{


    @FXML private TextField sourceDirField;
    @FXML private TextField obsidianBaseDirField;
    @FXML private TextArea resultArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    private final Generator generator;

    @FXML
    public void onGenerateClicked() {
        String sourceDir = sourceDirField.getText();
        String baseDir = obsidianBaseDirField.getText();

        Platform.runLater(() -> {
            progressBar.setVisible(true);
            progressBar.setManaged(true);
            progressBar.setProgress(0);
            progressLabel.setVisible(true);
        });

        new Thread(() -> {
            try {
                generator.generate(
                        sourceDir,
                        baseDir,
                        this::appendResult,
                        (current, total) -> {
                            double progress = (total == 0) ? 0 : current / total;
                            System.out.println("progress: " + progress);
                            System.out.println("total: " + total);

                            Platform.runLater(() -> {
                                try {
                                    if (progressBar != null) {
                                        progressBar.setProgress(progress);
                                        progressLabel.setText(String.format("진행률: %.2f%%", progress * 100));
                                    } else {
                                        System.out.println("ProgressBar가 null인 상태입니다.");
                                    }
                                } catch (Exception e) {
                                    appendResult("ProgressBar 업데이트 중 예외 발생: " + e.getMessage());
                                }
                            });

                        });
                Platform.runLater(() -> {
                    appendResult("완료되었습니다~!");
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    progressLabel.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    appendResult("오류 발생: " + e.getMessage());
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    progressLabel.setVisible(false);
                });
            }
        }).start();
    }

    private void appendResult(String message) {
        Platform.runLater(() -> {
            int maxLines = 500;
            String[] lines = resultArea.getText().split("\n");
            if (lines.length >= maxLines) {

                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 100; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                resultArea.setText(sb.toString());
            }
            resultArea.appendText(message + "\n");
        });
    }

    @FXML
    public void onBrowseSourceDir() {
        File selectedDir = showDirectoryChooser("Select Source Directory");
        if (selectedDir != null) {
            sourceDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    public void onBrowseObsidianDir() {
        File selectedDir = showDirectoryChooser("Select Obsidian Base Directory");
        if (selectedDir != null) {
            obsidianBaseDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    private File showDirectoryChooser(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(getPrimaryStage());
    }

    private Stage getPrimaryStage() {
        return (Stage) sourceDirField.getScene().getWindow();
    }
}
