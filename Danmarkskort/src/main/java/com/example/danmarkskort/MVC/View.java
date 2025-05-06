package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.MapObject;
import com.example.danmarkskort.MapObjects.Tilegrid;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class View {
    //region Fields
    private Affine trans;
    private Affine bgTrans;
    private Canvas canvas;
    private transient Color bgColor;
    private transient Color scaleColor;
    private final Controller controller;
    private GraphicsContext graphicsContext;
    private final Scene scene;
    private final Stage stage;
    private boolean firstTimeDrawingMap;
    private Tilegrid tilegrid;
    /// Extra objects outside the grid that are included in draw method. Objects are added in {@link #addObjectToDraw(MapObject)}
    private final Set<MapObject> extraDrawObjects;
    //endregion

    //region Constructor(s)
    /**
     * The View-constructor switches the scene from a given stage and a filepath to an FXML-file
     * @param stage the given stage -- usually coming from the window we're in, as to not open a new window
     * @param filename the given filepath -- fx. "startup.fxml" for the start-up scene
     * @throws IOException thrown if the program fails to load the FXML-file
     */
    public View(Stage stage, String filename) throws IOException {
        extraDrawObjects = new HashSet<>();

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
        if (filename.equals("newStart.fxml")) {
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

        if (controller.getCanvas() != null) initializeCanvas();
        if (controller.getCheckBoxOBJ() != null) fixCheckBox();
    }
    //endregion

    //region Methods
    /// Sets the canvas' transform, and binds its height and width
    private void initializeCanvas() {
        //Canvas'et og dets GraphicsContext gemmes
        canvas = controller.getCanvas();
        graphicsContext = canvas.getGraphicsContext2D();
        trans = new Affine();
        bgTrans = new Affine();
        bgColor = Color.LIGHTBLUE;
        scaleColor = Color.BLACK;
        graphicsContext.setTransform(trans);

        //Canvas højde og bredde bindes til vinduets
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(stage.heightProperty());

        //Listeners tilføjes, der redrawer Canvas'et når vinduet skifter størrelse
        scene.widthProperty().addListener(_ -> drawMap());
        scene.heightProperty().addListener(_ -> drawMap());
    }

    /// Draws the whole map in the tiles visible
    public void drawMap() {
        assert graphicsContext != null && canvas != null;

        //Preps the graphicsContext for drawing the map (paints background and sets transform and standard line-width)
        graphicsContext.setTransform(bgTrans);
        graphicsContext.setFill(bgColor);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (controller.getScaleText() != null) updateScale();
        graphicsContext.setTransform(trans);
        graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));

        //Draws map. Only draws tiles that are in view
        if (tilegrid != null) {
            try {
                tilegrid.drawVisibleTiles(graphicsContext, getViewport(), getLOD());
            } catch (NonInvertibleTransformException e) {
                System.out.println("Error getting viewport! Error: " + e.getMessage());
            }
        }

        //Draws extra objects
        for (MapObject object : extraDrawObjects) {
            object.draw(graphicsContext);
        }

        if (firstTimeDrawingMap) {
            System.out.println("Finished first time drawing!");
            firstTimeDrawingMap = false;
        }
    }

    /// Saves the object given as parameter and includes it in {@link #drawMap()}.
    public void addObjectToDraw(MapObject mapObject) {
        if (mapObject != null) {
            extraDrawObjects.add(mapObject);
        }
    }

    /// Removes the object given as parameter
    public void removeObjectToDraw(MapObject mapObject) { extraDrawObjects.remove(mapObject); }

    /// Method pans on the canvas
    public void pan(double dx, double dy) {
        //Moves the map
        trans.prependTranslation(dx, dy);
        drawMap();
    }

    /**
     * Zooms in and out, zoom level is limited by {@code minZoom} and {@code maxZoom} which can be changed in the constructor
     * @param dx deltaX
     * @param dy deltaY
     * @param factor of zooming in. 1 = same level, >1 = Zoom in, <1 = Zoom out
     */
    public void zoom(double dx, double dy, double factor) {
        //Zooms
        trans.prependTranslation(-dx, -dy);
        trans.prependScale(factor, factor);
        trans.prependTranslation(dx, dy);
        drawMap();
    }

    /// Zooms in on the coords given as parameter
    public void zoomTo(float x, float y) {
        //Finds how much we need to zoom in/out by dividing the target with the currentZoom level
        Point2D pivot = trans.transform(x, y);
        double targetScale = 500.0;
        double factor = targetScale / trans.getMxx();
        zoom(pivot.getX(), pivot.getY(), factor);

        //Moves the view so the given XY is in the middle
        double deltaX = (canvas.getWidth()/2) - pivot.getX();
        double deltaY = (canvas.getHeight()/2) - pivot.getY();
        pan(deltaX, deltaY);
    }

    /// Changes the current zoom level to a range from 0 to 4 (needed for the LOD). 0 is minimum amount of details, 4 is maximum
    private int getLOD() {
        double zoomLevel = trans.getMxx();
        if (zoomLevel > 550) return 5;
        if (zoomLevel > 160) return 4;
        if (zoomLevel > 85) return 3;
        if (zoomLevel > 8)  return 2;
        if (zoomLevel > 4)  return 1;
        else return 0;
    }

    /// Method updates the scale-bar on the map based on the zoom-level of the map
    private void updateScale() {
        graphicsContext.setLineWidth(3);
        graphicsContext.setStroke(scaleColor);

        double scalePosX = scene.getWidth() - 25;
        double scalePosY = scene.getHeight() - 15;
        graphicsContext.strokeLine(scalePosX, scalePosY, scalePosX-100, scalePosY);
        graphicsContext.strokeLine(scalePosX, scalePosY+5, scalePosX, scalePosY-5);
        graphicsContext.strokeLine(scalePosX-100, scalePosY+5, scalePosX-100, scalePosY-5);

        double scaleOrigin = 67; //NB! Magic number...
        double currentScale = scaleOrigin / trans.getMxx();

        String text;
        if (currentScale < 1) { text = String.format("%.2f", currentScale * 1_000) + " m"; }
        else { text = String.format("%.2f", currentScale) + " km"; }

        controller.getScaleText().setText(text);
    }

    /// Method updates the look of the checkbox for creating an OBJ-file on the start-up scene
    private void fixCheckBox() {
        CheckBox checkBoxOBJ = controller.getCheckBoxOBJ();
        StackPane box = (StackPane) checkBoxOBJ.getChildrenUnmodifiable().getLast();
        StackPane mark = (StackPane) box.getChildrenUnmodifiable().getFirst();

        box.setStyle("-fx-border-color: darkgrey; -fx-background-color: grey");
        mark.setStyle("-fx-background-color: darkgrey");
    }
    //endregion

    //region Getters and setters
    public Stage getStage() { return stage; }
    public Affine getTrans() { return trans; }
    public float[] getViewport() throws NonInvertibleTransformException {
        Point2D minXY = trans.inverseTransform(0, 0);
        Point2D maxXY = trans.inverseTransform(canvas.getWidth(), canvas.getHeight());
        return new float[]{(float) minXY.getX(), (float) minXY.getY(), (float) maxXY.getX(), (float) maxXY.getY()};
    }
    public void setTilegrid(Tilegrid tilegrid) { this.tilegrid = tilegrid; }
    public void setBgColor(Color bgColor) { this.bgColor = bgColor; }
    public void setScaleColor(Color scaleColor) { this.scaleColor = scaleColor; }
    public boolean isFirstTimeDrawingMap() { return firstTimeDrawingMap; } //for tests

    public Scene getScene() {
    return scene;
    }
    //endregion
}