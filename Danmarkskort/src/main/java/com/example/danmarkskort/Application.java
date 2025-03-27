package com.example.danmarkskort;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.example.danmarkskort.MVC.View;

import java.io.IOException;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Rotternes Danmarkskort");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        new View(stage, "startup.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}