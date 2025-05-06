package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Road;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import org.testfx.framework.junit5.ApplicationTest;
import com.example.danmarkskort.MVC.View;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest extends ApplicationTest{
    //...
    View view;
    Stage stage;
    Model model;
    Canvas canvas;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
       this.canvas = new Canvas(600, 400);

            try {
                view = new View(primaryStage, "newStart.fxml");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    @Test
    protected void createControllerTest() {
       try{ assertNotNull(view.getFXMLLoader().getController());}
       catch (Exception e){fail("Failed to create controller: " + e.getMessage());}
    }


    @Deprecated
    @Test
    protected void POITest(){
//vent med denne til POIs er færdige
    }

    @Deprecated
    @Test
    protected void setSearchTypeTest() throws XMLStreamException, IOException {
        Canvas canvas = new Canvas(600, 400);
        File file = new File("data/Bornholm.zip");
        assertTrue(file.exists(), "File does not exist: " + file.getAbsolutePath());
        Model model = new Model("data/Bornholm.zip", canvas, false);
        Controller controller = view.getFXMLLoader().getController();
        controller.shortestRoute();

        assertFalse(model.getSearch().isQuickestRoute());
    }

    @Test
    protected void exportAsPDFTest() throws IOException, XMLStreamException, InvocationTargetException, IllegalAccessException {
        File file = new File("data/Bornholm.zip");
        assertTrue(file.exists(), "File does not exist: " + file.getAbsolutePath());
        Model model = new Model("data/Bornholm.zip", canvas, false);
        Controller controller = view.getFXMLLoader().getController();

        List<Road> route = model.getSearch().getRoute();

    }
}
