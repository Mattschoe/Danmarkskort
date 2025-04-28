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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.*;

///A Model is a Singleton class that stores the map in a tile-grid. It also stores the parser which parses the .osm data. Call {@link #getInstance()} to get the Model
public class Model {
    //region Fields
    private static Model modelInstance;
    private final File file;
    private Parser parser;
    private int numberOfTilesX, numberOfTilesY;
    private final Tilegrid tilegrid;
    private final Search search;
    private List<Road> latestRoute;
    private TrieST trieCity;
    private TrieST trieStreet;
    private Map<String, Node> citiesToNode;
    private int numberOfChunks;
    //endregion

    //region Constructor(s)
    /** Checks what filetype the filepath parameter is.
     *  Calls {@link #parseOBJToParser()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    public Model(String filePath, Canvas canvas) {
        assert canvas != null;

        file = new File(filePath);
        assert file.exists();

        numberOfChunks = 8;

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
        //parser = null; //Fjerner reference til parser så den bliver GC'et
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
        TLongObjectHashMap<Node> ID2Node = new TLongObjectHashMap<>(66_289_558);
        Set<Road> roads = new HashSet<>(2_214_235);
        Set<Polygon> polygons = new HashSet<>(6_168_995);

        System.out.println("Deserializing parser...");
        //region Reading .obj files
        //region Parser
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            parser = (Parser) input.readObject();
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading parser!: " + e.getMessage());
        }
        //endregion

        //region Nodes
        System.out.println("- Deserializing nodes...");
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numberOfChunks); //Spawn a thread for each chunk
            List<Future<TLongObjectHashMap<Node>>> futures = new ArrayList<>(numberOfChunks);

            //Makes each thread deserialize a chunk
            for (int i = 0; i < numberOfChunks; i++) {
                final String path = "data/StandardMap/nodes_" + i + ".bin";
                futures.add(executor.submit(() -> deserializeNodeChunk(path)));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); //Waiting for all threads to finish.

            //Collects results
            for (Future<TLongObjectHashMap<Node>> future : futures) {
                ID2Node.putAll(future.get());
            }
        } catch (Exception e) {
            System.out.println("Error reading nodes! " + e.getMessage());
        }
        //endregion

        //region Roads
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numberOfChunks);
            List<Future<List<Road>>> futures = new ArrayList<>(numberOfChunks);

            //Makes each thread deserialize a chunk
            for (int i = 0; i < numberOfChunks; i++) {
                final String path = "data/StandardMap/roads_" + i + ".bin";
                futures.add(executor.submit(() -> deserializeRoadChunk(path)));
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); //Waiting for all threads to finish.

            //Collects results
            for (Future<List<Road>> future : futures) {
                roads.addAll(future.get());
            }
        } catch (Exception e) {
            System.out.println("Error reading Roads! " + e.getMessage());
        }
        //endregion


        //Inserts into parser
        parser.setNodes(ID2Node);
        parser.setRoads(roads);
        //parser.setPolygons(polygons);

        //Closes input and checks for errors
        assert parser != null && parser.getNodes() != null && parser.getRoads() != null && parser.getPolygons() != null;

        //Loads polygons colors after serialization
        for (Polygon polygon : parser.getPolygons().valueCollection()) {
            polygon.determineColor();
        }

        //Adds roads back into nodes adjacency list
        for (Road road : parser.getRoads()) {
            for (Node node : road.getNodes()) {
                node.addEdge(road);
            }
        }
    }

    ///Deserializes a single node chunk of a ".bin" file
    private TLongObjectHashMap<Node> deserializeNodeChunk(String path) throws IOException {
        FileChannel fileChannel = new RandomAccessFile(path, "r").getChannel();
        MappedByteBuffer inputBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        int nodeCount = inputBuffer.getInt();

        TLongObjectHashMap<Node> ID2Node = new TLongObjectHashMap<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            long ID = inputBuffer.getLong();
            float x = inputBuffer.getFloat();
            float y = inputBuffer.getFloat();
            String city = readNodeString(inputBuffer);
            String houseNumber = readNodeString(inputBuffer);
            short postcode = inputBuffer.getShort();
            String street = readNodeString(inputBuffer);
            double distanceTo = inputBuffer.getDouble();

            Node newNode = new Node(x, y, city, houseNumber, postcode, street);
            newNode.setDistanceTo(distanceTo);
            ID2Node.put(ID, newNode);
        }
        return ID2Node;
    }

    ///Deserializes a single road chunk of a ".bin" file
    private List<Road> deserializeRoadChunk(String path) throws IOException {
        return null;
    }

    ///Reads the string saved in the parser. Can return null if marked by "-1" in binary file (See write method)
    private String readNodeString(MappedByteBuffer inputBuffer) {
        int length = inputBuffer.getInt();
        if (length < 0) return null;
        byte[] data = new byte[length];
        inputBuffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    private String readRoadString(MappedByteBuffer inputBuffer) {
        return null;
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


        //region Saves nodes
        try {
            System.out.println("Saving nodes...");
            //List<Node> nodes = new ArrayList<>(parser.getNodes().valueCollection());

            TLongObjectHashMap<Node> ID2Node = parser.getNodes();
            long[] nodeIDs = ID2Node.keySet().toArray();
            int amountOfNodes = ID2Node.size();
            int chunkSize = (int) Math.ceil((double) amountOfNodes / numberOfChunks); //Splits all nodes into chunks

            //Saves nodes into chunks
            for (int i = 0; i < numberOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, ID2Node.size());
                if (start >= end) break; //Edgecase

                //Initializes a Chunk
                List<Long> chunk = new ArrayList<>(chunkSize);
                for (int j = start; j < end; j++) chunk.add(nodeIDs[j]);

                long totalBytes = Integer.BYTES + computeNodeChunkSize(chunk, ID2Node); //Int is for chunk size

                FileChannel fileChannel = new RandomAccessFile(("data/StandardMap/nodes_" + i + ".bin"), "rw").getChannel();
                MappedByteBuffer outputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, totalBytes);

                //Serializing chunk:
                outputBuffer.putInt(chunk.size()); //So we now how much to loop through later
                for (Long ID : chunk) {
                    Node node = ID2Node.get(ID);
                    //ID
                    outputBuffer.putLong(ID);

                    //XY
                    outputBuffer.putFloat(node.getX());
                    outputBuffer.putFloat(node.getY());

                    //region Address. If address is null we parse -1 so we know later that its a null.
                    //City
                    if (node.getCity() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = node.getCity().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }

                    //House number
                    if (node.getHouseNumber() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = node.getHouseNumber().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }

                    outputBuffer.putShort(node.getPostcode()); //Postcode

                    //Street
                    if (node.getStreet() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = node.getStreet().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }
                    //endregion

                    //Other
                    outputBuffer.putDouble(node.getDistanceTo());
                }
                outputBuffer.force(); //Flushes to disk
                System.out.println("Saved chunk " + i + " with " + (end - start) + " amount of nodes!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving nodes to OBJ!: " + e.getMessage());
        }
        //endregion

        //region Roads
        try {
            System.out.println("Saving roads...");
            List<Road> roads = new ArrayList<>(parser.getRoads()) ;
            int amountOfRoads = roads.size();
            //int chunkSize = (int) Math.ceil((double) amountOfNodes / numberOfChunks); //Splits all nodes into chunks

            //Saves nodes into chunks
            for (int i = 0; i < numberOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, roads.size());
                if (start >= end) break; //Edgecase

                List<Road> chunk = roads.subList(start, end);
                long totalBytes = Integer.BYTES + computeRoadChunkSize(chunk); //Int is for chunk size

                FileChannel fileChannel = new RandomAccessFile(("data/StandardMap/roads_" + i + ".bin"), "rw").getChannel();
                MappedByteBuffer outputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, totalBytes);

                //Serializing chunk:
                outputBuffer.putInt(chunk.size()); //So we now how much to loop through later
                for (Road road : chunk) {

                }
                outputBuffer.force(); //Flushes to disk
                System.out.println("Saved chunk " + i + " with " + (end - start) + " amount of nodes!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving nodes to OBJ!: " + e.getMessage());
        }
        //endregion

        System.exit(0);
    }

    ///Computes how much space is needed to be allocated in the chunk
    private long computeNodeChunkSize(List<Long> nodeIDs, TLongObjectHashMap<Node> ID2Node) {
        long size = 0;
        for (Long ID : nodeIDs) {
            Node node = ID2Node.get(ID);
            size += Long.BYTES; //ID
            size += Float.BYTES*2; //XY
            if (node.getCity() != null) size += Integer.BYTES + node.getCity().getBytes(StandardCharsets.UTF_8).length; //Int for reading amount of bytes
            else size += Integer.BYTES; //Space for "-1"
            if (node.getHouseNumber() != null) size += Integer.BYTES + node.getHouseNumber().getBytes(StandardCharsets.UTF_8).length;
            else size += Integer.BYTES; //Space for "-1"
            size += Short.BYTES; //Postcode
            if (node.getStreet() != null) size += Integer.BYTES + node.getStreet().getBytes(StandardCharsets.UTF_8).length;
            else size += Integer.BYTES; //Space for "-1"
            size += Double.BYTES; //distanceTo
        }
        return size;
    }

    ///Computes how much space is needed to be allocated in the chunk
    private long computeRoadChunkSize(List<Road> roads) {
        long size = 0;
        for (Road road : roads) {
            //MANGLER NODES
            size += 3; //foot, bicycle, isDrivable
            size += Integer.BYTES; //MaxSpeed
            size += Integer.BYTES + road.getType().getBytes(StandardCharsets.UTF_8).length;
            size += Integer.BYTES + road.getRoadName().getBytes(StandardCharsets.UTF_8).length;
        }
        return size;
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

         //region Adds Nodes
         for (Node node : parser.getNodes().valueCollection()) {
             int tileX = (int) ((node.getX() - minX) / tileSize);
             int tileY = (int) ((node.getY() - minY) / tileSize);

             //Clamps the x, y so they are within bounds (This avoids floating point errors with 0 or negative numbers
             tileX = Math.min(Math.max(tileX, 0), numberOfTilesX - 1);
             tileY = Math.min(Math.max(tileY, 0), numberOfTilesY - 1);

             tileGrid[tileX][tileY].addMapObject(node);
         }
         //endregion

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


    /**
     * Inserts all streets and cities of the complex nodes to Tries
     */
    private void loadAddressNodes() {
        trieCity = new TrieST();
        trieStreet = new TrieST();
        citiesToNode = new HashMap<>();
        for (Node node : parser.getAddressNodes()) { //gennemgår alle address nodes
            String street = node.getStreet();
            String city = node.getCity();

            //Inserts a representative node of a city.
            if (city != null) citiesToNode.put(city, node);

            //Insert node into streets if it has an address
            if (street != null) trieStreet.put(node);
        }
        //Adds nodes to the city trie
        for (Node node : citiesToNode.values()) {
            trieCity.put(new SimpleNode(node.getCity(), node));
        }
    }

    ///Returns a list of city-nodes that are correlated with the given {@code prefix} from the trie
    public List<Node> getCitiesFromPrefix(String prefix) {
        return trieCity.keysWithPrefix(prefix);
    }

    ///Returns a list of street-nodes that are correlated with the given {@code prefix} from the trie
    public List<Node> getStreetsFromPrefix(String prefix) {
        return trieStreet.keysWithPrefix(prefix);
    }
    //endregion

    //region Getters and setters
    public Tilegrid getTilegrid() { return tilegrid; }
    public List<Road> getLatestRoute() { return latestRoute; }
    public void setLatestRoute(List<Road> route) { latestRoute = route; }
    //endregion
}