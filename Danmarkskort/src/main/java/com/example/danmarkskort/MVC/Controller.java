package com.example.danmarkskort.MVC;

import com.example.danmarkskort.LoadingBar;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.*;
import com.example.danmarkskort.PDFOutput;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Controller {
    //region Fields
    private View view;
    private Model model;
    private double lastX, lastY; //Used to pan
    private boolean panRequest, zoomRequest; //Used by AnimationTimer
    private ScrollEvent scrollEvent; //Used to zoom
    private MouseEvent mouseEvent; //Used to pan
    private final Map<String,POI> favoritePOIs = new HashMap<>();
    private final List<POI> oldPOIs = new ArrayList<>();
    List<Road> latestRoute = new ArrayList<>();
    private List<Node> autoSuggestResults;
    private TextField searchingSource;
    private boolean putTextSwitched;
    private LoadingBar loadingBar;

    private long lastSystemTime; //Used to calculate FPS
    private int framesThisSec;   //Used to calculate FPS

    //region FXML fields
    @FXML private Canvas canvas;
    @FXML private AnchorPane poiGroup;
    @FXML private CheckBox checkBoxOBJ;
    @FXML private CheckMenuItem fpsButton;
    @FXML private CheckMenuItem guideButton;
    @FXML private ListView<String> listView;
    @FXML private Text closestRoadText;
    @FXML private Text fpsText;
    @FXML private Text scaleText;
    @FXML private TextField searchBar;
    @FXML private TextField destination;
    @FXML private TextArea guideText;
    @FXML private Button switchSearch;
    @FXML private Button findRoute;
    @FXML private Button savePOIButton;
    @FXML private MenuItem fastestRoute;
    @FXML private MenuItem shortestRoute;
    @FXML private Menu POIMenu;
    @FXML private TextField addNamePOI;
    @FXML private Button addToPOIsUI;
    @FXML private Button POIClose;
    @FXML private TextArea addPOIBox;
    @FXML public Text loadingText;
    @FXML public ProgressBar progressBar;
    //endregion
    //endregion

    //region Constructor(s)
    /**
     * View-konstruktøren skaber/kører en instans af
     * konstruktøren her, når den loader en FXML-scene
     */
    public Controller() {
        canvas = new Canvas(400, 600);
        listView = new ListView<>();
        autoSuggestResults = new ArrayList<>();
        putTextSwitched = false;

        loadingText = new Text();
        progressBar = new ProgressBar();

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
                    view.zoom(scrollEvent.getX(), scrollEvent.getY(), Math.pow(1.01, factor));
                    zoomRequest = false;
                }
            }
        };
        fpsTimer.start();
        //endregion
    }
    //endregion

    //region Methods
    /// MIDLERTIDIG METODE FOR AT GØRE DET NEMT AT ÅBNE COVERAGE-RAPPORTEN.
    @FXML protected void openTestCoverage() { //TODO %% SKAL FJERNES SENERE
        try { Desktop.getDesktop().open(new File("build/reports/jacoco/test/html/index.html")); }
        catch (Exception e) { System.out.println("No test coverage report exists! Try building the app"); }
    }

    /**
     * Loads a map file and processes its content asynchronously.
     * Updates the UI with a loading screen until the process is completed.
     * If successful, switches the scene to display the generated map.
     *
     * @param mapFile the file object representing the map file to be loaded.
     * @throws IOException if an error occurs while loading the map file or switching views.
     */
    private void loadFile(File mapFile) throws IOException {
        boolean createOBJ = checkBoxOBJ != null && checkBoxOBJ.isSelected();
        loadingBar = LoadingBar.getInstance();

        view = new View(view.getStage(), "loading.fxml");

        //Reference the UI elements after view is loaded
        Text loadingText = (Text) view.getScene().lookup("#loadingText");
        ProgressBar progressBar = (ProgressBar) view.getScene().lookup("#progressBar");
        //Start the Timeline for UI updates
        StringBuffer dots = new StringBuffer();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> {

                    loadingText.setText(loadingBar.getLoadingText() + dots.append(".")); // For testing
                    progressBar.setProgress(loadingBar.getProgress());

                    if (dots.toString().equals("...")) {
                        dots.delete(0,5);
                    }
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //start the background task
        Task<Void> loadingTask = new Task<>() {
            @Override protected Void call() throws Exception {
                model = Model.getInstance(mapFile.getPath(), canvas, createOBJ);
                return null;
            }

            @Override protected void succeeded() {
                try {
                    timeline.stop();
                    view = new View(view.getStage(), "mapOverlay.fxml");
                    view.setTilegrid(model.getTilegrid());
                    view.drawMap();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new Thread(loadingTask).start();
    }

    //region Start-up scene methods
    /**
     * Method runs upon clicking the "Upload file"-button in the start-up scene.
     * Lets the user pick a file and tries to parse it as a map. If successful,
     * switches the scene to a canvas with the map drawn.
     */
    @FXML protected void uploadInputButton() throws IOException {
        //Laver en FileChooser til at åbne en stifinder når brugeren klikker 'Upload fil'
        FileChooser fileChooser = new FileChooser();

        //Sætter et par stilistiske elementer
        fileChooser.setTitle("Choose your file");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("All readable files", "*.osm", "*.obj",/* "*.txt",*/ "*.zip"),
                new ExtensionFilter("OpenStreetMap-files", "*.osm"),
                new ExtensionFilter("Parser-class objects", "*.obj"),
                //new ExtensionFilter("Text-files", "*.txt"),
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
            view.getStage().setTitle("Rats' Map of Denmark - "+ selectedFile.getName());
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

        loadFile(standardMapFile);
    }


    @FXML protected void toggleCreateOBJ() {
        if (checkBoxOBJ.getChildrenUnmodifiable().isEmpty()) return;
        StackPane box = (StackPane) checkBoxOBJ.getChildrenUnmodifiable().getLast();
        StackPane mark = (StackPane) box.getChildrenUnmodifiable().getFirst();

        if (checkBoxOBJ.isSelected()) {
            box.setStyle("-fx-border-color: darkgreen; -fx-background-color: green");
            mark.setStyle("-fx-background-color: #ffffff");
        }
        else {
            box.setStyle("-fx-border-color: darkgrey; -fx-background-color: grey");
            mark.setStyle("-fx-background-color: darkgrey");
        }
    }

    /// Disables the list view if we have picked one and then moves the mouse out of the listview
    @FXML protected void mouseExitedListView() {
        listView.setVisible(false);
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

    /// Methods runs upon modifying the {@code searchbar} in the UI.
    @FXML protected void searchBarsTyped(KeyEvent event) {
        if (model == null) model = Model.getInstance();

        TextField source = (TextField) event.getSource();
        searchingSource = source;

        //Changes the position of the ListView depending on which searchbar is being written in
        if (source.getId().equals("searchBar")) listView.setLayoutY(58);
        if (source.getId().equals("destination")) listView.setLayoutY(94);

        if (event.getCharacter().equals("\r")) {
            if (!autoSuggestResults.isEmpty()) {
                listView.setVisible(false);
                findRoute.setVisible(true);
                findRoute.requestFocus();
                savePOIButton.setVisible(true);

                //When the user presses enter on a searchbar, it gets
                //updated with the best(first) match from the suggestions
                autoSuggestResults = autoSuggest(source.getText().toLowerCase());
                Node selection = autoSuggestResults.getFirst();

                view.zoomTo(selection.getX(), selection.getY());
            }
        }
        else {
            String input = source.getText();

            //If search-bar is empty we return out
            if (input == null || input.isEmpty()) {
                listView.setVisible(false);
                return;
            }
            else {
                input = input.toLowerCase();
            }

            listView.getItems().clear(); //Cleans up earlier search
            listView.setVisible(true); //Make the trie-matches visible

            autoSuggestResults = autoSuggest(input); //Dynamically auto-suggest from user input

            //Changes listView size dynamically
            if (listView.getItems().size() >= 3) listView.setPrefHeight(88);
            if (listView.getItems().size() == 2) listView.setPrefHeight(65);
            if (listView.getItems().size() == 1) listView.setPrefHeight(41);
        }
    }

    /// When the user presses 'DOWN' in a searchbar, focus shifts to the ListView if it is visible
    @FXML protected void searchBarsPressed(KeyEvent event) {
        if (event.getCode().toString().equals("DOWN")) {
            if (listView.isVisible()) {
                listView.requestFocus();
            }
        }
    }

    /// When the user presses 'Enter' on the ListView, the ListView is disabled and the Find Route-button is enabled
    @FXML protected void onListViewTyped(KeyEvent event) {
        if (event.getCharacter().equals("\r")) {
            Node chosenNode = autoSuggestResults.get(listView.getSelectionModel().getSelectedIndex());
            searchingSource.setText(chosenNode.getAddress());
            view.zoomTo(chosenNode.getX(), chosenNode.getY());

            listView.setVisible(false);
            findRoute.setVisible(true);
            findRoute.requestFocus();
            savePOIButton.setVisible(true);
        }
    }

    /**
     * Auto-suggests roads and cities in a drop-down menu from the search-bar.
     * Will auto-suggest cities, unless there are none, then suggests potential streets.
     * @return list of nodes which match the search-input
     */
    private List<Node> autoSuggest(String input) {
        List<Node> cities = model.getCitiesFromPrefix(input);
        List<Node> streets = model.getStreetsFromPrefix(input);

        //If prefix matches
        if (!cities.isEmpty()) {
            for (Node node : cities) {
                listView.getItems().add(node.getCity());
            }
            return cities;
        }
        else if (!streets.isEmpty()) { //If no city found we show streets
            for (Node node : streets) {
                listView.getItems().add(node.getAddress());
            }
            return streets;
        }

        listView.setVisible(false);
        return Collections.emptyList();
    }

    /**
     * Initiates a search for a route between two nodes and updates the view accordingly.
     * The method first clears any previously drawn route, performs the search,
     * and then visualizes the new route on the map.
     *
     * @param from the starting node of the route.
     * @param to the destination node of the route.
     */
    private void startSearch(Node from, Node to) {
        //First removes the last route from the draws
        if (!latestRoute.isEmpty()) {
            for (Road road : latestRoute) {
                road.setPartOfRoute(false);
                view.removeObjectFromDraw(road);
            }
        }

        System.out.println("Starting search...");
        latestRoute = model.search(from, to);

        //Adds route to the view so it gets drawn
        for (Road road : latestRoute) {
            view.addObjectToDraw(road);
        }
        view.drawMap(); //Draws to refresh instantly
        System.out.println("Finished search!");
    }
    //endregion

    //region Canvas methods
    /// When the user chooses a node from the suggestions, overrides the searchbar and zooms onto the Node
    @FXML protected void onAddressPickedFromList(MouseEvent event) {
        Node chosenNode = autoSuggestResults.get(listView.getSelectionModel().getSelectedIndex());
        searchingSource.setText(chosenNode.getAddress());
        view.zoomTo(chosenNode.getX(), chosenNode.getY());

        listView.setVisible(false);
        findRoute.setVisible(true);
        findRoute.requestFocus();
        savePOIButton.setVisible(true);

        //noinspection StatementWithEmptyBody
        if (event.getClickCount() == 2) {
            //Method used to be in here but Gertrud-testen showed it was unintuitive
        }
    }

    /// Saves a POI from the last given input in either search-bar, and adds POI to the 'POIs' Menu in the MenuBar
    @FXML protected void savePOItoHashMap() {
        if (searchingSource == null) {
            System.out.println("Oh no, fishy behaviour!!!! Search source is null");
        }
        else {
            //region > Save the given name of the POI
            //System.out.println("Text from "+searchingSource.getId()+": "+ searchingSource.getText());
            String poiName = addNamePOI.getText();
            if (poiName.trim().isEmpty()) {
                System.out.println("Cannot save a Point of Interest w/o a name!");
                return;
            }
            //endregion

            POI poi = null;
            //region > Make new POI if searchSource-text doesn't match last oldPOI
            if (oldPOIs.isEmpty() || !searchingSource.getText().equals(oldPOIs.getLast().getNodeAddress())) {
                Node node = model.getStreetsFromPrefix(searchingSource.getText().toLowerCase()).getFirst();

                poi = model.createPOI(node.getX(), node.getY(), poiName);

                view.zoomTo(node.getX(), node.getY()); //Zooms to the POI and redraws the map
            }
            //endregion
            //region > Remove POI from oldPOIs if search-source text matches the last oldPOI, so it isn't deleted
            else {
                poi = oldPOIs.getLast();
                oldPOIs.remove(poi);
            }
            //endregion
            //region > Put the POI in favoritePOIs, add it to draw and close POI-menu
            favoritePOIs.put(poiName, poi);
            view.addObjectToDraw(poi);
            closePOIMenu();
            //endregion<
            //region > Make a new Menu for the POI, and add it to the POIs Menu
            Menu POIMenuItem = new Menu(poiName);
            POIMenu.getItems().add(POIMenuItem);
            //endregion
            //region > Make 'Delete' and 'Show Address'-MenuItems for the POI, and add them to the POI Menu
            MenuItem deletePOI = new MenuItem("Delete");
            deletePOI.setOnAction(_ -> {
                POI thisPoi = favoritePOIs.get(poiName);
                favoritePOIs.remove(poiName);
                POIMenu.getItems().remove(POIMenuItem);
                model.removePOI(thisPoi);
                view.removeObjectFromDraw(thisPoi);
                view.drawMap();
            });

            MenuItem showAddress = new MenuItem("Show Address");
            showAddress.setOnAction(_ -> {
                POI thisPoi = favoritePOIs.get(poiName);
                if (thisPoi != null) {
                    String address = thisPoi.getNodeAddress();
                    searchingSource.setText(address);
                }
            });

            POIMenuItem.getItems().addAll(showAddress, deletePOI);
            //endregion

            //noinspection DataFlowIssue
            System.out.println("Saved POI \""+ poiName +"\" at "+poi.getNodeAddress());
        }
    }

    /**
     * Opens the Point of Interest (POI) menu in the user interface.
     * This method makes several UI components related to adding and managing POIs visible,
     * ensuring that users can interact with the POI functionality.
     */
    @FXML protected void openPOIMenu() {
        poiGroup.setVisible(true);
        addPOIBox.setVisible(true);
        addNamePOI.setVisible(true);
        addNamePOI.clear();
        addToPOIsUI.setVisible(true);
        POIClose.setVisible(true);
    }

    /**
     * Closes the Point of Interest (POI) menu in the user interface.
     *
     * This method hides UI components related to the POI functionality
     * ensuring the POI menu is no longer visible to the user.
     */
    @FXML protected void closePOIMenu() {
        poiGroup.setVisible(false);
        addPOIBox.setVisible(false);
        addNamePOI.setVisible(false);
        addToPOIsUI.setVisible(false);
        POIClose.setVisible(false);
    }

    /// Metode til at fjerne den røde markering på kortet for en POI. Virker kun for den POI, der senest er placeret
    @FXML public void removePOIMarker(POI poi) {
        //sæt knappen til visible og kald denne metode et sted
        model.removePOI(poi);
        view.drawMap();
    }

    /// Mangler logic for at finde korteste vej
    @FXML public void shortestRoute() {
        model.setSearchType(false);
    }

    ///  Mangler logic for at finde hurtigste vej
    @FXML public void fastestRoute() {
        model.setSearchType(true);
    }

    /// Method to export a route as PDF
    @FXML protected boolean exportAsPDF(){
        System.out.println("Attempting to export as PDF!");

        List<Road> latestRoute = Model.getInstance().getLatestRoute();

        if (latestRoute != null) {
            try {
                PDFOutput.generateRoute(latestRoute, true);
                System.out.println("PDF-export successful!");
                return true;
            }
            catch (Exception e) {
                System.out.println("PDF-export failed! Error: "+ e.getMessage());
                return false;
            }
        }
        else System.out.println("PDF-export failed; no route has been successfully set yet!");
        return false;
    }

    /// Method to open a textbox with a written guide when "Guide" is pressed
    @FXML protected void guideTextButton() {
        guideText.setVisible(guideButton.isSelected());
    }

    /// Method runs upon zooming/scrolling on the Canvas
    @FXML protected void onCanvasScroll(ScrollEvent e) {
        if (model == null) model = Model.getInstance(); //Det her er even mere cooked
        scrollEvent = e;
        zoomRequest = true;
    }

    /// Method runs upon releasing a press on the Canvas
    @FXML protected void onCanvasClick(MouseEvent e) {
        //region DOUBLE CLICK (Searching)
        if (e.getClickCount() == 2) {
            //region > Make POI
            POI poi = null;
            try {
                Point2D point = view.getTrans().inverseTransform(e.getX(), e.getY());
                poi = model.createPOI((float) point.getX(), (float) point.getY(), "Test");
                savePOIButton.setVisible(true);
            } catch (NonInvertibleTransformException exception) {
                System.out.println("An error occurred trying to invert mouse-click coordinates!"+ exception.getMessage());
            }
            view.drawMap(); //Makes sure that the POI is shown instantly
            //endregion

            //region > Determine which searchbar gets the POI's address
            if (poi != null) {
                if (!findRoute.isVisible()) findRoute.setVisible(true);
                if (!savePOIButton.isVisible()) savePOIButton.setVisible(true);

                if (searchBar.getText().trim().isEmpty()) {
                    searchBar.setText(poi.getNodeAddress());
                    searchingSource = searchBar;
                }
                else if (destination.getText().trim().isEmpty()) {
                    destination.setText(poi.getNodeAddress());
                    destination.setVisible(true);
                    switchSearch.setVisible(true);
                    searchingSource = destination;
                }
                else {
                    putTextSwitched = !putTextSwitched;

                    if (!putTextSwitched) {
                        searchBar.setText(poi.getNodeAddress());
                        searchingSource = searchBar;
                    }
                    else {
                        destination.setText(poi.getNodeAddress());
                        searchingSource = destination;
                    }
                }

                oldPOIs.add(poi);
            }
            //endregion

            if (oldPOIs.size() > 2) {
                while (oldPOIs.size() > 2) {
                    POI removed = oldPOIs.removeFirst();

                    if (!favoritePOIs.containsValue(removed)) {
                        oldPOIs.remove(removed);
                        removePOIMarker(removed);
                    }
                    //Removes the deleted POI's from the map after
                    //they've been deleted via the savePOIToHashMap function
                }
            }
        }
    }

    /**
     * This method controls the visibility of UI components such as the destination
     * search bar and switch search button. It validates that both origin and
     * destination input fields contain data before proceeding. Once validated,
     * it retrieves the starting and ending nodes based on the provided inputs and
     * initiates a route search between these nodes.
     * If either the origin or destination inputs are empty, an error message is printed
     * to the console, and the route search is not performed.
     */
    @FXML public void findRouteClicked() {
        if (listView.isVisible()) listView.setVisible(false);

        if (!switchSearch.isVisible() && !destination.isVisible()) {
            switchSearch.setVisible(true);
            destination.setVisible(true);
            destination.requestFocus();
        }
        else if (!searchBar.getText().trim().isEmpty() && !destination.getText().trim().isEmpty()) {
            String origin = searchBar.getText().toLowerCase();
            String termin = destination.getText().toLowerCase();

            Node from = model.getStreetsFromPrefix(origin).getFirst();
            POI startPOI = model.createPOI(from.getX(), from.getY(), "Test1");
            //from = getClosestRoadNode(from); IKKE SLET LÆS getClosestRoadNode

            Node to = model.getStreetsFromPrefix(termin).getFirst();
            POI endPOI = model.createPOI(to.getX(), to.getY(), "Test2");
            //to = getClosestRoadNode(to); IKKE SLET LÆS getClosestRoadNode

            startSearch(startPOI.getClosestNodeWithRoad(), endPOI.getClosestNodeWithRoad());
            //startSearch(from, to); IKKE SLET LÆS getClosestRoadNode

            model.removePOI(startPOI);
            model.removePOI(endPOI);
            view.drawMap();
        }
        else {
            System.out.println("Cannot search for a route without both a start- AND an endpoint!");
        }
    }

    /// ** DO NOT DELETE ** Returns the closest Node which is in a Road, from the given Node
    @Deprecated private Node getClosestRoadNode(Node node) {
        /*
         * Metoden er @Deprecated fordi jeg ikke kunne få ruten til at se ligeså lækker ud,
         * uden at bruge POIs, som med. Samme begrundelse for den udkommenterede -men ikke
         * slettede!- kode i findRouteButton-metoden. Var varsom med at bruge POIs til rute-
         * søgningen fordi de virkede til at drille Joakim's POIs, når man havde andre/lav-
         * ede flere POIs idet/efter man lavede en rutesøgning...
         * SLET IKKE METODEN IN CASE VI PRØVER AT LAVE RUTESØGNINGEN IGEN UDEN POIs!!!!!!!
         */

        Point2D localPoint;
        try { localPoint = view.getTrans().inverseTransform(node.getX(), node.getY()); }
        catch (NonInvertibleTransformException e) { throw new RuntimeException(e); }
        double localX = localPoint.getX();
        double localY = localPoint.getY();

        Tile tile = model.getTilegrid().getTileFromXY((float) localX, (float) localY);
        Road closestRoad = getClosestRoad(tile, localX, localY);
        double closestDistance = Double.POSITIVE_INFINITY;

        for (Node n : closestRoad.getNodes()) {
            double nodeX = n.getX();
            double nodeY = n.getY();
            double distance = Math.sqrt(Math.pow((nodeX - node.getX()), 2) + Math.pow((nodeY - node.getY()), 2)); //Jeg har stjålet MN's afstandsformel 3:) -OFS. a^2 + b^2 = c^2 type shit
            if (distance < closestDistance) {
                closestDistance = distance;
                node = n;
            }
        }
        return node;
    }

    /// Switches the text in the 'From' and 'To' search-bars
    @FXML public void switchDestinationAndStart() {
        if (listView.isVisible()) listView.setVisible(false);
        putTextSwitched = !putTextSwitched;

        String temp = searchBar.getText();
        searchBar.setText(destination.getText());
        destination.setText(temp);
    }

    /// Method runs upon pressing down on the Canvas
    @FXML protected void onCanvasPressed(MouseEvent e) {
        if (model == null) model = Model.getInstance();
        lastX = e.getX();
        lastY = e.getY();
    }

    /**
     * Methods runs upon hovering the mouse on the Canvas. Finds the nearest Road
     * and changes the display text in the bottom left corner of the application.
     */
    @FXML protected void onCanvasHover(MouseEvent e) {
        if (model == null) model = Model.getInstance();
        double x, y;
        Tile tile;

        try {
            Point2D point = view.getTrans().inverseTransform(e.getX(), e.getY());
            x = point.getX();
            y = point.getY();
            tile = model.getTilegrid().getTileFromXY((float) x, (float) y);
        } catch (Exception exception) { return; }

        if (tile != null) {
            Road closestRoad = getClosestRoad(tile, x, y);
            if (closestRoad != null) {
                closestRoadText.setText("Closest road: "+ closestRoad.getRoadName());
            }
        }
        else {
            closestRoadText.setText("Closest road: N/A");
        }
    }

    /**
     * Method determines the closest Road, given a Tile and a set of coordinates in the Tile
     * @return {@code null} if there are no nodes in the Tile
     */
    private Road getClosestRoad(Tile tile, double x, double y) {
        double closestDistance = Double.MAX_VALUE;
        Road closestRoad = null;

        for (Road road : tile.getRoads()) {
            if (road.getRoadName().isEmpty()) continue;
            for (Node node : road.getNodes()) {
                double nodeX = node.getX();
                double nodeY = node.getY();
                double distance = Math.sqrt(Math.pow((nodeX - x), 2) + Math.pow((nodeY - y), 2)); //Jeg har stjålet MN's afstandsformel 3:) -OFS. a^2 + b^2 = c^2 type shit
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestRoad = road;
                }
            }
        }

        return closestRoad;
    }

    /// Method runs upon dragging on the canvas
    @FXML protected void onCanvasDragged(MouseEvent e) {
        mouseEvent = e;
        panRequest = true;
    }
    //endregion

    //region Palette methods
    /// Switches to the default palette
    @FXML private void paletteDefault() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.LIGHTBLUE);
        setMiscColors(Color.BLACK);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("default");
                if (mo instanceof Polygon p) p.setPalette("default");
            }
        }
        view.drawMap();
    }

    /// Switches to the Midnight palette
    @FXML private void paletteMidnight() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.rgb(23, 3, 63));
        setMiscColors(Color.ANTIQUEWHITE);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("midnight");
                if (mo instanceof Polygon p) p.setPalette("midnight");
            }
        }
        view.drawMap();
    }

    /// Switches to the Basic palette
    @FXML private void paletteBasic() {
        if (model == null) model = Model.getInstance();

        view.setBgColor(Color.GHOSTWHITE);
        setMiscColors(Color.INDIGO);
        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("basic");
                if (mo instanceof Polygon p) p.setPalette("basic");
            }
        }
        view.drawMap();
    }

    /// Adjusts a few colors in the interface
    private void setMiscColors(Color color) {
        view.setScaleColor(color);
        scaleText.setFill(color);
        fpsText.setFill(color);
        closestRoadText.setFill(color);
    }
    //endregion

    //region Getters and setters
    /**
     * Sætter Controllerens view-felt til et givent View
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View og Controller kan snakke sammen)
     * @param view View'et som Controllerens view-felt sættes til
     */
    public void setView(View view) { this.view = view; }

    /**
     * Returnerer Controllerens canvas-felt, der "populates" direkte idet en scene FXML-loades
     * (Denne metode bruges kun af View-klassen en enkelt gang, så View kan få Canvas'et af Controlleren)
     * @return Controllerens canvas-felt
     */
    public Canvas getCanvas() { return canvas; }

    public Text getScaleText() { return scaleText; }
    public CheckBox getCheckBoxOBJ() { return checkBoxOBJ; }

    //endregion
}