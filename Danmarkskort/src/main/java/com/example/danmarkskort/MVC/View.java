package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.MapObjects.Tilegrid;
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
    private Affine bgTrans;
    private Canvas canvas;
    private final Controller controller;
    private GraphicsContext graphicsContext;
    private Parser parser;
    private final Scene scene;
    private final Stage stage;
    private boolean firstTimeDrawingMap;
    private int currentZoom, minZoom, maxZoom;
    private List<Tile> visibleTiles;
    private Tilegrid tilegrid;
    //endregion

    //region Constructor(s)
    /** The View-constructor switches the scene from a given stage and a filepath to an FXML-file
     *  @param stage the given stage -- usually coming from the window we're in, as to not open a new window
     *  @param filename the given filepath -- fx. "startup.fxml" for the start-up scene
     *  @throws IOException thrown if the program fails to load the FXML-file
     */
    public View(Stage stage, String filename) throws IOException {
        //The standard location for fxml. Needs to be added to every filepath
        String fxmlLocation = "/com/example/danmarkskort/" + filename;
        firstTimeDrawingMap = true;

        //Gemmer Stage'en
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
        minZoom = 1;
        maxZoom = 15;
        currentZoom = maxZoom;
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
        controller.bindZoomBar();

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
        graphicsContext.setTransform(bgTrans);
        graphicsContext.setFill(Color.LIGHTBLUE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        graphicsContext.setTransform(trans);
        graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));

        //Draws map
        //region TESTING
        //Tegner kun tiles inde for viewport
        if (tilegrid != null) {
            try {
                tilegrid.drawVisibleTiles(graphicsContext, getViewport(), getLOD());
                //tilegrid.drawVisibleTiles(graphicsContext, getViewport(), 4);
            } catch (NonInvertibleTransformException e) {
                System.out.println("Error getting viewport! Error: " + e.getMessage());
            }
        }
        //endregion


        if (firstTimeDrawingMap) {
            System.out.println("Finished first time drawing!");
            firstTimeDrawingMap = false;
        }
    }

    /// Method pans on the canvas -- STOLEN FROM NUTAN
    public void pan(double dx, double dy) {
        //Moves the map
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    /** Zooms in and out, zoom level is limited by {@code minZoom} and {@code maxZoom} which can be changed in the constructor
     *  @param dx deltaX
     *  @param dy deltaY
     *  @param factor of zooming in. 1 = same level, >1 = Zoom in, <1 = Zoom out
     */
    public void zoom(double dx, double dy, double factor, boolean ignoreMinMax) {
        /*if (factor >= 1 && currentZoom > minZoom) currentZoom--; //Zoom ind
        else if (factor <= 1 && currentZoom < maxZoom) currentZoom++; //Zoom out
        else if (ignoreMinMax) {
            //Needs to be changed
        } else {
            //If we are not allowed to zoom
            System.out.println("Nuhu");
            return;
        }*/

        //Zooms
        trans.prependTranslation(-dx, -dy);
        trans.prependScale(factor, factor);
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    /// Changes the current zoom level to a range from 0 to 4 (needed for the LOD). 0 is minimum amount of details, 4 is maximum
    private int getLOD() {
        if (trans.getMxx() > 65) return 4;
        if (trans.getMxx() > 40) return 3;
        if (trans.getMxx() > 8)  return 2;
        if (trans.getMxx() > 4)  return 1;
        else return 0;
    }
    //endregion

    //region Getters and setters
    public Stage getStage() { return stage; }
    public Affine getTrans() { return trans; }
    public void setVisibleTiles(List<Tile> visibleTiles) {
        this.visibleTiles = visibleTiles;
    }
    public float[] getViewport() throws NonInvertibleTransformException {
        Point2D minXY = trans.inverseTransform(0, 0);
        Point2D maxXY = trans.inverseTransform(canvas.getWidth(), canvas.getHeight());
        return new float[]{(float) minXY.getX(), (float) minXY.getY(), (float) maxXY.getX(), (float) maxXY.getY()};
    }
    public void setTilegrid(Tilegrid tilegrid) { this.tilegrid = tilegrid; }
    //endregion
}