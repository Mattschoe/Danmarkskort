package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.View;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ViewTest extends ApplicationTest {
    private View view;
    private Stage primaryStage;
    String fxmlLocation;
    Controller controller;

    @Override
    public void start(Stage stage) throws Exception {
       Controller controller = new Controller();
        this.primaryStage = stage;
        //view = new View(stage, "newStart.fxml");

    }

    private void setUpView(String fxmlFile) {
        interact(() -> {
            try {
                this.view = new View(primaryStage, fxmlFile);
            } catch (IOException e) {
                fail("Failed to load FXML: " + fxmlFile);
            }
        });
    }

    /**
     * Tests if view is created correctly
     */
    @Disabled @Test
    protected void testViewIsCreated() {
        assertNotNull(view, "View should not be null");
    }

    @Test
    public void testViewWithStartScene() {
        setUpView("newStart.fxml");
        assertNotNull(view);
        assertTrue(view.getStage().isShowing());
    }

    @Disabled @Test
    public void testViewWithAnotherScene() throws IOException {
        try {
                setUpView("mapOverlay.fxml");
                assertFalse(view.isFirstTimeDrawingMap()); //make sure the map is drawn when mapOverlay is the stage
                assertNotNull(view);
                assertTrue(view.getStage().isShowing());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


    }


    @Test
    protected void testDrawMap(){
        interact(() -> {

            setUpView("newStart.fxml");
            setUpView("mapOverlay.fxml");
            view.drawMap();
            assertFalse(view.isFirstTimeDrawingMap());
        });
    }

    @Test
    //test to check that
    protected void zoomToTest(){

    }
}
