package com.example.danmarkskort.MVC;

import com.example.danmarkskort.AddressSearch.Street;
import com.example.danmarkskort.AddressSearch.TrieST;
import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.MapObjects.Tilegrid;
import com.example.danmarkskort.Parser;
import javafx.scene.canvas.Canvas;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

///A Model is a Singleton class that stores the map in a tile-grid. It also stores the parser which parses the .osm data. Call {@link #getInstance()} to get the Model
public class Model {
    //region Fields
    private static Model modelInstance;
    private final File file;
    private Parser parser;
    private File outputFile; //The output .obj file
    private int numberOfTilesX, numberOfTilesY;
    private final Tilegrid tilegrid;
    private TrieST<String> trieCity;
    private TrieST<String> trieStreet;
    //endregion

    //region Constructor(s)
    /** Checks what filetype the filepath parameter is.
     *  Calls {@link #parseOBJ()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    public Model(String filePath, Canvas canvas) {
        assert canvas != null;

        file = new File(filePath);
        assert file.exists();

        //region Parser
        //If .obj file
        if (filePath.endsWith(".obj")) {
            try {
                parseOBJ();
            } catch (Exception e) {
                System.out.println("Error loading .obj!: " + e.getMessage());
                System.out.println("Stacktrace:");
                e.getStackTrace();
            }
        } else {
            //If anything else it creates a new parser and tries saves it as .obj
            try {
                parser = new Parser(file);
                //saveParserToOBJ(); I STYKKER :(
            } catch (ParserSavingException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error loading in the parser: " + e.getMessage() + " | with the exception being: " + e.getClass());
            }
        }
        assert parser != null;
        //endregion

        //region Tilegrid
        //Converts into tilegrid
        int tileSize = 10;
        double[] tileGridBounds = getMinMaxCoords();
        Tile[][] tileGrid = initializeTileGrid(tileGridBounds[0], tileGridBounds[1], tileGridBounds[2], tileGridBounds[3], tileSize);

        tilegrid = new Tilegrid(tileGrid, tileGridBounds, tileSize, numberOfTilesX, numberOfTilesY);
        //endregion

        loadAddressNodes();
    }
    //endregion

    //region Methods
    /** Method used to initialize the singleton Model. Method is only meant to be called once, for getting the instance, call {@link #getInstance()}
     *  @param filePath the path where the file that needs parsing is loaded (ex.: "/data/small.osm")
     *  @param canvas the Canvas which the scene is drawn upon
     *  @return Model (Singleton)
     */
    public static Model getInstance(String filePath, Canvas canvas) {
        if (modelInstance == null) {
            modelInstance = new Model(filePath, canvas);
        }
        return modelInstance;
    }

    /** Method used to get the singleton Model. The method {@link #getInstance(String, Canvas)} HAS to be called first to initialize the singleton
     *  @return Model (Singleton)
     *  @throws IllegalStateException if the singleton is not initialized
     */
    public static Model getInstance() {
        if (modelInstance == null) {
            throw new IllegalStateException("Singleton is not initialized, Call getInstance(String filePath, Canvas canvas) first.");
        }
        return modelInstance;
    }

