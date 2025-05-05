package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.View;
import com.example.danmarkskort.MapObjects.Tilegrid;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.application.Application;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class ViewTest extends ApplicationTest {
    private View view;

    @Override
    public void start(Stage stage) throws Exception {
        // Adjust filename if needed
        view = new View(stage, "newStart.fxml");
    }

    /***
     * Tests if view is created correctly
     */
    @Test
    protected void testViewIsCreated() {
        assertNotNull(view, "View should not be null");
    }

    @Test
    protected void testDrawMap(){
        interact(() -> {
            view.drawMap();
            assertFalse(view.isFirstTimeDrawingMap());
        });
    }

    @Test
    protected void LODTest(){

    }
}
