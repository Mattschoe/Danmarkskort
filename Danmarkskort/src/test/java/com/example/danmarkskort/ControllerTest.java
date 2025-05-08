package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.MapObjects.Tilegrid;
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

    @Test
    protected void hoverPanZoomAndFPSTest() {
        model = Model.getInstance("data/small.zip", canvas, false);

        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml"); //Skifter View
            controller = view.getController();

            controller.getFPSButton().setSelected(true);
            assertNotEquals("", controller.getFPSButton().getText()); //FPS-teksten skulle gerne være opdateret fra blank

            Robot robot = new Robot();
            robot.setAutoDelay(10);

            //Snupper app-vinduets x/y-koordinater og tiløjer hhv. 100/47.5
            int x = (int) stage.getScene().getWindow().getX() + 100;
            int y = (int) stage.getScene().getWindow().getY() + 48;
            robot.mouseMove(x, y); //Musen navigerer til søgebaren

            clickMouseBtn(robot, InputEvent.BUTTON1_DOWN_MASK);
            clickKey(robot, KeyEvent.VK_T);
            clickKey(robot, KeyEvent.VK_I);
            clickKey(robot, KeyEvent.VK_N);
            clickKey(robot, KeyEvent.VK_ENTER);

            x = (int) stage.getScene().getWindow().getX() + 300;
            y = (int) stage.getScene().getWindow().getY() + 200;
            robot.mouseMove(x, y); //Rykker ind midt i programmet

            robot.mouseWheel(-1); //Zoomer ind én gang

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            x = (int) stage.getScene().getWindow().getX() + 275;
            y = (int) stage.getScene().getWindow().getY() + 175;
            robot.mouseMove(x, y); //Rykker ind midt i programmet
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            //FPS-teksten skulle gerne være opdateret fra 0 (der er gået mere end et sekund)
            assertNotEquals("FPS: 0", controller.getFPSButton().getText());
        }));
    }

    @Test
    protected void searchBarsAndListViewTest() {
        //Loader en fil
        model = Model.getInstance("data/small.zip", canvas, false);

        interact(() -> assertDoesNotThrow(() -> {
            view = new View(stage, "mapOverlay.fxml"); //Skifter View
            controller = view.getController();

            Robot robot = new Robot();
            robot.setAutoDelay(10);

            //Snupper app-vinduets x/y-koordinater og tiløjer hhv. 100/47.5
            int x = (int) stage.getScene().getWindow().getX() + 100;
            int y = (int) stage.getScene().getWindow().getY() + 48;
            robot.mouseMove( x, y); //Musen navigerer til søgebaren
            clickMouseBtn(robot, InputEvent.BUTTON1_DOWN_MASK);

            //Den her næste lange serie af knap-klik sikrer at søgebarerne ikke kaster nogle errors,
            //når de bliver interageret med på forskellige måder.
            controller.getSearchBar().setText("Kø");
            clickKey(robot, KeyEvent.VK_B);
            clickKey(robot, KeyEvent.VK_BACK_SPACE);
            clickKey(robot, KeyEvent.VK_BACK_SPACE);
            clickKey(robot, KeyEvent.VK_BACK_SPACE);
            clickKey(robot, KeyEvent.VK_T);
            clickKey(robot, KeyEvent.VK_I);
            clickKey(robot, KeyEvent.VK_N);
            clickKey(robot, KeyEvent.VK_DOWN);
            clickKey(robot, KeyEvent.VK_ENTER);
            clickMouseBtn(robot, InputEvent.BUTTON1_DOWN_MASK);
            clickKey(robot, KeyEvent.VK_ENTER);
            clickMouseBtn(robot, InputEvent.BUTTON1_DOWN_MASK);
            clickKey(robot, KeyEvent.VK_DOWN);
            clickKey(robot, KeyEvent.VK_BACK_SPACE);

            x = (int) stage.getScene().getWindow().getX() + 100;
            y = (int) stage.getScene().getWindow().getY() + 73;
            robot.mouseMove( x, y); //Musen navigerer ned til ListView'en og vælger den første entry
            clickMouseBtn(robot, InputEvent.BUTTON1_DOWN_MASK);
        }));
    }

    private static void clickKey(Robot robot, int key) {
        robot.keyPress(key);
        robot.keyRelease(key);
    }

    private static void clickMouseBtn(Robot robot, int button) {
        robot.mousePress(button);
        robot.mouseRelease(button);
    }
}
