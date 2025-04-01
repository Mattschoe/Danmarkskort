package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.*;
import com.example.danmarkskort.Parser;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class View {
    //region Fields
    private Affine trans;
    private Affine backgroundTrans;
    private Canvas canvas;
    private final Controller controller;
    private GraphicsContext graphicsContext;
    private Parser parser;
    private final Scene scene;
    private final Stage stage;
    private boolean firstTimeDrawingMap;
    int currentZoom, minZoom, maxZoom;
    Tilegrid tilegrid;
    List<Tile> visibleTiles;
    //endregion

    /** View-konstruktøren skifter scene ud fra en given stage og filstien til en FXML-fil
     * @param stage givne stage ved start-up fås denne af App's start-metode, ellers genbruger Controlleren Stage'en der allerede vises
     * @param filename givne filsti f.eks. "startup.fxml" til start-scenen
     * @throws IOException kastes hvis programmet fejler i at loade FXML-filen
     */
    public View(Stage stage, String filename) throws IOException {
        //The standard location for fxml. Needs to be added to every filepath
        String fxmlLocation = "/com/example/danmarkskort/" + filename;
        firstTimeDrawingMap = true;

        //Saves the Stage
        this.stage = stage;

        //Skaber en FXMLLoader, klar til at loade den specificerede FXML-fil
        URL url = getClass().getResource(fxmlLocation);
        assert url != null;
        FXMLLoader root = new FXMLLoader(url);

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

        initializeCanvas();

        //Sets up the Zoom levels
        currentZoom = 8;
        minZoom = 1;
        maxZoom = 8;
    }

    ///Giver Canvas en Transform og bunden højde/bredde
    private void initializeCanvas() {
        //Canvas'et og dets GraphicsContext gemmes
        canvas = controller.getCanvas();
        graphicsContext = canvas.getGraphicsContext2D();
        trans   = new Affine();
        backgroundTrans = new Affine();
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

        //Preps the graphicsContext for drawing the map (paints background and sets transform and standard line-width)
        graphicsContext.setTransform(backgroundTrans);
        graphicsContext.setFill(Color.ANTIQUEWHITE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setTransform(trans);
        graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));

        //region TESTING
        //Tegner kun tiles inde for viewport
        if (tilegrid != null) {
            try {
                System.out.println(getLOD());
                tilegrid.drawVisibleTiles(graphicsContext, getViewport(), getLOD());
            } catch (NonInvertibleTransformException e) {
                System.out.println("Error getting viewport! Error: " + e.getMessage());
            }
        }
        //endregion


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

    /// Method pans on the canvas -- STOLEN FROM NUTAN
    public void pan(double dx, double dy) {
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
        if (factor >= 1 && currentZoom > minZoom) currentZoom--; //Zoom ind
        else if (factor <= 1 && currentZoom < maxZoom) currentZoom++; //Zoom out
        else if (ignoreMinMax) { //Needs to be changed

        } else { //If we are not allowed to zoom
            System.out.println("Nuhu");
            return;
        }
        //Zooms
        trans.prependTranslation(-dx, -dy);
        trans.prependScale(factor, factor);
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    /**
     * Changes the current zoom level to a range from 0 to 4 (needed for the LOD)
     */
    private int getLOD() {
        if (currentZoom > maxZoom) return 0;
        if (currentZoom < minZoom) return 4;
        return (maxZoom - currentZoom) * 4 / (maxZoom - minZoom);
    }

    //region GETTERS AND SETTERS
    Stage getStage() { return stage; }
    public void setVisibleTiles(List<Tile> visibleTiles) {
        this.visibleTiles = visibleTiles;
    }
    public double[] getViewport() throws NonInvertibleTransformException {
        Point2D minXY = trans.inverseTransform(0, 0);
        Point2D maxXY = trans.inverseTransform(canvas.getWidth(), canvas.getHeight());
        return new double[]{minXY.getX(), minXY.getY(), maxXY.getX(), maxXY.getY()};
    }
    public void setTilegrid(Tilegrid tilegrid) { this.tilegrid = tilegrid; }
    //endregion
}