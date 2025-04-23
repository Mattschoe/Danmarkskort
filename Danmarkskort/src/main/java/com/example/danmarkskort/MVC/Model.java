package com.example.danmarkskort.MVC;

import com.example.danmarkskort.AddressSearch.Street;
import com.example.danmarkskort.AddressSearch.TrieST;
import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.*;
import com.example.danmarkskort.Parser;
import com.example.danmarkskort.Searching.Search;
import gnu.trove.map.hash.TLongObjectHashMap;
import javafx.scene.canvas.Canvas;

import java.io.*;
import java.util.*;

///A Model is a Singleton class that stores the map in a tile-grid. It also stores the parser which parses the .osm data. Call {@link #getInstance()} to get the Model
public class Model {
    //region Fields
    private static Model modelInstance;
    private final File file;
    private Parser parser;
    private File outputFile; //The output .obj file
    private int numberOfTilesX, numberOfTilesY;
    private Tilegrid tilegrid;
    private Search search;
    private List<Road> latestRoute;
    private TrieST<String> trieCity;
    private TrieST<String> trieStreet;
    Set<String> streets;
    Set<String> cities; //Do we need?
    //endregion

    //region Constructor(s)
    /** Checks what filetype the filepath parameter is.
     *  Calls {@link #parseOBJToParser()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    public Model(String filePath, Canvas canvas) {
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
        float[] tileGridBounds = getMinMaxCoords();
        Tile[][] tileGrid = initializeTileGrid(tileGridBounds[0], tileGridBounds[1], tileGridBounds[2], tileGridBounds[3], tileSize);

        tilegrid = new Tilegrid(tileGrid, tileGridBounds, tileSize, numberOfTilesX, numberOfTilesY);
        System.out.println("Finished creating Tilegrid!");
        //endregion

        loadAddressNodes();

        search = new Search(parser.getNodes().valueCollection());
        parser = null; //Fjerner reference til parser så den bliver GC'et
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

        System.out.println("Deserializing parser...");
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
        try {
            File folder = new File("data/StandardMap");
            File[] nodeFiles = folder.listFiles((dir, name) -> name.startsWith("nodes_") && name.endsWith(".obj"));
            assert nodeFiles != null;

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<TLongObjectHashMap<Node>>> futureMap = new ArrayList<>();

            //Runs through each nodefile and makes a thread serialize it
            for (File file : nodeFiles) {
                futureMap.add(executor.submit(() -> {
                    TLongObjectHashMap<Node> localMap = new TLongObjectHashMap<>();
                    try {
                        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                        int chunkSize = input.readInt();
                        for (int i = 0; i < chunkSize; i++) {
                            long nodeID = input.readLong();
                            Node node = (Node) input.readObject();
                            localMap.put(nodeID, node);
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading file: " + file.getName() + ", with error: " + e.getMessage());
                    }
                    return localMap;
                }));
            }

            //Adds each localMap from the threads into the collected map
            for (Future<TLongObjectHashMap<Node>> future : futureMap) {
                id2Node.putAll(future.get());
            }
            executor.shutdown();
            System.out.println("- Finished reading nodes!");
        } catch (Exception e) {
            System.out.println("Error reading nodes!: " + e.getMessage());
        }

        //Roads
        try {
            File folder = new File("data/StandardMap");
            File[] roadFiles = folder.listFiles((dir, name) -> name.startsWith("roads_") && name.endsWith(".obj"));
            assert roadFiles != null;

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<TLongObjectHashMap<Road>>> futureMap = new ArrayList<>();

            //Runs through each nodefile and makes a thread serialize it
            for (File file : roadFiles) {
                futureMap.add(executor.submit(() -> {
                    TLongObjectHashMap<Road> localMap = new TLongObjectHashMap<>();
                    try {
                        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                        int chunkSize = input.readInt();
                        for (int i = 0; i < chunkSize; i++) {
                            long roadID = input.readLong();
                            Road road = (Road) input.readObject();
                            localMap.put(roadID, road);
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading file: " + file.getName() + ", with error: " + e.getMessage());
                    }
                    return localMap;
                }));
            }

            //Adds each localMap from the threads into the collected map
            for (Future<TLongObjectHashMap<Road>> future : futureMap) {
                id2Road.putAll(future.get());
            }
            executor.shutdown();
            System.out.println("- Finished reading roads!");
        } catch (Exception e) {
            System.out.println("Error reading roads!: " + e.getMessage());
        }

        //Polygons
        //Roads
        try {
            File folder = new File("data/StandardMap");
            File[] polygonFiles = folder.listFiles((dir, name) -> name.startsWith("polygons_") && name.endsWith(".obj"));
            assert polygonFiles != null;

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<TLongObjectHashMap<Polygon>>> futureMap = new ArrayList<>();

            //Runs through each nodefile and makes a thread serialize it
            for (File file : polygonFiles) {
                futureMap.add(executor.submit(() -> {
                    TLongObjectHashMap<Polygon> localMap = new TLongObjectHashMap<>();
                    try {
                        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                        int chunkSize = input.readInt();
                        for (int i = 0; i < chunkSize; i++) {
                            long polygonID = input.readLong();
                            Polygon polygon = (Polygon) input.readObject();
                            localMap.put(polygonID, polygon);
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading file: " + file.getName() + ", with error: " + e.getMessage());
                    }
                    return localMap;
                }));
            }

            //Adds each localMap from the threads into the collected map
            for (Future<TLongObjectHashMap<Polygon>> future : futureMap) {
                id2Polygon.putAll(future.get());
            }
            executor.shutdown();
            System.out.println("- Finished reading polygons!");
        } catch (Exception e) {
            System.out.println("Error reading polygons!: " + e.getMessage());
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
            System.out.println("Saving nodes...");
            int numberOfChunks = 8;
            TLongObjectHashMap<Node> nodes = parser.getNodes();
            long[] nodeIDs = nodes.keySet().toArray(); //Need this to split it into chunks
            int amountOfNodes = nodes.size();
            int chunkSize = (int) Math.ceil((double) amountOfNodes / numberOfChunks); //Splits all nodes into chunks

            for (int i = 0; i < numberOfChunks; i++) {
                //Determines the start and end indexes for each chunks
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, amountOfNodes);

                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/nodes_" + i + ".obj"));
                outputStream.writeInt(end - start); //Amount of nodes in this chunk

                //Saves all nodes that the chunk have space for
                for (int j = start; j < end; j++) {
                    long id = nodeIDs[j];
                    outputStream.writeLong(id);
                    outputStream.writeObject(nodes.get(id));
                }
                outputStream.close();
                System.out.println("Saved chunk " + i + " with " + (end - start) + " amount of nodes");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving nodes to OBJ!: " + e.getMessage());
        }

        //Saves Roads
        /* try {
            System.out.println("Saving roads...");
            int numberOfChunks = 8;
            TLongObjectHashMap<Road> roads = parser.getRoads();
            long[] roadIDs = roads.keySet().toArray(); //Need this to split it into chunks
            int amountOfRoads = roads.size();
            int chunkSize = (int) Math.ceil((double) amountOfRoads / numberOfChunks); //Splits all nodes into chunks

            for (int i = 0; i < numberOfChunks; i++) {
                //Determines the start and end indexes for each chunks
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, amountOfRoads);

                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/roads_" + i + ".obj"));
                outputStream.writeInt(end - start); //Amount of nodes in this chunk

                //Saves all nodes that the chunk have space for
                for (int j = start; j < end; j++) {
                    long id = roadIDs[j];
                    outputStream.writeLong(id);
                    outputStream.writeObject(roads.get(id));
                }
                outputStream.close();
                System.out.println("Saved chunk " + i + " with " + (end - start) + " amount of roads!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving roads to OBJ!: " + e.getMessage());
        }

         */


