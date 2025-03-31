package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
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

public class View {
    //region Fields
    private Affine trans;
    private Affine bgTrans;
    private Canvas canvas;
    private final Controller controller;
    private GraphicsContext graphicsContext;
    private Parser parser;
    private final Scene scene;
    private final Stage stage;
    private boolean firstTimeDrawingMap;
    int currentZoom, minZoom, maxZoom;
    //endregion

    //region Constructor(s)
    /** The View-constructor switches the scene from a given stage and a filepath to an FXML-file
     * @param stage the given stage -- usually coming from the window we're in, as to not open a new window
     * @param filename the given filepath -- fx. "startup.fxml" for the start-up scene
     * @throws IOException thrown if the program fails to load the FXML-file
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

        /* Her plejede at være et if-statement ift. hvorvidt controller.getCanvas() var null, men dette
         * blev redundant efter Matthias tilføjede instansiering af canvas i Controller's konstruktør */
        initializeCanvas();

        //Sets up the Zoom levels
        currentZoom = 7;
        minZoom = 1;
        maxZoom = 6;
    }
    //endregion

    //region Methods
    /// Sets the canvas' transform, and binds its height and width
    private void initializeCanvas() {
        //Canvas'et og dets GraphicsContext gemmes
        canvas = controller.getCanvas();
        graphicsContext = canvas.getGraphicsContext2D();
        trans   = new Affine();
        bgTrans = new Affine();
        graphicsContext.setTransform(trans);

        //Canvas højde og bredde bindes til vinduets
        canvas.widthProperty() .bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        //Listeners tilføjes, der redrawer Canvas'et når vinduet skifter størrelse
        scene.widthProperty() .addListener(_ -> drawMap(parser));
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
        graphicsContext.setTransform(bgTrans);
        graphicsContext.setFill(Color.ANTIQUEWHITE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setTransform(trans);
        graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));

        //Draws map
        drawPolygons();
        int zoomPercentage = (int) (((double) currentZoom/maxZoom) * 100);
        int fullDetails = 40; //% when all details should be drawn
        int mediumDetails = 70; //% when a balanced amount of details should be drawn
        if (zoomPercentage < fullDetails && zoomPercentage < mediumDetails) {
            //Draws with all details
        } else if (zoomPercentage < mediumDetails) {
            //Draws with some details
        } else {
            //Draws the map with least amount of details
        }

        drawPolygons();
        drawRoads();

        if (firstTimeDrawingMap) {
            System.out.println("Finished first time drawing!");
            firstTimeDrawingMap = false;

            pan(-0.5599 * parser.getBounds()[1], parser.getBounds()[2]);
            zoom(0, 0, 0.95 * canvas.getHeight() / (parser.getBounds()[2] - parser.getBounds()[0]));
        }

    }

    /// Method pans on the canvas -- STOLEN FROM NUTAN
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    /**
     * Zooms in and out, zoom level is limited by {@code minZoom} and {@code maxZoom} which can be changed in the constructor
     * @param dx deltaX
     * @param dy deltaY
     * @param factor of zooming in. 1 = same level, >1 = Zoom in, <1 = Zoom out
     */
    public void zoom(double dx, double dy, double factor) {
        if (factor >= 1 && currentZoom > minZoom) { //Zoom ind
            currentZoom--;
            trans.prependTranslation(-dx, -dy);
            trans.prependScale(factor, factor);
            trans.prependTranslation(dx, dy);
            drawMap(parser);
        } else if (factor <= 1 && currentZoom < maxZoom) { //Zoom out
            currentZoom++;
            trans.prependTranslation(-dx, -dy);
            trans.prependScale(factor, factor);
            trans.prependTranslation(dx, dy);
            drawMap(parser);
        }
    }

    /// Draws all roads. Method is called in {@link #drawMap(Parser)}
    private void drawRoads() {
        for (Road r : parser.getRoads().values()) {
            if (r.getType().equals("route")) continue;
            r.drawRoad(graphicsContext);
        }
    }

    /// Draws all polygons (buildings etc.). Method is called in {@link #drawMap(Parser)}
    private void drawPolygons() {
        for (Polygon p : parser.getPolygons().values()) {
            p.drawPolygon(graphicsContext);
        }
    }
    //endregion

    //region Getter and setters
    Stage  getStage() { return stage; }
    Affine getTrans() { return trans; }
    //endregion
}