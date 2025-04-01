package com.example.danmarkskort.MVC;

import com.example.danmarkskort.MapObjects.Tile;
import javafx.animation.AnimationTimer;
import com.example.danmarkskort.AddressSearch.TrieST;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //region fields
    View view;
    Model model;
    File standardMapFile;
    @FXML Label valgtFil;
    @FXML Canvas canvas;
    @FXML
    TextField searchBar;
    @FXML
    ListView<String> viewList;
    double lastX, lastY;
    boolean panRequest, zoomRequest;
    MouseEvent mouseEvent;
    ScrollEvent scrollEvent;
    TrieST<String> trieCity; //part of test
    TrieST<String> trieStreet;
    MouseEvent event;
    //endregion

    /** View-konstruktøren skaber/kører en instans af
     * konstruktøren her, når den loader en FXML-scene
     */
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
        //endregion
    }

    /**
     * Passes the given file into a Model class that starts parsing it
     * @param mapFile the file which the map is contained. Given by user when choosing file
     */
    private void loadFile(File mapFile) {
        model = Model.getInstance(mapFile.getPath(), canvas);
        view.setTilegrid(model.getTilegrid());
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
    @FXML private ListView<String> listView;

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
        view.zoom(e.getX(), e.getY(), Math.pow(1.01, factor), false);
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