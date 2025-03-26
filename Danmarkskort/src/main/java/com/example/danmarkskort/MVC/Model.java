package com.example.danmarkskort.MVC;

import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.MapObject;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import com.example.danmarkskort.Parser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.util.*;

public class Model {
    //region Fields
    private File file;
    private Parser parser;
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private File outputFile; //The output .obj file
    private Tile[][] tileGrid;
    //endregion

    /**
     * Checks what filetype the filepath parameter is. Calls {@link #parseOBJ()} if it's a .obj file, if not, it creates a new {@link Parser} class and propagates the responsibility
     * @param filePath the path where the file that needs parsing is loaded (ex.: "/data/small.osm")
     */
    public Model(String filePath, Canvas canvas) {
        assert canvas != null;
        file = new File(filePath);
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        assert file.exists();

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

        //Converts into tilegrid
        initializeTileGrid(0, 0,100,   100, 10);
        // System.out.println(biggestX + " " + biggestY);

        double[] coords = getMinMaxCoords();
        System.out.println("Min: " + coords[0] + " " + coords[1] + " | max: " + coords[2] + " " + coords[3]);

        //TESTING
        for (int x = 0; x < tileGrid.length; x++) {
            for (int y = 0; y < tileGrid[x].length; y++) {
                tileGrid[x][y].draw(graphicsContext);
                // System.out.println("(" + x + "," + y + ") " + tileGrid[x][y].getObjectsInTile().size());
                /* for (MapObject object : tileGrid[x][y].getObjectsInTile()) {
                    System.out.println(object);
                } */
                // System.out.println();
            }
        }

        //region TESTING
        //Search search = new Search(getAllNodesWithStreetAddresses(parser.getNodes().values()));
        //endregion
    }

    /**
     * Parses a .obj file. This method is called in the Parser constructer if the givin filepath ends with .obj
     */
    private void parseOBJ() throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        parser = (Parser) input.readObject();
        input.close();
    }

    /**
     * Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
     */
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

    /**
     * Initializes the maps tile-grid and puts alle the MapObjects in their respective Tile
     */
    private void initializeTileGrid(double minX, double minY, double maxX, double maxY, int tileSize) {
        //Calculates number of tiles along each axis
        int numberOfTilesX = (int) Math.ceil((maxX - minX) / tileSize);
        int numberOfTilesY = (int) Math.ceil((maxY - minY) / tileSize);

        //Initializes the Tile objects inside the grid variable
         tileGrid = new Tile[numberOfTilesX][numberOfTilesY];
         for (int x = 0; x < numberOfTilesX; x++) {
             for (int y = 0; y < numberOfTilesY; y++) {
                 tileGrid[x][y] = new Tile();
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
    }

    /**
     * @return the minimum x and y coordinate and the maximum. Used for splitting that box into tiles in {@link #initializeTileGrid(double, double, double, double, int)}
     */
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


    /**
     * Saves the Tile gid to a OBJ file so we cant fast load it later
     */
    private void saveTileGridToOBJ() {

    }

    //region getters and setters

    /**
     * Gives all nodes that contains an address
     * @param allNodes all nodes parsed in the parser
     * @return all nodes with a street address (f.ex: "Decembervej")
     */
    public Set<Node> getAllNodesWithStreetAddresses(Collection<Node> allNodes) {
        Set<Node> nodesWithStreetAddresses = new HashSet<>();
        for (Node node : allNodes) {
            try {
                assert node.getAddress()[3] != null;
                nodesWithStreetAddresses.add(node);
            } catch (NullPointerException _) {} //Doesn't have a street address
        }
        return nodesWithStreetAddresses;
    }
    public Parser getParser() {
        return parser;
    }
    //endregion
}
