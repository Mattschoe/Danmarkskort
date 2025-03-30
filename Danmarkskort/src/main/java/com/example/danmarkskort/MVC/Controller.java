package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.Tile;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Controller {
    //region fields
    View view;
    Model model;
    File standardMapFile;
    @FXML Label valgtFil;
    @FXML Canvas canvas;
    double lastX, lastY;
    boolean panRequest, zoomRequest;
    MouseEvent event;
    double[] viewport;
    //endregion

    /** View-konstruktøren skaber/kører en instans af
     * konstruktøren her, når den loader en FXML-scene
     */
    public Controller() {
        standardMapFile = new File("./data/small.osm.obj"); //Skal ændres senere
        canvas = new Canvas(400, 600);
        assert standardMapFile.exists();
        System.out.println("Controller created!");

        //Det her er cooked -MN
        try {
            model = Model.getInstance();
        } catch (IllegalStateException e) {
            //Model not loaded yet, so we wait
        }

        //region AnimationTimer
        ///TO DO: Fix, this doesnt work og tror det er fordi den lægger i construktøren men idk -MN
        AnimationTimer fpsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (panRequest) {
                    try {
                        view.setVisibleTiles(model.getTilesInView(view.getViewport())); //Absolut cooked at kalde en view metode, ved hjælp af en model metode, ved at give den parametre med getter metoder fra view, men øh. If it works ig -MN
                    } catch (NonInvertibleTransformException exception) {
                        System.out.println("Error getting viewport from view!");
                    }
                    double dx = event.getX() - lastX;
                    double dy = event.getY() - lastY;
                    view.pan(dx, dy);

                    lastX = event.getX();
                    lastY = event.getY();
                    panRequest = false;
                }
            }
        };
        fpsTimer.start();
        //endregion
    }

    /**
     * Passes the given file into a Model class that starts parsing it
     * @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        model = Model.getInstance(mapFile.getPath(), canvas);
        assert model.getParser() != null;
    }

    /** Funktionalitet forbundet med "Upload fil"-knappen på startskærmen. Køres når knappen klikkes */
    @FXML protected void uploadInputButton() throws IOException{
        //Laver en FileChooser til at åbne en stifinder når brugeren klikker 'Upload fil'
        FileChooser fileChooser = new FileChooser();

        //Sætter et par stilistiske elementer
        fileChooser.setTitle("Vælg fil");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Alle læsbare filer", "*.osm","*.obj","*.txt","*.zip"),
                new ExtensionFilter("OpenStreetMap-filer", "*.osm"),
                new ExtensionFilter("Parser-klasse", "*.obj"),
                new ExtensionFilter("Tekst-filer", "*.txt"),
                new ExtensionFilter("Zip-filer", "*.zip"),
                new ExtensionFilter("Alle filer", "*.*"));
        String routeDesktop = switch(System.getProperty("os.name").split(" ")[0]) {
            case "Windows" -> System.getProperty("user.home") + "\\Desktop";
            case "MAC"     -> System.getProperty("user.home") + "/Desktop";
            default        -> System.getProperty("user.home");};
        fileChooser.setInitialDirectory(new File(routeDesktop));

        //Åbner stifinderen og gemmer filen som brugeren vælger
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            //Loads View and model
            view = new View(view.getStage(), "mapOverlay.fxml");
            loadFile(selectedFile);
            assert view != null;

            //Starts up the map
            view.drawMap(model.getParser());
            try {
                view.setVisibleTiles(model.getTilesInView(view.getViewport())); //Absolut cooked at kalde en view metode, ved hjælp af en model metode, ved at give den parametre med getter metoder fra view, men øh. If it works ig -MN
            } catch (NonInvertibleTransformException exception) {
                System.out.println("Error getting viewport from view!");
            }
        }
    }

    /** Funktionalitet forbundet med "Kør standard"-knappen på startskærmen. Køres når knappen klikkes */
    @FXML protected void standardInputButton() throws IOException {
        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);
        assert view != null;
        view.drawMap(model.getParser());
    }

    //region events
    /** Metode køres når man zoomer på Canvas'et */
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked

        try {
            view.setVisibleTiles(model.getTilesInView(view.getViewport())); //Absolut cooked at kalde en view metode, ved hjælp af en model metode, ved at give den parametre med getter metoder fra view, men øh. If it works ig -MN
        } catch (NonInvertibleTransformException exception) {
            System.out.println("Error getting viewport from view!");
        }

        double factor = e.getDeltaY();
        view.zoom(e.getX(), e.getY(), Math.pow(1.01, factor), true);
    }

    /** Metode køres når man slipper sit klik på Canvas'et */
    @FXML protected void onCanvasClick(MouseEvent e) {
        System.out.println("Clicked at ("+ e.getX() +", "+ e.getY() +")!");
    }

    /** Metode køres idet man klikker ned på Canvas'et */
    @FXML protected  void onCanvasPressed(MouseEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked
        lastX = e.getX();
        lastY = e.getY();
    }

    /** Metode køres når man trækker på Canvas'et. Metode er limitet til 60FPS */
    @FXML protected void onCanvasDragged(MouseEvent event) {
        this.event = event;
        panRequest = true;
    }
    //endregion

    //region getters and setters
    /** Sætter Controllerens view-felt til et givent View
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View og Controller kan snakke sammen)
     * @param view View'et som Controllerens view-felt sættes til
     */
    void setView(View view) {
        this.view = view;
    }
    /** Returnerer Controllerens canvas-felt, der "populates" direkte idet en scene FXML-loades
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View kan få Canvas'et af Controlleren)
     * @return Controllerens canvas-felt
     */
    Canvas getCanvas() {
        return canvas;
    }
    //endregion
}