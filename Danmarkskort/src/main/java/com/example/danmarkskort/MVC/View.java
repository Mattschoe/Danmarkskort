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
    //region fields
    private Affine trans;
    private Affine bgTrans;
    private Canvas canvas;
    private final Controller controller;
    private GraphicsContext graphicsContext;
    private Parser parser;
    private final Scene scene;
    private final Stage stage;
    private boolean firstTimeDrawingMap;
    //endregion

    /** View-konstruktøren skifter scene ud fra en given stage og filstien til en FXML-fil
     * @param stage givne stage -- ved start-up fås denne af Application's start-metode, ellers genbruger Controlleren Stage'en der allerede vises
     * @param filename givne filsti -- f.eks. "startup.fxml" til start-scenen
     * @throws IOException kastes hvis programmet fejler i at loade FXML-filen
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
    }

    ///Giver Canvas en Transform og bunden højde/bredde
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
        drawRoads();
        drawPolygons();

        if (firstTimeDrawingMap) {
            firstTimeDrawingMap = false;
            System.out.println("Done drawing!");

            //STJÅLET FRA NUTAN
            pan(-0.5599 * parser.getBounds()[1], parser.getBounds()[2]);
            zoom(0, 0, 0.95 * canvas.getHeight() / (parser.getBounds()[2] - parser.getBounds()[0]));
        }
    }

    ///STJÅLET FRA NUTAN
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        drawMap(parser);
    }

    ///STJÅLET FRA NUTAN
    public void zoom(double dx, double dy, double factor) {
        pan(-dx, -dy);
        trans.prependScale(factor, factor);
        pan(dx, dy);
        drawMap(parser);
    }

    ///Draws all roads. Method is called in {@link #drawMap(Parser)}
    private void drawRoads() {
        Road road;
        for (long id : parser.getRoads().keySet()) {
            road = parser.getRoads().get(id);
            if (road.getRoadType().equals("route")) continue;
            road.drawRoad(canvas);
        }
    }

    ///Draws all polygons (buildings etc.). Method is called in {@link #drawMap(Parser)}
    private void drawPolygons() {
        Polygon polygon;
        for (long id : parser.getPolygons().keySet()) {
            polygon = parser.getPolygons().get(id);
            polygon.drawPolygon(graphicsContext);
        }
    }

    //GETTERS AND SETTERS
    Stage  getStage() { return stage; }
    Affine getTrans() { return trans; }
}