    /// Parses a .obj file. This method is called in the Parser constructor if the given filepath ends with .obj
    private void parseOBJ() throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        parser = (Parser) input.readObject();
        input.close();
    }

    /// Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
    private void saveParserToOBJ() {
        outputFile = new File(file+".obj");
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFile));
            outputStream.writeObject(parser);
            outputStream.close();
        } catch (IOException e) {
            throw new ParserSavingException("Error saving parser as .obj! Error Message: " + e.getMessage());
        }
    }

    /// Initializes the maps tile-grid and puts alle the MapObjects in their respective Tile
    private Tile[][] initializeTileGrid(double minX, double minY, double maxX, double maxY, int tileSize) {
        //Calculates number of tiles along each axis
        numberOfTilesX = (int) Math.ceil((maxX - minX) / tileSize);
        numberOfTilesY = (int) Math.ceil((maxY - minY) / tileSize);

        //Initializes the Tile objects inside the grid variable
         Tile[][] tileGrid = new Tile[numberOfTilesX][numberOfTilesY];
         for (int x = 0; x < numberOfTilesX; x++) {
             for (int y = 0; y < numberOfTilesY; y++) {
                 double i = Math.min(minX + x * tileSize, maxX);
                 double j = Math.min(minY + y * tileSize, maxY);
                 tileGrid[x][y] = new Tile(i, j, i + tileSize, j + tileSize, tileSize);
             }
         }

         //Adds Nodes
         for (Node node : parser.getNodes().values()) {
             int tileX = (int) ((node.getX() - minX) / tileSize);
             int tileY = (int) ((node.getY() - minY) / tileSize);

             //Clamps the x, y so they are within bounds (This avoids floating point errors with 0 or negative numbers
             tileX = Math.min(Math.max(tileX, 0), numberOfTilesX - 1);
             tileY = Math.min(Math.max(tileY, 0), numberOfTilesY - 1);

             tileGrid[tileX][tileY].addMapObject(node);
         }

         //Adds Roads
         for (Road road : parser.getRoads().values()) {
             //Converts start- and endXY to tile sizes
             double[] boundingBox = road.getBoundingBox();
             int startTileX = (int) ((boundingBox[0] - minX) / tileSize);
             int startTileY = (int) ((boundingBox[1] - minY) / tileSize);
             int endTileX = (int) ((boundingBox[2] - minX) / tileSize);
             int endTileY = (int) ((boundingBox[3] - minY) / tileSize);

             //Clamps the x, y so they are within bounds (This avoids floating point errors with 0 or negative numbers
             startTileX = Math.min(Math.max(startTileX, 0), numberOfTilesX - 1);
             startTileY = Math.min(Math.max(startTileY, 0), numberOfTilesY - 1);
             endTileX = Math.min(Math.max(endTileX, 0), numberOfTilesX - 1);
             endTileY = Math.min(Math.max(endTileY, 0), numberOfTilesY - 1);

             //Adds all roads to the tiles that overlap the bounding box
             for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                 for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                     tileGrid[tileX][tileY].addMapObject(road);
                 }
             }
         }

         //Adds Polygons
         for (Polygon polygon : parser.getPolygons().values()) {
             //Converts start- and endXY to tile sizes
             double[] boundingBox = polygon.getBoundingBox();
             int startTileX = (int) ((boundingBox[0] - minX) / tileSize);
             int startTileY = (int) ((boundingBox[1] - minY) / tileSize);
             int endTileX = (int) ((boundingBox[2] - minX) / tileSize);
             int endTileY = (int) ((boundingBox[3] - minY) / tileSize);

             //Clamps the x, y so they are within bounds (This avoids floating point errors with 0 or negative numbers
             startTileX = Math.min(Math.max(startTileX, 0), numberOfTilesX - 1);
             startTileY = Math.min(Math.max(startTileY, 0), numberOfTilesY - 1);
             endTileX = Math.min(Math.max(endTileX, 0), numberOfTilesX - 1);
             endTileY = Math.min(Math.max(endTileY, 0), numberOfTilesY - 1);

             //Adds all roads to the tiles that overlap the bounding box
             for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                 for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                     tileGrid[tileX][tileY].addMapObject(polygon);
                 }
             }
         }

         //Initializes all tile grids draw methods
         for (int x = 0; x < numberOfTilesX; x++) {
             for (int y = 0; y < numberOfTilesY; y++) {
                 tileGrid[x][y].initializeDrawMethods();
             }
         }
         return tileGrid;
    }

    /// @return the minimum x and y coordinate and the maximum. Used for splitting that box into tiles in {@link #initializeTileGrid(double, double, double, double, int)}
    private double[] getMinMaxCoords() {
        double[] minMaxCoords = new double[4];
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        //Loops through each node and gets the minimum and maximum node
        for (Node node : parser.getNodes().values()) {
            double nodeX = node.getX();
            double nodeY = node.getY();

            //X
            if (nodeX < minX ) {
                minX = nodeX;
            } else if (nodeX > maxX) {
                maxX = nodeX;
            }

            //Y
            if (nodeY < minY ) {
                minY = nodeY;
            } else if (nodeY > maxY) {
                maxY = nodeY;
            }
        }
        minMaxCoords[0] = minX;
        minMaxCoords[1] = minY;
        minMaxCoords[2] = maxX;
        minMaxCoords[3] = maxY;
        return minMaxCoords;
    }

    /// Saves the Tile gid to a OBJ file so we cant fast load it later
    private void saveTileGridToOBJ() {
        //TODO MAKE THIS WORK ???
    }

    /**
     * Inserts all streets and cities of the complex nodes to Tries
     */
    private void loadAddressNodes() {
        //TODO
        //æøå issues
        //Hvis man trykker på forslag og derfor enter = null
        //HÅNDTERING AF VEJE MED SAMME VEJNAVN
        trieCity = new TrieST<>(true);
        trieStreet = new TrieST<>(false);
        Set<String> streets = new HashSet<>();
        Set<String> cities = new HashSet<>();
        int testCounter = 0; //TESTING
        for (Node node : parser.getAddressNodes()) { //gennemgår alle address nodes
            String[] address = node.getAddress();

            //Byer indsættes i trien til byer
            if (address[0] != null && !cities.contains(address[0])) { //Hvis byen ikke allerede er indlæst
                trieCity.put(address[0], node);
                cities.add(address[0]);
            }

            if (address[3] != null) { //street NOGET GÅR GALT I STREET
                if (streets.contains(address[3])) {//Hvis vejnavnet ALLEREDE ER TAGET
                    int streetCounter = 0;
                    for (Node streetNode : trieStreet.getList(address[3])) { //Kører gennem listen af noder med samme vejnavn
                            if (!streetNode.getCity().equals(address[0])) {
                            streetCounter++;
                            }
                    }
                    if (streetCounter == trieStreet.getList(address[3]).size()) { //Hvis navnet er unikt!
                        trieStreet.put(address[3], node);
                        for (Node nnode : trieStreet.getList(address[3])) { //Test condition
                            System.out.println(address[3] + " i byen " + nnode.getCity());
                        }
                    }
                } else {
                streets.add(address[3]); //Nu er vejnavnet indsat!
                trieStreet.put(address[3], node);
            }
            }
        }

                //

        }



    //endregion

    //region Getters and setters
    public Parser   getParser()   { return parser;   }
    public Tilegrid getTilegrid() { return tilegrid; }
    public TrieST<String> getTrieCity() {
        return trieCity;
    }
    public TrieST<String> getTrieStreet() {
        return trieStreet;
    }
    //endregion
}