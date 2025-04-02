package com.example.danmarkskort.MVC;

import javafx.animation.AnimationTimer;
import com.example.danmarkskort.AddressSearch.TrieST;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //region Fields
    private View view;
    private Model model;
    private double lastX, lastY;
    private boolean panRequest, zoomRequest;
    private ScrollEvent scrollEvent;
    private TrieST<String> trieCity; //part of test
    private TrieST<String> trieStreet;
    private MouseEvent mouseEvent;

    @FXML private Canvas canvas;
    @FXML private ListView<String> listView;
    @FXML private Slider zoomBar;
    @FXML private TextField searchBar;
    //endregion

    /** View-konstruktøren skaber/kører en instans af
     * konstruktøren her, når den loader en FXML-scene
     */
    public Controller() {
        canvas = new Canvas(400, 600);
        System.out.println("Controller created!");

        this.trieCity = new TrieST<>(true);
        this.trieStreet = new TrieST<>(false);
        listView = new ListView<>();

        //Det her er cooked -MN
        try {
            model = Model.getInstance();
        } catch (IllegalStateException _) {} //Model not loaded yet, so we wait


        //region AnimationTimer
        //TODO: Fix, this doesnt work og tror det er fordi den lægger i konstruktøren men idk -MN
        //OBS JEG MISTÆNKER DET HER FOR IKKE AT VIRKE -MN
        //UPDATE: Jeg tror endnu mindre på det nu -MN
        //UPDATE: Jeg tror en lille smule på det, men jeg har ikke læst op på AnimationTimer-klassen endnu -OFS
        AnimationTimer fpsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (panRequest) {
                    double dx = mouseEvent.getX() - lastX;
                    double dy = mouseEvent.getY() - lastY;
                    view.pan(dx, dy);

                    lastX = mouseEvent.getX();
                    lastY = mouseEvent.getY();
                    panRequest = false;
                } else if (zoomRequest) {
                    double factor = scrollEvent.getDeltaY();
                    view.zoom(scrollEvent.getX(), scrollEvent.getY(), Math.pow(1.01, factor), true);
                    zoomRequest = false;
                }
            }
        };
        fpsTimer.start();
        //endregion
    }
    //endregion

    //region Methods
    /** Passes the given file into a Model class that starts parsing it
     *  @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        model = Model.getInstance(mapFile.getPath(), canvas);
        view.setTilegrid(model.getTilegrid());
        assert model.getParser() != null;
    }

    /** Method runs right after a Controller is created -- if we're in a scene with a zoomBar,
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

    @Override public void initialize(URL url, ResourceBundle resourceBundle) {
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                searchBar.setText(selected);
            }
        });
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

    /// Method runs upon clicking the "Run standard"-button in the start-up scene
    @FXML protected void standardInputButton() throws IOException {
        File standardMapFile = new File("./data/small.osm.obj"); //TODO skal ændres senere
        assert standardMapFile.exists();

        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);

        view.drawMap(model.getParser());
    }

    /// Methods runs upon typing in the search-bar
    @FXML protected void searchBarTyped(KeyEvent event) {
        listView.getItems().clear();
        String input = searchBar.getText();
        if (searchBar.getText().isEmpty()) {
            listView.setVisible(false);
        } else {
            listView.setVisible(true);
        }

        // HVIS DER STADIG ER MULIGE BYER
        if (!trieCity.keysWithPrefix(input).isEmpty()) {
            System.out.println("Dette er byer");
            autoSuggest(event, input, trieCity);

        } else { // Skal lede i vejnavne
            System.out.println("Dette er veje");
            autoSuggest(event, input, trieStreet);
        }
    }

    /// Auto-suggests roads and cities in a drop-down menu from the search-bar (????)
    private void autoSuggest(KeyEvent event, String input, TrieST<String> trie) {
        if (event.getCharacter().equals("\r")) { // Hvis der trykkes enter
            if (trie.keysThatMatch(input) != null) {
                System.out.println(trie.get(trie.keysThatMatch(input).getFirst()));
            } else {
                System.out.println(trie.keysWithPrefix(input).getFirst());
            }
        } else {
            if (event.getCharacter().equals("\b")) { //hvis der trykkes backspace eller
                event.consume();
            }
            //Finder de 3 første relevante adresser.
            for (int i = 0; i < trie.keysWithPrefix(input).size(); i++) {
                listView.getItems().add(trie.keysWithPrefix(input).get(i));
                if (i >= 2) { return; }
            }
        }
    }

    /// Method runs upon zooming/scrolling on the Canvas
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked
        scrollEvent = e;
        zoomRequest = true;
    }

    /** Metode køres når man slipper sit klik på Canvas'et */
    @FXML protected void onCanvasClick(MouseEvent e) {
        System.out.println("Clicked at ("+ e.getX() +", "+ e.getY() +")!");
    }

    /** Metode køres idet man klikker ned på Canvas'et */
    @FXML protected  void onCanvasPressed(MouseEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked xd
        lastX = e.getX();
        lastY = e.getY();
    }

    /// Method runs upon dragging on the canvas
    @FXML protected void onCanvasDragged(MouseEvent e) {
        mouseEvent = e;
        panRequest = true;
    }
    //endregion

    //region Getters and setters
    /** Sætter Controllerens view-felt til et givent View
     *  (Denne metode bruges kun af View-klassen en enkelt gang, så View og Controller kan snakke sammen)
     *  @param view View'et som Controllerens view-felt sættes til
     */
    public void setView(View view) { this.view = view; }

    /** Returnerer Controllerens canvas-felt, der "populates" direkte idet en scene FXML-loades
     *  (Denne metode bruges kun af View-klassen en enkelt gang, så View kan få Canvas'et af Controlleren)
     *  @return Controllerens canvas-felt
     */
    public Canvas getCanvas() { return canvas; }
    //endregion
}