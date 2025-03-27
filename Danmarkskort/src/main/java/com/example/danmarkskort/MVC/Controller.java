package com.example.danmarkskort.MVC;

import javafx.fxml.FXML;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;

import java.io.IOException;

public class Controller {
    //region Fields
    private View view;
    private Model model;
    private double lastX, lastY;

    @FXML private Canvas canvas;
    @FXML private TextField searchBar;
    @FXML private Slider zoomBar;
    //endregion

    //region Constructor(s)
    /// The View-constructor creates an instance of this constructor upon loading an FXML-scene
    public Controller() {
        canvas = new Canvas(400, 600);
        System.out.println("Controller created!");
    }
    //endregion

    //region (Dynamic) Methods
    /** Runs right after a Controller is created -- if we're in a scene with a zoomBar,
     *  the zoomBar's slider is set to communicate with the zoom-level of the canvas/document
     */
    public void initialize() {
        if (zoomBar != null) {
            zoomBar.valueProperty().addListener((_, _, _) -> {
                //Functionality for the zoomBar Slider -- I've given up for now
                //TODO FIX/CHANGE OR REMOVE ZOOMSLIDER
            });
        }
    }

    /** Method runs upon clicking the "Upload file"-button in the start-up scene.
     *  Lets the user pick a file and tries to parse it as a map. If successful,
     *  switches the scene to a canvas with the map drawn.
     */
    @FXML protected void uploadInputButton() throws IOException {
        //Laver en FileChooser til at åbne en stifinder når brugeren klikker 'Upload fil'
        FileChooser fileChooser = new FileChooser();

        //Sætter et par stilistiske elementer
        fileChooser.setTitle("Choose your file");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("All readable files", "*.osm","*.obj","*.txt","*.zip"),
                new ExtensionFilter("OpenStreetMap-files", "*.osm"),
                new ExtensionFilter("Parser-class objects", "*.obj"),
                new ExtensionFilter("Text-files", "*.txt"),
                new ExtensionFilter("Zip-files", "*.zip"),
                new ExtensionFilter("All files", "*.*"));
        String routeDesktop = switch(System.getProperty("os.name").split(" ")[0]) {
            case "Windows" -> System.getProperty("user.home") + "\\Desktop";
            case "MAC"     -> System.getProperty("user.home") + "/Desktop";
            default        -> System.getProperty("user.home");};
        fileChooser.setInitialDirectory(new File(routeDesktop));

        //Åbner stifinderen og gemmer filen som brugeren vælger
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            view = new View(view.getStage(), "mapOverlay.fxml");
            loadFile(selectedFile);
            assert view != null;

            view.drawMap(model.getParser());
        }
    }

    /// Method runs upon clicking the "Run standard"-button in the start-up scene.
    @FXML protected void standardInputButton() throws IOException {
        File standardMapFile = new File("./data/small.osm.obj"); //Skal ændres senere
        assert standardMapFile.exists();

        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);

        view.drawMap(model.getParser());
    }

    /** Passes the given file into a Model class that starts parsing it
     *  @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        model = new Model(mapFile.getPath(), canvas);
        assert model.getParser() != null;
    }

    /// Method runs upon zooming/scrolling on the Canvas
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        double factor = Math.pow(1.01, e.getDeltaY());
        double zoomLvl = view.getTrans().getMxx();

        //Der zoomes kun hvis...
        boolean cond1 = 2_017 < zoomLvl && zoomLvl < 140_000; //Hvis man er inde for zoom-grænserne
        boolean cond2 = zoomLvl < 2_017   && factor > 1;      //Hvis man er zoomet max ud men man zoomer ind
        boolean cond3 = zoomLvl > 140_000 && factor < 1;      //Hvis man er zoomet max ind men man zoomer ud

        if (cond1 || cond2 || cond3) view.zoom(e.getX(), e.getY(), factor);

        //zoomBar.adjustValue(zoomLvl / 140_000 * 100);
        //zoomBar.adjustValue(zoomLvl);
    }

    /// Method runs upon typing in the search-bar. For now simply prints what's written
    @FXML protected void onSearchBarType(KeyEvent e) {
        if (e.getCharacter().charAt(0) == '\r') System.out.println("ENTER");
        else System.out.println(searchBar.getText());
    }

    /// Method runs upon releasing a click on the canvas
    @FXML protected void onCanvasClick(MouseEvent e) {
        System.out.println("Clicked at ("+ e.getX() +", "+ e.getY() +")!");
    }

    /// Method runs upon pressing on the canvas
    @FXML protected  void onCanvasPressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }

    /// Method runs upon dragging on the canvas
    @FXML protected void onCanvasDragged(MouseEvent e) {
        double dx = e.getX() - lastX;
        double dy = e.getY() - lastY;
        view.pan(dx, dy);

        lastX = e.getX();
        lastY = e.getY();
    }
    //endregion

    //region Getter and setters
    /** Sætter Controllerens view-felt til et givent View
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View og Controller kan snakke sammen)
     * @param view View'et som Controllerens view-felt sættes til
     */
    void setView(View view) { this.view = view; }

    /** Returnerer Controllerens canvas-felt, der "populates" direkte idet en scene FXML-loades
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View kan få Canvas'et af Controlleren)
     * @return Controllerens canvas-felt
     */
    Canvas getCanvas() { return canvas; }
    //endregion
}