        //Saves Polygons
        try {
            System.out.println("Saving polygons...");
            int numberOfChunks = 16;
            TLongObjectHashMap<Polygon> polygons = parser.getPolygons();
            long[] polygonID = polygons.keySet().toArray(); //Need this to split it into chunks
            int amountOfPolygons = polygons.size();
            int chunkSize = (int) Math.ceil((double) amountOfPolygons / numberOfChunks); //Splits all nodes into chunks

            for (int i = 0; i < numberOfChunks; i++) {
                //Determines the start and end indexes for each chunks
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, amountOfPolygons);

                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/StandardMap/polygons_" + i + ".obj"));
                outputStream.writeInt(end - start); //Amount of nodes in this chunk

                //Saves all nodes that the chunk have space for
                for (int j = start; j < end; j++) {
                    long id = polygonID[j];
                    outputStream.writeLong(id);
                    outputStream.writeObject(polygons.get(id));
                }
                outputStream.close();
                System.out.println("Saved chunk " + i + " with " + (end - start) + " amount of polygons!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving polygons to OBJ!: " + e.getMessage());
        }
        System.exit(0);
    }

    ///Creates a POI and stores it into its given Tile
    public POI createPOI(float localX, float localY, String name) {
        Tile tile = tilegrid.getTileFromXY(localX, localY);
        if (tile == null) return null; //No tile within point
        POI POI = new POI(localX, localY, name, tile);
        tile.addPOI(POI);
        return POI;
    }

    /**
     * Starts a search from {@code startNode} to {@code endNode}
     * @return A structured list of all roads in the route. Returns null if route not found
     */
    public List<Road> search(Node startNode, Node endNode) {
        search.route(startNode, endNode);
        return search.getRoute();
    }

    /// Initializes the maps tile-grid and puts alle the MapObjects in their respective Tile
    private Tile[][] initializeTileGrid(float minX, float minY, float maxX, float maxY, int tileSize) {
        //Calculates number of tiles along each axis
        numberOfTilesX = (int) Math.ceil((maxX - minX) / tileSize);
        numberOfTilesY = (int) Math.ceil((maxY - minY) / tileSize);

        //Initializes the Tile objects inside the grid variable
         Tile[][] tileGrid = new Tile[numberOfTilesX][numberOfTilesY];
         for (int x = 0; x < numberOfTilesX; x++) {
             for (int y = 0; y < numberOfTilesY; y++) {
                 float i = Math.min(minX + x * tileSize, maxX);
                 float j = Math.min(minY + y * tileSize, maxY);
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
         for (Road road : parser.getRoads()) {
             //Converts start- and endXY to tile sizes
             float[] boundingBox = road.getBoundingBox();
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
             float[] boundingBox = polygon.getBoundingBox();
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

    /// @return the minimum x and y coordinate and the maximum. Used for splitting that box into tiles}
    private float[] getMinMaxCoords() {
        float[] minMaxCoords = new float[4];
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        //Loops through each node and gets the minimum and maximum node
        for (Node node : parser.getNodes().valueCollection()) {
            float nodeX = node.getX();
            float nodeY = node.getY();

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

    /// Saves the Tile gid to a OBJ file so we cant fast load it later
    private void saveTileGridToOBJ() {
        //TODO MAKE THIS WORK ???
    }

    /**
     * Inserts all streets and cities of the complex nodes to Tries
     */
    private void loadAddressNodes() {
        //TODO
        //Man skal panne for at skabe en instans af model??
        //æøå issues
        //Hvis man trykker på forslag og derfor enter = null
        //vejnavnene skal komme op i forskelllige byer (evt. dette)
        trieCity = new TrieST<>(true);
        trieStreet = new TrieST<>(false);
        streets = new HashSet<>();
        cities = new HashSet<>();
        int testCounter = 0; //TESTING
        for (Node node : parser.getAddressNodes()) { //gennemgår alle address nodes
            String[] address = node.getAddress();

            //Byer indsættes i trien til byer
            if (address[0] != null && !cities.contains(address[0])) { //Hvis byen ikke allerede er indlæst
                trieCity.put(address[0], node);
                cities.add(address[0]);
            }

            //streets.add(address[3]); //Nu er vejnavnet indsat!
            trieStreet.put(address[3], node);
        }
    }



    //endregion

    //region Getters and setters
    public Parser   getParser()   { return parser;   }
    public Tilegrid getTilegrid() { return tilegrid; }
    public List<Road> getLatestRoute() { return latestRoute; }
    public void setLatestRoute(List<Road> route) { latestRoute = route; }
    public TrieST<String> getTrieCity() {
        return trieCity;
    }
    public TrieST<String> getTrieStreet() {
        return trieStreet;
    }
    public Set<String> getStreets() {
        return streets;
    }
    public Set<String> getCities() {
        return cities;
    }

    //endregion
}