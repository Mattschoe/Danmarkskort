package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.Tile;
import javafx.animation.AnimationTimer;
import com.example.danmarkskort.AddressSearch.TrieST;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //region Fields
    private View view;
    private Model model;
    private File standardMapFile;
    @FXML Label valgtFil;
    @FXML Canvas canvas;
    @FXML private TextField searchBar;
    @FXML private ListView<String> viewList;
    private double lastX, lastY;
    private boolean panRequest, zoomRequest;
    private MouseEvent mouseEvent;
    private ScrollEvent scrollEvent;
    private TrieST<String> trieCity; //part of test
    private TrieST<String> trieStreet;

    @FXML private Slider zoomBar;
    @FXML ListView<String> listView;
    MouseEvent event;
    //endregion

    //region Constructor(s)
    /// The View-constructor creates an instance of this constructor upon loading an FXML-scene
    public Controller() {
        standardMapFile = new File("./data/small.osm.obj"); //Skal ændres senere
        canvas = new Canvas(400, 600);
        assert standardMapFile.exists();
        System.out.println("Controller created!");

        this.trieCity = new TrieST<>(true);
        this.trieStreet = new TrieST<>(false);
        listView = new ListView<>();

        //Det her er cooked -MN
        try {
            model = Model.getInstance();
        } catch (IllegalStateException _) {} //Model not loaded yet, so we wait


        //region AnimationTimer
        //TO DO: Fix, this doesnt work og tror det er fordi den lægger i construktøren men idk -MN
        //OBS JEG MISTÆNKER DET HER FOR IKKE AT VIRKE -MN
        //UPDATE: Jeg tror endnu mindre på det nu -MN
        AnimationTimer fpsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (panRequest) {
                    double dx = event.getX() - lastX;
                    double dy = event.getY() - lastY;
                    view.pan(dx, dy);

                    lastX = event.getX();
                    lastY = event.getY();
                    panRequest = false;
                } else if (zoomRequest) {
                    double factor = scrollEvent.getDeltaY();
                    view.zoom(scrollEvent.getX(), scrollEvent.getY(), Math.pow(1.01, factor), true);
                    zoomRequest = false;
                }
            }
        };
        fpsTimer.start();
    }
    //endregion

    /**
     * Passes the given file into a Model class that starts parsing it
     * @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        model = Model.getInstance(mapFile.getPath(), canvas);
        view.setTilegrid(model.getTilegrid());
        assert model.getParser() != null;
    }

    //region Methods
    /** Runs right after a Controller is created -- if we're in a scene with a zoomBar,
     *  the zoomBar's slider is set to communicate with the zoom-level of the canvas/document
     */
    @Deprecated protected void initialize() {
        if (zoomBar != null) {
            zoomBar.valueProperty().addListener((_, _, _) -> {
                //Functionality for the zoomBar Slider -- I (Olli) have given up for the time being
                //TODO FIX/CHANGE/REMOVE ZOOMSLIDER
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
            //Loads View and model
            view = new View(view.getStage(), "mapOverlay.fxml");
            loadFile(selectedFile);
            assert view != null;

            //Starts up the map
            view.drawMap(model.getParser());
        }
    }

    /** Funktionalitet forbundet med "Kør standard"-knappen på startskærmen. Køres når knappen klikkes */
    @FXML protected void standardInputButton() throws IOException {
        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);
        assert view != null;
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

    private void autoSuggest(KeyEvent event, String input, TrieST<String> trie) {
        if (event.getCharacter().equals("\r")) { // Hvis der trykkes enter
            if (trie.keysThatMatch(input)!=null) {
                System.out.println(trie.get(trie.keysThatMatch(input).getFirst()));
            } else {
                System.out.println(trie.keysWithPrefix(input).getFirst());
            }
        } else {
            if (event.getCharacter().equals("\b")) { //hvis der trykkes backspace eller
                event.consume();
            }
            //Finder de 3 første relevante addresser.
            for (int i = 0; i < trie.keysWithPrefix(input).size(); i++) {
                listView.getItems().add(trie.keysWithPrefix(input).get(i));
                if (i >= 2) {
                    return;
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                searchBar.setText(selected);
            }

        });
    }


    /** Metode køres når man zoomer på Canvas'et */
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked
        double factor = e.getDeltaY();
        view.zoom(e.getX(), e.getY(), Math.pow(1.01, factor), true);
    }

    /** Metode køres når man slipper sit klik på Canvas'et */
    @FXML protected void onCanvasClick(MouseEvent e) {
        System.out.println("Clicked at ("+ e.getX() +", "+ e.getY() +")!");
    }

    /// Method runs upon pressing on the canvas
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

    /// Method runs upon typing in the search-bar. For now simply prints what's written
    @FXML protected void onSearchBarType(KeyEvent e) {
        if (e.getCharacter().charAt(0) == '\r') System.out.println("ENTER");
        else System.out.println(searchBar.getText());
    }
    //endregion

    //region Getters and setters
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
    public Canvas getCanvas() { return canvas; }
    //endregion
}