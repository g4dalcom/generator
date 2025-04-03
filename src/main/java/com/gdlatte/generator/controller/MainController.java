package com.gdlatte.generator.controller;

import com.gdlatte.generator.service.Generator;
import javafx.fxml.FXML;
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

    private final Generator generator;

    @FXML
    public void onGenerateClicked() {
        String sourceDir = sourceDirField.getText();
        String baseDir = obsidianBaseDirField.getText();

        try {
            generator.generate(sourceDir, baseDir, this::appendResult);
            appendResult("완료되었습니다~!");
        } catch (Exception e) {
            appendResult("오류 발생: " + e.getMessage());
        }
    }

    private void appendResult(String message) {
        resultArea.appendText(message + "\n");
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
