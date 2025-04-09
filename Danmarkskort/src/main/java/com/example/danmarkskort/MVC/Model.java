package com.example.danmarkskort.MVC;

import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.MapObjects.Tilegrid;
import com.example.danmarkskort.Parser;
import gnu.trove.map.hash.TLongObjectHashMap;
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
     *  Calls {@link #parseOBJToParser()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    private Model(String filePath, Canvas canvas) {
        assert canvas != null;

        file = new File(filePath);
        assert file.exists();

        //region Parser
        //If .obj file
        if (filePath.endsWith(".obj")) {
            try {
                parseOBJToParser();
                System.out.println("Finished deserializing parser!");
            } catch (Exception e) {
                System.out.println("Error loading .obj!: " + e.getMessage());
                System.out.println("Stacktrace:");
                e.getStackTrace();
            }
        } else {
            //If anything else it creates a new parser and tries saves it as .obj
            try {
                parser = new Parser(file);
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
    private void parseOBJToParser() {
        TLongObjectHashMap<Node> id2Node = new TLongObjectHashMap<>(66_289_558);
        TLongObjectHashMap<Road> id2Road = new TLongObjectHashMap<>(2_214_235);
        TLongObjectHashMap<Polygon> id2Polygon = new TLongObjectHashMap<>(6_168_995);

        //region Reading .obj files
        //Parser
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            parser = (Parser) input.readObject();
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading parser!: " + e.getMessage());
        }

        //Nodes
        /* try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("data/StandardMap/nodes.obj")));
            int totalNodes = input.readInt();

            for (int i = 0; i < totalNodes; i++) {
                long id = input.readLong();
                Node node = (Node) input.readObject();
                id2Node.put(id, node);

            }
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading nodes!: " + e.getMessage());
        } */

        //Roads
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("data/StandardMap/roads.obj")));
            int totalRoads = input.readInt();
            for (int i = 0; i < totalRoads; i++) {
                long id = input.readLong();
                Road road = (Road) input.readObject();
                id2Road.put(id, road);
            }
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading roads!: " + e.getMessage());
        }

        //Polygons
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("data/StandardMap/polygons.obj")));
            int totalPolygons = input.readInt();

            for (int i = 0; i < totalPolygons; i++) {
                long id = input.readLong();
                Polygon polygon = (Polygon) input.readObject();
                id2Polygon.put(id, polygon);
            }
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading nodes!: " + e.getMessage());
        }
        //endregion


        //Inserts into parser
        parser.setNodes(id2Node);
        parser.setRoads(id2Road);
        parser.setPolygons(id2Polygon);

        //Closes input and checks for errors
        assert parser != null && parser.getNodes() != null && parser.getRoads() != null && parser.getPolygons() != null;

        //Loads polygons colors after serialization
        for (Polygon polygon : parser.getPolygons().valueCollection()) {
            polygon.determineColor();
        }
    }

    /// Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
    public void saveParserToOBJ() {
        //Saves parser
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/parser.obj"));
            System.out.println("Saving parser...");
            outputStream.writeObject(parser);
            System.out.println("Finished saving parser!");
            System.out.println();
            outputStream.close();
        } catch (Exception e) {
            throw new ParserSavingException("Error saving parser to OBJ!: " + e.getMessage());
        }

        //Saves nodes
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/nodes.obj"));
            outputStream.writeInt( parser.getNodes().size());
            System.out.println("Processing ~" + ((double) parser.getNodes().size()/1_000_000) + " million nodes...");
            int resetCounter = 0;
            for (Long id : parser.getNodes().keys()) {
                outputStream.writeLong(id);
                outputStream.writeObject(parser.getNodes().get(id));
                resetCounter++;
                if (resetCounter % 1_000_000 == 0) {
                    outputStream.reset();
                    System.out.println("Processed: " + (resetCounter/1_000_000) + " million nodes so far!");
                }
            }
            parser.getNodes().clear(); //Clears them so they can be GC'ed
            System.out.println("Finished with nodes!");
            System.out.println();
            outputStream.close();
        } catch (Exception e) {
            throw new ParserSavingException("Error saving nodes as .obj! Error Message: " + e.getMessage());
        }

        //Saves Roads
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/roads.obj"));
            outputStream.writeInt(parser.getRoads().size());
            System.out.println("Processing ~" + ((double) parser.getRoads().size()/1_000_000) + " million roads...");
            int resetCounter = 0;
            for (Long id : parser.getRoads().keys()) {
                outputStream.writeLong(id);
                outputStream.writeObject(parser.getRoads().get(id));
                resetCounter++;
                if (resetCounter % 1_000_000 == 0) {
                    outputStream.reset();
                    System.out.println("Processed: " + (resetCounter/1_000_000) + " million roads so far!");
                }
            }
            System.out.println("Finished with roads!");
            System.out.println();
            outputStream.close();
        } catch (Exception e) {
            throw new ParserSavingException("Error saving roads as .obj! Error Message: " + e.getMessage());
        }


        //Saves Polygons
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/polygons.obj"));
            outputStream.writeInt(parser.getPolygons().size());
            System.out.println("Processing ~" + ((double) parser.getPolygons().size()/1_000_000) + " million polygons...");
            int resetCounter = 0;
            for (Long id : parser.getPolygons().keys()) {
                outputStream.writeLong(id);
                outputStream.writeObject(parser.getPolygons().get(id));
                resetCounter++;
                if (resetCounter % 1_000_000 == 0) {
                    outputStream.reset();
                    System.out.println("Processed: " + (resetCounter/1_000_000) + " million polygons so far!");
                }
            }
            System.out.println("Finished with polygons!");
            System.out.println();
            outputStream.close();
        } catch (Exception e) {
            throw new ParserSavingException("Error saving polygon as .obj! Error Message: " + e.getMessage());
        }
        System.exit(0);
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
        assert minX != Double.POSITIVE_INFINITY && minY != Double.POSITIVE_INFINITY && maxX != Double.NEGATIVE_INFINITY && maxY != Double.NEGATIVE_INFINITY;
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