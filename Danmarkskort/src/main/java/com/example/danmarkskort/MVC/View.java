package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.*;
import com.example.danmarkskort.Parser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class View {
    //region fields
    Affine trans;
    Affine background;
    Canvas canvas;
    Controller controller;
    FXMLLoader root;
    String fxmlLocation; //The standard location for fxml. Needs to be added to every filepath
    GraphicsContext graphicsContext;
    Parser parser;
    Scene scene;
    Stage stage;
    boolean firstTimeDrawingMap;
    int currentZoom, minZoom, maxZoom;
    List<Tile> visibleTiles;
    ///The offset of which we moved around in the canvas. Always starts at 0 and accumulates when panning and zooming
    private double viewportOffsetX = 0;
    ///The offset of which we moved around in the canvas. Always starts at 0 and accumulates when panning and zooming
    private double viewportOffsetY = 0;
    //endregion

    /** View-konstruktøren skifter scene ud fra en given stage og filstien til en FXML-fil
     * @param stage givne stage ved start-up fås denne af Application's start-metode, ellers genbruger Controlleren Stage'en der allerede vises
     * @param filename givne filsti f.eks. "startup.fxml" til start-scenen
     * @throws IOException kastes hvis programmet fejler i at loade FXML-filen
     */
    public View(Stage stage, String filename) throws IOException {
        fxmlLocation = "/com/example/danmarkskort/" + filename;
        firstTimeDrawingMap = true;

        //Gemmer Stage'en
        this.stage = stage;

        //Skaber en FXMLLoader, klar til at loade den specificerede FXML-fil
        URL url = getClass().getResource(fxmlLocation);
        assert url != null;
        root = new FXMLLoader(url);

        //Hvis det er start-scenen, får vinduet en forudbestemt størrelse, ellers sættes den dynamisk
        double width, height;
        if (filename.equals("startup.fxml")) {
            width = 600;
            height = 400;
        } else {
            Scene prevScene = stage.getScene();
            width = prevScene.getWidth();
            height = prevScene.getHeight();
        }
        scene = new Scene(root.load(), width, height);

        //Controlleren gemmes i View-objektet og vice versa
        controller = root.getController();
        controller.setView(this);

        //Sætter scenen og fremviser
        stage.setScene(scene);
        stage.show();

        //Hvis vi laver en scene med et Canvas initialiseres og tegnes det
        if (controller.getCanvas() != null) initializeCanvas();

        //Sets up the Zoom levels
        currentZoom = 7;
        minZoom = 1;
        maxZoom = 6;
    }

    ///Giver Canvas en Transform og bunden højde/bredde
    private void initializeCanvas() {
        //Canvas'et og dets GraphicsContext gemmes
        canvas = controller.getCanvas();
        graphicsContext = canvas.getGraphicsContext2D();
        trans = new Affine();
        background = new Affine();
        graphicsContext.setTransform(trans);

        //Canvas højde og bredde bindes til vinduets
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        //Listeners tilføjes, der redrawer Canvas'et når vinduet skifter størrelse
        scene.widthProperty().addListener(_ -> drawMap(parser));
        scene.heightProperty().addListener(_ -> drawMap(parser));
    }

    /**
     * Draws the whole map given a parser.
     * @param parser the parser that model has stored
     */
    public void drawMap(Parser parser) {
        if (parser == null) return; //TODO %% Evt. find en bedre måde at sørge for at initializeCanvas IKKE køres før kortet loades
        assert graphicsContext != null && canvas != null;
        this.parser = parser;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        //Preps the graphicsContext for drawing the map (paints background and sets transform and standard line-width)
        graphicsContext.setTransform(background);
        graphicsContext.setFill(Color.ANTIQUEWHITE);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setTransform(trans);
        graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));

        //region TESTING
        //Tegner kun tiles inde for viewport
        if (visibleTiles != null) {
            //System.out.println(visibleTiles.size());
            for (Tile tile : visibleTiles) {
                tile.draw(graphicsContext);
            }
        }
        //endregion

        /*
        int zoomPercentage = (int) (((double) currentZoom/maxZoom) * 100);
        int fullDetails = 40; //% when all details should be drawn
        int mediumDetails = 70; //% when a balanced amount of details should be drawn
        // System.out.println(zoomPercentage);
        if (zoomPercentage < fullDetails && zoomPercentage < mediumDetails) { //Draws with all details
            // System.out.println("All details");
            drawAllRoads();
            drawAllPolygons(true);
        } else if (zoomPercentage < mediumDetails) { //Draws with some details
            // System.out.println("medium details");
            drawAllRoads();
            drawAllPolygons(true);
        } else { //Draws the map with the least amount of details
            // System.out.println("minimum details");
            drawAllSignificantHighways();
            drawAllPolygons(false);
        }
        */

        if (firstTimeDrawingMap) {
            System.out.println("Finished first time drawing!");
            firstTimeDrawingMap = false;

            //TODO: SKAL OPTIMERES VI DRAWER MAPPET LIKE 5 GANGE FØRSTE GANG
            //Moves the view over to the map
            double startZoom = (0.95 * canvas.getHeight() / (parser.getBounds()[2] - parser.getBounds()[0]));
            //pan(-0.5599 * parser.getBounds()[1], parser.getBounds()[2]);
            //zoom(0, 0, startZoom, true);
        }
    }




    ///STJÅLET FRA NUTAN
    public void pan(double dx, double dy) {
        //Saves the offset
        viewportOffsetX += dx;
        viewportOffsetY += dy;

        //Moves the map
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    /**
     * Zooms in and out, zoom level is limited by {@code minZoom} and {@code maxZoom} which can be changed in the constructor
     * @param dx deltaX
     * @param dy deltaY
     * @param factor of zooming in. 1 = same level, >1 = Zoom in, <1 = Zoom out
     */
    public void zoom(double dx, double dy, double factor, boolean ignoreMinMax) {
        if (factor >= 1 && currentZoom > minZoom) { //Zoom ind
            currentZoom--;
            pan(-dx, -dy);
            trans.prependScale(factor, factor);
            pan(dx, dy);
            drawMap(parser);
        } else if (factor <= 1 && currentZoom < maxZoom) { //Zoom out
            currentZoom++;
            pan(-dx, -dy);
            trans.prependScale(factor, factor);
            pan(dx, dy);
            drawMap(parser);
        } else if (ignoreMinMax) {
            pan(-dx, -dy);
            trans.prependScale(factor, factor);
            pan(dx, dy);
            drawMap(parser);
        }
    }

    ///Draws all roads. Method is called in {@link #drawMap(Parser)}
    private void drawAllRoads() {
        Road road;
        for (long id : parser.getRoads().keySet()) {
            road = parser.getRoads().get(id);
            if (road.getRoadType().equals("route")) continue;
            road.draw(graphicsContext);
        }
    }

    ///Draws all polygons (buildings etc.). Method is called in {@link #drawMap(Parser)}
    private void drawAllPolygons(boolean drawLines) {
        Polygon polygon;
        for (long id : parser.getPolygons().keySet()) {
            polygon = parser.getPolygons().get(id);
            polygon.draw(graphicsContext, drawLines);
        }
    }

    private void drawAllSignificantHighways() {
        for (Road road : parser.getSignificantHighways()) {
            road.draw(graphicsContext);
        }
    }

    //region GETTERS AND SETTERS
    Stage getStage() { return stage; }
    public void setVisibleTiles(List<Tile> visibleTiles) {
        this.visibleTiles = visibleTiles;
    }
    public double getViewportOffsetX() { return viewportOffsetX; }
    public double getViewportOffsetY() { return viewportOffsetY; }
    //endregion
}