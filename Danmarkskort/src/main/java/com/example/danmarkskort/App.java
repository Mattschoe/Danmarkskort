package com.example.danmarkskort;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.example.danmarkskort.MVC.View;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Rotternes Danmarkskort");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        new View(stage, "startup.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}