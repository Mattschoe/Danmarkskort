package com.example.danmarkskort.MVC;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

///A Model is a Singleton class that stores the map in a tile-grid. It also stores the parser which parses the .osm data. Call {@link #getInstance()} to get the Model
public class Model {
    //region Fields
    private static Model modelInstance;
    private final File file;
    private Parser parser;
    private File outputFile; //The output .obj file
    private int numberOfTilesX, numberOfTilesY;
    private Tilegrid tilegrid;
    //endregion

    //region Constructor(s)
    /** Checks what filetype the filepath parameter is.
     *  Calls {@link #parseOBJ()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    private Model(String filePath, Canvas canvas) {
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
                saveParserToOBJ();
            } catch (ParserSavingException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error loading in the parser: " + e.getMessage() + " | with the exception being: " + e.getClass());
            }
        }
        assert parser != null;
        //endregion

        //region Tilegrid
        //Converts into tilegrid if we haven't loaded a tilegrid in via OBJ
        System.out.println("Starting on tilegrid!");
        int tileSize = 11;
        double[] tileGridBounds = getMinMaxCoords();
        Tile[][] tileGrid = initializeTileGrid(tileGridBounds[0], tileGridBounds[1], tileGridBounds[2], tileGridBounds[3], tileSize);

        tilegrid = new Tilegrid(tileGrid, tileGridBounds, tileSize, numberOfTilesX, numberOfTilesY);
        System.out.println("Finished creating Tilegrid!");
        //endregion
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
        for (Polygon polygon : parser.getPolygons().valueCollection()) {
            polygon.determineColor();
        }
    }

    /// Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
    public void saveParserToOBJ() {
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
         for (Node node : parser.getNodes().valueCollection()) {
             int tileX = (int) ((node.getX() - minX) / tileSize);
             int tileY = (int) ((node.getY() - minY) / tileSize);

             //Clamps the x, y so they are within bounds (This avoids floating point errors with 0 or negative numbers
             tileX = Math.min(Math.max(tileX, 0), numberOfTilesX - 1);
             tileY = Math.min(Math.max(tileY, 0), numberOfTilesY - 1);

             tileGrid[tileX][tileY].addMapObject(node);
         }

         //region Adds Roads
         for (Road road : parser.getRoads().valueCollection()) {
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
         //endregion

         //region Adds Polygons
         for (Polygon polygon : parser.getPolygons().valueCollection()) {
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
         //endregion

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
        for (Node node : parser.getNodes().valueCollection()) {
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
    //endregion

    //region Getters and setters
    public Parser   getParser()   { return parser;   }
    public Tilegrid getTilegrid() { return tilegrid; }
    //endregion
}