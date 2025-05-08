package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.View;
import com.example.danmarkskort.MapObjects.POI;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobotInterface;
import org.testfx.framework.junit5.ApplicationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ViewTest extends ApplicationTest {
    private View view;

    @Override
    public FxRobotInterface clickOn(Node node, MouseButton... buttons) {
        return super.clickOn(node, buttons);
    }

    private Stage primaryStage;
    String fxmlLocation;
    Controller controller;

    @Override
    public void start(Stage stage) throws Exception {
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
    protected void removeObjectsFromMapTest() throws XMLStreamException, IOException {
        setUpView("mapOverlay.fxml");
        File file = new File("data/testing/viewTestDoc.osm");
        Parser parser = new Parser(file);

        Set<Road> roads = parser.getRoads();
        Road[] roadArray = roads.toArray(new Road[0]);
        view.removeObjectFromDraw(roadArray[0]);

        assertFalse(view.getExtraDrawObjects().contains(roadArray[0]));
    }
}
