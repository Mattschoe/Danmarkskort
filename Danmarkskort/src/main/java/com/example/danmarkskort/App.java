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
        stage.setTitle("Rats' Map of Denmark");
        stage.setMinWidth(428);
        stage.setMinHeight(312);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png")));
        stage.getIcons().add(icon);

        new View(stage, "newStart.fxml");
    }

    public static void main(String[] args) { launch(); }
}