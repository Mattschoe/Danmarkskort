package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.*;
import javafx.animation.AnimationTimer;
import com.example.danmarkskort.AddressSearch.TrieST;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //region Fields
    private View view;
    private Model model;
    private double lastX, lastY; //Used to pan
    private boolean panRequest, zoomRequest; //Used by AnimationTimer
    private ScrollEvent scrollEvent; //Used to zoom
    private TrieST<String> trieCity; //Part of test
    private TrieST<String> trieStreet;
    private MouseEvent mouseEvent; //Used to pan
    private POI startPOI;
    private POI endPOI;
    private List<String> POIList = List.of("En", "TO", "Tre");

    private long lastSystemTime; //Used to calculate FPS
    private int framesThisSec;   //Used to calculate FPS

    //region FXML fields
    @FXML private Canvas canvas;
    @FXML private CheckMenuItem fpsButton;
    @FXML private CheckMenuItem guideButton;
    @FXML private ListView<String> listView;
    @FXML private Slider zoomBar;
    @FXML private Text fpsText;
    @FXML private Text zoomText;
    @FXML private TextField searchBar;
    @FXML private TextArea guideText;
    @FXML private Button switchSearch;
    @FXML private Button findRoute;
    @FXML private TextField destination;
    @FXML private MenuItem POIMenuButton;


    //endregion
    //endregion

    //region Constructor(s)
    /**
     * View-konstruktøren skaber/kører en instans af
     * konstruktøren her, når den loader en FXML-scene
     */
    public Controller() {
        canvas = new Canvas(400, 600);
        System.out.println("Controller created!");

        this.trieCity = new TrieST<>(true);
        this.trieStreet = new TrieST<>(false);
        listView = new ListView<>();

        //region AnimationTimer
        AnimationTimer fpsTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (fpsText != null) {
                    if (fpsButton.isSelected()) calculateFPS(now);
                    else if (!fpsText.getText().isEmpty()) fpsText.setText("");
                }

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
    }

    /** Runs right after a Controller is created --
     *  configures something(???) for an object in the mapOverlay.fxml scene
     */
    @Override public void initialize(URL url, ResourceBundle resourceBundle) {
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                searchBar.setText(selected);
            }
        });
    }

    //region Start-up scene methods
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
            view.drawMap();
        }
    }

    /// Method runs upon clicking the "Run standard"-button in the start-up scene
    @FXML protected void standardInputButton() throws IOException {
        File standardMapFile = new File("data/StandardMap/parser.obj"); //TODO skal ændres senere
        assert standardMapFile.exists();

        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);

        view.drawMap();
    }

    //endregion

    //region mapOverlay.fxml scene methods
    /// Calculates FPS and adjusts the display-text
    private void calculateFPS(long systemTime) {
        long deltaSystemTime = systemTime - lastSystemTime;
        ++framesThisSec;

        if (deltaSystemTime >= 1_000_000_000) {
            double fps = framesThisSec / (deltaSystemTime / 1_000_000_000.0);
            fpsText.setText(String.format("FPS: %.0f", fps));

            framesThisSec = 0;
            lastSystemTime = systemTime;
        }
    }

    /// Adds a listener on View's Affine "trans" which updates the zoomBar based on trans' zoom-factor
    public void bindZoomBar() {
        if (zoomBar != null) {
            view.getTrans().mxxProperty().addListener(_ -> {
                double currentZoom = view.getTrans().getMxx();
                if (currentZoom < 1) {
                    zoomBar.setValue(0);
                    zoomText.setText("Zoom-factor: >1");
                }
                else {
                    if (currentZoom > 100) zoomBar.setValue(100);
                    else zoomBar.setValue(currentZoom);
                    zoomText.setText(String.format("Zoom-factor: %.0f", currentZoom));
                }
            });
        }
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

    private void startSearch() {
        System.out.println("Starting search...");
        model.search(startPOI.getClosestNodeWithRoad(), endPOI.getClosestNodeWithRoad());
        System.out.println("Finished search!");
    }

    /// Method opens af list of points of interests so the user can edit it.
    @FXML protected void POIMenu(){
        //der skal være en liste der bliver opdateret når man tilføjer og fjerne POI's som bliver vist når man klikker på menuen
        System.out.println("Så skal man kunne skfite her");
        System.out.println(POIList);
    }
    /// Method to export a route as PDF
    @FXML protected void exportAsPDF(){
        System.out.println("exporting as PDF");
    }

    /// Method to open a textbox with a written guide when "Guide" is pressed
    @FXML protected void guideTextButton(){
        guideText.setVisible(guideButton.isSelected());
        CloseGuideButton.setVisible(guideButton.isSelected());
    }


    //endregion

    //region Canvas methods
    /// Method runs upon zooming/scrolling on the Canvas
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked
        scrollEvent = e;
        zoomRequest = true;
    }

    /** Metode køres når man slipper sit klik på Canvas'et */
    @FXML protected void onCanvasClick(MouseEvent e) {
        if (e.getClickCount() == 1) {
            double x, y;

            try {
                Point2D point = view.getTrans().inverseTransform(e.getX(), e.getY());
                x = point.getX();
                y = point.getY();
            } catch (Exception exception) {
                return;
            }

            Tile tile = model.getTilegrid().getTileFromXY((float) x, (float) y);
            if (tile == null) return;
            double closestDistance = Double.MAX_VALUE;
            Node closestNode = null;
            for (Node node : tile.getNodesInTile()) {
                if (node.getEdges().isEmpty()) continue;
                double nodeX = node.getX();
                double nodeY = node.getY();
                double distance = Math.sqrt(Math.pow((nodeX - x), 2) + Math.pow((nodeY - y), 2)); //Afstandsformlen ser cooked ud i Java wth -MN
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestNode = node;
                }
            }
            assert closestNode != null;
            System.out.println(closestNode);
        }
        //endregion

        //region DOUBLE CLICK (Searching)
        if (e.getClickCount() == 2) {
            //Makes POI
            Affine transform = view.getTrans();
            POI POI = null;
            try {
                Point2D point = transform.inverseTransform(e.getX(), e.getY());
                POI = model.createPOI((float) point.getX(), (float) point.getY(), "Test");
            } catch (NonInvertibleTransformException exception) {
                System.out.println("Error inversion mouseclick coords!" + exception.getMessage());
            }
            view.drawMap(); //Makes sure that the POI is shown instantly

            //Assigns spot for POI. Sets as start if empty or if "find route" has not been activated, if else, else we set it as the destination
            if (POI != null) {
                onActivateSearch();
                if (searchBar.getText().trim().isEmpty() || !destination.isVisible()) {
                    startPOI = POI;
                } else {
                    endPOI = POI;
                }
                updateSearchText();
            }
        }
        //endregion
    }

    ///Opens the search menu when activated. If both start- and endPOI are initialized, this button is used for activating the route finding between the two POI's.
    @FXML public void onActivateSearch() {
        findRoute.setVisible(true);
    }

    ///"Find Route" button on UI
    @FXML public void onRouteSearchStart() {
        if (startPOI != null && endPOI != null && !searchBar.getText().trim().isEmpty() && !destination.getText().trim().isEmpty()) {
            startSearch();
        } else {
            switchSearch.setVisible(true);
            destination.setVisible(true);
        }
    }

    @FXML public void switchDestinationAndStart() {
        POI temp = startPOI;
        startPOI = endPOI;
        endPOI = temp;
        updateSearchText();
    }

    ///Updates the text in the search. Call this after changing the POI responsible for the text
    private void updateSearchText() {
        searchBar.clear();
        destination.clear();

        if (startPOI != null) searchBar.setText(startPOI.getNodeAddress());
        if (endPOI != null) destination.setText(endPOI.getNodeAddress());
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

    //region ColorSheet toggles
    @FXML private void paletteDefault() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.LIGHTBLUE);
        fpsText.setFill(Color.BLACK);
        zoomText.setFill(Color.BLACK);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("default");
                if (mo instanceof Polygon p) p.setPalette("default");
            }
        }
        view.drawMap();
    }

    @FXML private void paletteMidnight() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.rgb(23, 3, 63));
        fpsText.setFill(Color.WHITE);
        zoomText.setFill(Color.WHITE);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("midnight");
                if (mo instanceof Polygon p) p.setPalette("midnight");
            }
        }
        view.drawMap();
    }

    @FXML private void paletteBasic() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.GHOSTWHITE);
        fpsText.setFill(Color.INDIGO);
        zoomText.setFill(Color.INDIGO);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("basic");
                if (mo instanceof Polygon p) p.setPalette("basic");
            }
        }
        view.drawMap();
    }
    //endregion

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