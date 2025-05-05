package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.MapObject;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.POI;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.PDFOutput;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //region Fields
    private View view;
    private Model model;
    private double lastX, lastY; //Used to pan
    private boolean panRequest, zoomRequest; //Used by AnimationTimer
    private ScrollEvent scrollEvent; //Used to zoom
    private MouseEvent mouseEvent; //Used to pan
    private POI startPOI;
    private POI endPOI;
    private Map<String,POI> favoritePOIs = new HashMap<>();
    private List<POI> oldPOIs = new ArrayList<>();
    private List<POI> deletedPOIs = new ArrayList<>();
    List<Road> latestRoute = new ArrayList<>();
    private List<Node> autoSuggestResults;
    private TextField searchingSource;

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
    @FXML private Button removePOIButton;
    @FXML private Button savePOIButton;
    @FXML private MenuItem POIMenuButton;
    @FXML private Menu POIMenu;
    @FXML private TextField addNamePOI;
    @FXML private Button addToPOIsUI;
    @FXML private Button POIClose;
    @FXML private TextArea addPOIBox;
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
     * Passes the given file into a Model class that starts parsing it
     * @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        boolean createOBJ = checkBoxOBJ != null && checkBoxOBJ.isSelected();
        model = Model.getInstance(mapFile.getPath(), canvas, createOBJ);

        view.setTilegrid(model.getTilegrid());
    }

    /**
     * Runs right after a Controller is created -- configures
     * something(???) for an object in the mapOverlay.fxml scene
     */
    @Override public void initialize(URL url, ResourceBundle resourceBundle) {
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                String selected = listView.getSelectionModel().getSelectedItem();
                searchingSource.setText(selected);
            }
        });
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

        view = new View(view.getStage(), "mapOverlay.fxml");
        loadFile(standardMapFile);

        view.drawMap();
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
    @FXML protected void searchBarTyped(KeyEvent event) {
        if (model == null) model = Model.getInstance();

        TextField source = (TextField) event.getSource();
        searchingSource = source;

        if (source.getId().equals("searchBar")) listView.setLayoutY(58);
        if (source.getId().equals("destination")) listView.setLayoutY(94);

        if (event.getCharacter().equals("\r")) {
            if (!autoSuggestResults.isEmpty()) {
                listView.setVisible(false);

                Node selection = autoSuggestResults.getFirst();
                source.setText(selection.getAddress());

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

            listView.getItems().clear(); //Potential cleanup from earlier search
            listView.setVisible(true); //Make the trie-matches visible

            autoSuggestResults = autoSuggest(input); //Dynamically auto-suggest from user input

            //Changes listView size dynamically
            if (listView.getItems().size() >= 3) listView.setPrefHeight(88);
            if (listView.getItems().size() == 2) listView.setPrefHeight(65);
            if (listView.getItems().size() == 1) listView.setPrefHeight(41);
        }

        //If user wants to search we pick the top node
        /*if (event.getCharacter().equals("\r")) { //If "Enter" is pressed
            Node selection = autoSuggestResults.getFirst();
            view.zoomTo(selection.getX(), selection.getY());
        } else {
            listView.getItems().clear(); //Potential cleanup from earlier search
            String input = searchBar.getText().toLowerCase();

            //if search-bar is empty we return out
            if (input == null || input.isEmpty()) {
                listView.setVisible(false);
                return;
            }

            //Auto suggests dynamically every user input
            listView.setVisible(true);
            autoSuggestResults = autoSuggest(input);

            //Handle look and position of listView
            TextField source = (TextField) event.getSource();
            if (source.getId().equals("searchBar")) listView.setLayoutY(58);
            if (source.getId().equals("destination")) listView.setLayoutY(94);
            if (listView.getItems().size() >= 3) listView.setPrefHeight(88);
            if (listView.getItems().size() == 2) listView.setPrefHeight(51);
            if (listView.getItems().size() == 1) listView.setPrefHeight(27);
        }*/
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


    private void startSearch(Node from, Node to) {
        //First removes the last route from the draws
        if (!latestRoute.isEmpty()) {
            for (Road road : latestRoute) {
                road.setPartOfRoute(false);
                view.removeObjectToDraw(road);
            }
        }

        System.out.println("Starting search...");
        //latestRoute = model.search(startPOI.getClosestNodeWithRoad(), endPOI.getClosestNodeWithRoad());
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
    /// When user chooses a node in the autosuggestion we override the searchbar, and zoom in on the node
    @FXML protected void onAddressPickedFromList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Node chosenNode = autoSuggestResults.get(listView.getSelectionModel().getSelectedIndex());
            searchBar.setText(chosenNode.getAddress());
            view.zoomTo(chosenNode.getX(), chosenNode.getY());
        }
    }

    /**
     * This method is used to save the current POI to a map and add it as a menuitem to the POI menubar to give it delete and find address as options.
     * when deleting the POI it removes it from favoritePOI and adds it to a deletedPOI list which is used to clean the map later.
     */
    @FXML protected void savePOIToHashMap(){
        if(startPOI == null){return;}
        String name = addNamePOI.getText();
        favoritePOIs.put(name, startPOI);
        oldPOIs.remove(startPOI);
        view.addObjectToDraw(startPOI);

        closePOIMenu();

        Menu POIMenuItem = new Menu(name);
        POIMenu.getItems().add(POIMenuItem);

        MenuItem deletePOI = new MenuItem("Delete");
        deletePOI.setOnAction(_ -> {
            view.removeObjectToDraw(startPOI);
            POI poi = favoritePOIs.get(name);
            favoritePOIs.remove(name);
            deletedPOIs.add(poi);
            POIMenu.getItems().remove(POIMenuItem);
            view.removeObjectToDraw(poi);
            model.removePOI(poi);
            view.drawMap();
        });

        MenuItem showAddress = new MenuItem("Show Address");
        showAddress.setOnAction(_ -> {
            POI poi = favoritePOIs.get(name);
            if (poi != null) {
                String address = poi.getNodeAddress();
                    searchBar.setText(address);
            }
        });
        POIMenuItem.getItems().addAll(showAddress, deletePOI);
        System.out.println("Saved POI!: " + startPOI + " with name: " + name);
    }

    @FXML protected void openPOIMenu(){
        poiGroup.setVisible(true);
        addPOIBox.setVisible(true);
        addNamePOI.setVisible(true);
        addNamePOI.clear();
        addToPOIsUI.setVisible(true);
        POIClose.setVisible(true);
    }

    @FXML protected void closePOIMenu(){
        poiGroup.setVisible(false);
        addPOIBox.setVisible(false);
        addNamePOI.setVisible(false);
        addToPOIsUI.setVisible(false);
        POIClose.setVisible(false);
    }

    /// Metode til at fjerne den røde markering på kortet for en POI. Virker kun for den POI, der senest er placeret
    @FXML public void removePOIMarker(POI poi){
        //sæt knappen til visible og kald denne metode et sted
        model.removePOI(poi);
        view.drawMap();
    }

    /// Method to export a route as PDF
    @FXML protected void exportAsPDF(){
        System.out.println("Attempting to export as PDF!");

        List<Road> latestRoute = Model.getInstance().getLatestRoute();

        if (latestRoute != null) {
            try {
                PDFOutput.generateRoute(latestRoute, true);
                System.out.println("PDF-export successful!");
            }
            catch (Exception e) {
                System.out.println("PDF-export failed; "+ e.getMessage());
            }
        }
        else System.out.println("PDF-export failed; no route has been successfully set yet!");
    }

    /// Method to open a textbox with a written guide when "Guide" is pressed
    @FXML protected void guideTextButton(){
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
            //Makes POI
            Affine transform = view.getTrans();
            POI poi = null;
            try {
                Point2D POIMark = transform.inverseTransform(e.getX(), e.getY()); //ændret point til et felt, POIMark
                poi = model.createPOI((float) POIMark.getX(), (float) POIMark.getY(), "Test");
                savePOIButton.setVisible(true);
            } catch (NonInvertibleTransformException exception) {
                System.out.println("Error inversion mouseclick coords!" + exception.getMessage());
            }
            view.drawMap(); //Makes sure that the POI is shown instantly

            //Assigns spot for POI. Sets as start if empty or if "find route" has not been activated, if else, else we set it as the destination
            if (poi != null) {
                onActivateSearch();
                if (searchBar.getText().trim().isEmpty() || !destination.isVisible()) {
                    startPOI = poi;
                    oldPOIs.add(poi);
                } else {
                    endPOI = poi;
                    oldPOIs.add(poi);
                }

                updateSearchText();
            }
            //Removes old POI from the map if they are not added to the list of favorites so there are no more than 2 active at once.

            if (oldPOIs.size() > 2) {
                while (oldPOIs.size() > 2) {
                   // System.out.println(oldPOIs);
                    POI removed = oldPOIs.removeFirst();

                    if (!favoritePOIs.containsValue(removed)) {
                        oldPOIs.remove(removed);
                        removePOIMarker(removed);
                    }
                    //Removes the deleted POI's from the map after they have been deleted via the savePOIToHashMap function
                }
            }
        }
        //endregion
    }

    /// Opens the search menu when activated. If both start- and endPOI are initialized, this button is used for activating the route finding between the two POI's.
    @FXML public void onActivateSearch() {
        findRoute.setVisible(true);
    }

    /// "Find Route" button on UI
    @FXML public void onRouteSearchStart() {
        if (!switchSearch.isVisible() && !destination.isVisible()) {
            switchSearch.setVisible(true);
            destination.setVisible(true);
        }
        else if (!searchBar.getText().trim().isEmpty() && !destination.getText().trim().isEmpty()) {
            String origin = searchBar.getText().toLowerCase();
            String termin = destination.getText().toLowerCase();

            Node from = model.getStreetsFromPrefix(origin).getFirst();
            from = getClosestRoadNode(from);

            Node to = model.getStreetsFromPrefix(termin).getFirst();
            to = getClosestRoadNode(to);

            startSearch(from, to);
        }
        else {
            System.out.println("Cannot search for a route without both a start- AND an endpoint!");
        }

        /*if (startPOI != null && endPOI != null && !searchBar.getText().trim().isEmpty() && !destination.getText().trim().isEmpty()) {
            startSearch();
        } else {
            switchSearch.setVisible(true);
            destination.setVisible(true);
        }*/
    }

    private Node getClosestRoadNode(Node node) {
        Tile tile = model.getTilegrid().getTileFromXY(node.getX(), node.getY());
        Road closestRoad = getClosestRoad(tile, node.getX(), node.getY());
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

    @FXML public void switchDestinationAndStart() {
        String temp = searchBar.getText();
        searchBar.setText(destination.getText());
        destination.setText(temp);

        /*POI temp = startPOI;
        startPOI = endPOI;
        endPOI = temp;
        updateSearchText();*/
    }

    /// Updates the text in the search. Call this after changing the POI responsible for the text
    private void updateSearchText() {
        searchBar.clear();
        destination.clear();

        if (startPOI != null) searchBar.setText(startPOI.getNodeAddress());
        if (endPOI != null) destination.setText(endPOI.getNodeAddress());
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