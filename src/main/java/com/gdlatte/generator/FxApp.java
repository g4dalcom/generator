package com.gdlatte.generator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;


@Slf4j
public class FxApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(GeneratorApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/main.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = null;
        try {
            root = loader.load();
            Object controller = loader.getController();
            log.info("------------------------------");
            log.info("controller: {}", controller);
            log.info("------------------------------");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Generator UI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
