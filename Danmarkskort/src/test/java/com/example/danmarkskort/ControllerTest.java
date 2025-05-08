package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.MapObjects.Tilegrid;
import com.itextpdf.text.DocumentException;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Disabled;
import org.testfx.framework.junit5.ApplicationTest;
import com.example.danmarkskort.MVC.View;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.awt.AWTException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ControllerTest extends ApplicationTest {
    View view;
    Stage stage;
    Model model;
    Canvas canvas;
    Controller controller;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.canvas = new Canvas(600, 400);

        try {
            view = new View(stage, "newStart.fxml");
            controller = view.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    protected void createControllerTest() {
       try{ assertNotNull(view.getFXMLLoader().getController());}
       catch (Exception e){fail("Failed to create controller: " + e.getMessage());}
    }

    @Disabled @Test
    protected void setSearchTypeTest() {
        Canvas canvas = new Canvas(600, 400);
        File file = new File("data/Bornholm.zip");
        assertTrue(file.exists(), "File does not exist: " + file.getAbsolutePath());
        Model model = new Model("data/Bornholm.zip", canvas, false);
        Controller controller = view.getFXMLLoader().getController();
        controller.shortestRoute();

        assertFalse(model.getSearch().isQuickestRoute());
    }

    @Test
    protected void exportAsPDFTest() {
        File file = new File("data/Bornholm.zip");
        assertTrue(file.exists(), "File does not exist: " + file.getAbsolutePath());
        Model model = new Model("data/Bornholm.zip", canvas, false);
        Controller controller = view.getFXMLLoader().getController();

        List<Road> route = model.getSearch().getRoute();
    }

    @Test
    protected void standardInputTest() {
        assertDoesNotThrow(() -> interact(() -> {
            try { controller.standardInputButton(); }
            catch (IOException e) { throw new RuntimeException(e); }
        }));
    }

    @Test
    protected void uploadInputTest() {
        new Thread(() -> {
            try {
                Robot robot = new Robot();
                robot.delay(1_000);
                robot.keyPress(KeyEvent.VK_ESCAPE);
            } catch (AWTException e) {
                fail("UploadInputTest failed!");
            }
        }).start();

        interact(() ->
            assertDoesNotThrow(() -> controller.uploadInputButton())
        );
    }

    @Test
    protected void paletteButtonsTest() {
        //Loader en fil
        model = Model.getInstance("data/small.zip", canvas, false);

        //Skifter View
        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml");
            controller = view.getController();
        }));

        //Henter et tile
        Tilegrid tilegrid = model.getTilegrid();
        Tile tile = tilegrid.getTileFromXY(408.02264404296875f, 386.693603515625f);

        //Snupper en konkret bygning
        Polygon polygon = (Polygon) tile.getObjectsInTile().getLast();

        //Tjekker at bygningens farve skifter rigtigt når palette-knapperne klikkes
        assertEquals(Color.rgb(217, 208, 201), polygon.getColor());

        controller.paletteMidnight();
        assertEquals(Color.rgb(255, 255, 255, 0.2), polygon.getColor());

        controller.paletteBasic();
        assertEquals(Color.rgb(175, 175, 175, 0.3), polygon.getColor());

        controller.paletteDefault();
        assertEquals(Color.rgb(217, 208, 201), polygon.getColor());
    }

    @Test
    protected void toggleCreateOBJButtonTest() {
        CheckBox checkBoxOBJ = controller.getCheckBoxOBJ();
        StackPane box = (StackPane) checkBoxOBJ.getChildrenUnmodifiable().getLast();
        StackPane mark = (StackPane) box.getChildrenUnmodifiable().getFirst();

        assertEquals("-fx-border-color: darkgrey; -fx-background-color: grey", box.getStyle());
        assertEquals("-fx-background-color: darkgrey", mark.getStyle());

        checkBoxOBJ.setSelected(true);
        controller.toggleCreateOBJ();

        assertEquals("-fx-border-color: darkgreen; -fx-background-color: green", box.getStyle());
        assertEquals("-fx-background-color: #ffffff", mark.getStyle());

        checkBoxOBJ.setSelected(false);
        controller.toggleCreateOBJ();

        assertEquals("-fx-border-color: darkgrey; -fx-background-color: grey", box.getStyle());
        assertEquals("-fx-background-color: darkgrey", mark.getStyle());
    }

    @Test
    protected void makeRouteAndSavePOITest() {
        //Loader en fil
        model = Model.getInstance("data/small.zip", canvas, false);

        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml"); //Skifter View
            controller = view.getController();

            controller.getSearchBar().setText("Sverrigsgade 28, 2300 København S");
            controller.findRouteClicked();

            controller.getDestination().setText("Ny Østergade 4, 1101 København K");
            controller.switchDestinationAndStart();
            controller.findRouteClicked();

            controller.setSearchingSource(controller.getSearchBar()); //Sætter lige fokus i søgebaren
            controller.getAddNamePOI().setText("TestPOI");
            controller.savePOItoHashMap();
        }));
    }

    @Disabled
    @Test
    protected void searchBarsAndListViewTest() {
        //Loader en fil
        model = Model.getInstance("data/small.zip", canvas, false);

        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml"); //Skifter View
            controller = view.getController();

            Robot robot = new Robot();
            robot.setAutoDelay(200); //200 millisekunder mellem hver action, så risikerer vi ikke at det går for stærkt

            robot.mouseMove(450, 135); //Navigerer til søgebaren
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); //Klikker ned med musen, så søgebaren får fokus
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); //Musen giver slip på klikket

            robot.keyPress(KeyEvent.VK_T); //Klikker 't'
            robot.keyPress(KeyEvent.VK_I); //Klikker 'i'
            robot.keyPress(KeyEvent.VK_N); //Klikker 'n'
            robot.keyPress(KeyEvent.VK_DOWN); //Klikker pil ned, ListView åbner og får fokus
            robot.keyPress(KeyEvent.VK_ENTER); //Klikker enter; adressen "Tinghuset 44G, 1440 København K" kommer i søgebaren

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); //Klikker ned med musen igen, søgebaren får fokus
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); //Musen giver slip på klikket

            robot.keyPress(KeyEvent.VK_ENTER); //Klikker 'Enter', 'Find Route'-knappen får fokus

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); //Klikker ned med musen igen, søgebaren får fokus
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); //Musen giver slip på klikket

            robot.keyPress(KeyEvent.VK_DOWN); //Klikker pil ned, rykker forrest i teksten i søgebaren
            robot.keyPress(KeyEvent.VK_BACK_SPACE); //Sletter et bogstav så ListView åbner igen og får fokus

            robot.mouseMove(450, 160); //Flytter musen ned på ListView'en
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); //Klikker ned med musen, vælger en adresse fra ListView'en
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); //Musen giver slip på klikket
        }));
    }
    @Disabled
    @Test
    protected void canvasHoverPanZoomAndFPSTest() {
        //Loader en fil
        model = Model.getInstance("data/small.zip", canvas, false);

        //long start = System.currentTimeMillis();
        //noinspection StatementWithEmptyBody
        //while (System.currentTimeMillis() - start < 5_000) {/* wait */}

        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml"); //Skifter View
            controller = view.getController();

            controller.getFPSButton().setSelected(true);
            assertNotEquals("", controller.getFPSButton().getText()); //FPS-teksten skulle gerne være opdateret fra blank

            Robot robot = new Robot();
            robot.setAutoDelay(10);

            robot.mouseMove(450, 135); //Navigerer til søgebaren
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); //Klikker ned med musen, så søgebaren får fokus
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); //Musen giver slip på klikket
            robot.keyPress(KeyEvent.VK_T); //Klikker 't'
            robot.keyPress(KeyEvent.VK_I); //Klikker 'i'
            robot.keyPress(KeyEvent.VK_N); //Klikker 'n'
            robot.keyPress(KeyEvent.VK_ENTER); //Klikker 'Enter'
            robot.keyRelease(KeyEvent.VK_ENTER); //Giver slip på 'Enter'

            robot.mouseMove(730, 470); //Dragger musen
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            for (int i = 0; i < 100; ++i) {
                int x = 730 - (2*i);
                int y = 470 - (2*i);
                robot.mouseMove(x, y);
            }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseWheel(-1); //Zoomer ind én gang

            for (int i = 0; i < 50; ++i) {
                int y = 470 - (2*i);
                robot.mouseMove(730, y);
            }

            //FPS-teksten skulle gerne være opdateret fra 0 (der er gået mere end et sekund)
            assertNotEquals("FPS: 0", controller.getFPSButton().getText());
        }));
    }
}
