package com.example.danmarkskort.MVC;

import com.example.danmarkskort.AddressSearch.TrieST;
import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.*;
import com.example.danmarkskort.Parser;
import com.example.danmarkskort.Searching.Search;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import javafx.scene.canvas.Canvas;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * A Model is a Singleton class that stores the map in a tile-grid.
 * It also stores the parser which parses the .osm data.
 * Call {@link #getInstance()} to get the Model
 */
public class Model {
    //region Fields
    private static Model modelInstance;
    private File file;
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
    /**
     * Checks what filetype the filepath parameter is.
     * Calls {@link #parseOBJToParser()} if it's an OBJ-file, if not, creates a new {@link Parser} class and propagates the responsibility
     */
    public Model(String filePath, Canvas canvas, boolean createOBJ) {
        assert canvas != null;

        file = new File(filePath);
        assert file.exists();

        String filename = file.getName().split("\\.")[0];
        File possibleOBJ = new File("data/generated/"+ filename +"/parser.obj");
        if (possibleOBJ.exists()) {
            System.out.println("Found OBJ-file! Using that instead...");
            file = possibleOBJ;
        }

        numberOfChunks = 8;

        //region Parser
        //If .obj-file
        if (file.getPath().endsWith(".obj")) {
            try {
                System.out.println("Detected OBJ-file! Attempting to parse...");
                parseOBJToParser();
                System.out.println("Finished deserializing parser!");
            } catch (Exception e) {
                System.out.println("Error loading .obj!: " + e.getMessage());
                System.out.println("Stacktrace:");
                e.getStackTrace();
            }
        } else {
            //If anything else, creates a new parser and tries to save it as .obj
            try {
                if (file.getPath().endsWith(".osm")) System.out.println("Detected OSM-file! Attempting to parse...");
                if (file.getPath().endsWith(".zip")) System.out.println("Detected ZIP-file! Attempting to parse...");

                parser = new Parser(file);
            } catch (ParserSavingException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error loading the parser: "+ e.getClass() +": "+ e.getMessage());
            }
        }
        assert parser != null;
        //endregion

        //region Tilegrid
        //Converts into tilegrid if we haven't loaded a tilegrid in via OBJ
        System.out.println("Starting on Tilegrid!");
        int tileSize = 11;
        float[] tileGridBounds = getMinMaxCoords(parser.getNodes().valueCollection());
        Tile[][] tileGrid = initializeTileGrid(tileGridBounds[0], tileGridBounds[1], tileGridBounds[2], tileGridBounds[3], tileSize);

        tilegrid = new Tilegrid(tileGrid, tileGridBounds, tileSize, numberOfTilesX, numberOfTilesY);
        System.out.println("Finished creating Tilegrid!");
        //endregion

        System.out.println("Loading nodes into address and road searching!");
        loadAddressNodes();
        search = new Search(parser.getNodes().valueCollection());
        if (createOBJ) {
            if (file.getPath().endsWith(".obj")) {
                System.out.println("OBJ-file already exists, won't build a new one...");
            }
            else saveParserToOBJ();
        }
        parser = null; //Fjerner reference til parser så den bliver GC'et
        System.gc();
    }
    //endregion

    //region Methods
    /**
     * Method used to initialize the singleton Model. Method is only meant to be called once, for getting the instance, call {@link #getInstance()}
     * @param filePath the path where the file that needs parsing is loaded (ex.: "/data/small.osm")
     * @param canvas the Canvas which the scene is drawn upon
     * @return Model (Singleton)
     */
    public static Model getInstance(String filePath, Canvas canvas, boolean createOBJ) {
        modelInstance = null;
        System.gc();

        modelInstance = new Model(filePath, canvas, createOBJ);
        return modelInstance;
    }

    /**
     * Method used to get the singleton Model. The method {@link #getInstance(String, Canvas, boolean)} HAS to be called first to initialize the singleton
     * @return Model (Singleton)
     * @throws IllegalStateException if the singleton is not initialized
     */
    public static Model getInstance() {
        if (modelInstance == null) {
            throw new IllegalStateException("Singleton is not initialized, call getInstance(String filePath, Canvas canvas) first.");
        }
        return modelInstance;
    }

    /// Parses a .obj file. This method is called in the Parser constructor if the given filepath ends with .obj
    private void parseOBJToParser() {
        TLongObjectHashMap<Node> ID2Node = new TLongObjectHashMap<>(); //Avoids resizing
        String[] filePath = new String[0];
        if (System.getProperty("os.name").startsWith("Windows")) filePath = file.getPath().split("\\\\");
        else if (System.getProperty("os.name").startsWith("MAC")) filePath = file.getPath().split("/");
        else throw new RuntimeException("UNSUPPORTED OPERATING SYSTEM");
        String folder = filePath[filePath.length - 2];

        System.out.println("Deserializing binary files...");
        //region Reading .obj files
        //region Parser
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            parser = (Parser) input.readObject();
            input.close();
        } catch (Exception e) {
            System.out.println("Error reading parser!: " + e.getMessage());
        }
        System.out.println("- Finished deserializing parser!");
        //endregion

        //region Nodes
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numberOfChunks); //Spawn a thread for each chunk
            List<Future<TLongObjectHashMap<Node>>> futures = new ArrayList<>(numberOfChunks);

            //Makes each thread deserialize a chunk
            for (int i = 0; i < numberOfChunks; i++) {
                final String path;
                if (file.getPath().contains("StandardMap")) path = "data/StandardMap/nodes_" + i + ".bin";
                else path = "data/generated/"+ folder +"/nodes_"+ i +".bin";
                futures.add(executor.submit(() -> deserializeNodeChunk(path)));
            }
            executor.shutdown();

            //region Collects results
            //First ensures that we have space for the data
            List<TLongObjectHashMap<Node>> partialResults = new ArrayList<>();
            int totalNodeCount = 0;
            for (Future<TLongObjectHashMap<Node>> future : futures) {
                TLongObjectHashMap<Node> result = future.get();
                partialResults.add(result);
                totalNodeCount += result.size();
            }
            ID2Node.ensureCapacity(totalNodeCount);

            // Then we merge results
            for (TLongObjectHashMap<Node> partialNodes : partialResults) {
                for (long key : partialNodes.keys()) {
                    ID2Node.put(key, partialNodes.get(key));
                }
                partialNodes.clear();
            }
            //Cleanup
            partialResults.clear();
            futures.clear();
            System.out.println("- Finished deserializing nodes!");
            //endregion
        } catch (Exception e) {
            System.out.println("Error reading nodes! " + e.getMessage());
        }
        //endregion

        float[] minMax = getMinMaxCoords(ID2Node.valueCollection());

        Set<Road> roads = new HashSet<>();
        //region Roads
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numberOfChunks);
            List<Future<Set<Road>>> futures = new ArrayList<>(numberOfChunks);

            //Makes each thread deserialize a chunk
            for (int i = 0; i < numberOfChunks; i++) {
                final String path;
                if (file.getPath().contains("StandardMap")) path = "data/StandardMap/roads_" + i + ".bin";
                else path = "data/generated/"+ folder +"/roads_"+ i +".bin";
                futures.add(executor.submit(() -> deserializeRoadChunk(path, ID2Node)));
            }
            executor.shutdown();

            //region Collects results
            //First ensures that we have space for the data
            List<Set<Road>> partialResults = new ArrayList<>();
            int totalRoadCount = 0;
            for (Future<Set<Road>> future : futures) {
                Set<Road> result = future.get();
                partialResults.add(result);
                totalRoadCount += result.size();
            }
            ID2Node.ensureCapacity(totalRoadCount);

            //Then we merge results
            for (Set<Road> partialRoads : partialResults) {
                roads.addAll(partialRoads);
                partialRoads.clear();
            }
            //Cleanup
            partialResults.clear();
            futures.clear();
            //endregion
            System.out.println("- Finished deserializing roads!");
        } catch (Exception e) {
            System.out.println("Error reading Roads! " + e.getMessage());
        }

        //endregion

        //region Polygons
        Set<Polygon> polygons = Collections.emptySet(); //Instantiates to empty so java doesn't get angry
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numberOfChunks);
            List<Future<Set<Polygon>>> futures = new ArrayList<>(numberOfChunks);

            //Makes each thread deserialize a chunk
            for (int i = 0; i < numberOfChunks; i++) {
                final String path;
                if (file.getPath().contains("StandardMap")) path = "data/StandardMap/polygons_" + i + ".bin";
                else path = "data/generated/"+ folder +"/polygons_"+ i +".bin";
                futures.add(executor.submit(() -> deserializePolygonChunk(path)));
            }
            executor.shutdown();

            //region Collects results
            //First ensures that we have space for the data
            List<Set<Polygon>> partialResults = new ArrayList<>();
            int totalPolygonCount = 0;
            for (Future<Set<Polygon>> future : futures) {
                Set<Polygon> result = future.get();
                partialResults.add(result);
                totalPolygonCount += result.size();
            }
            polygons = new HashSet<>((int) (1 + totalPolygonCount/0.75)); //Instantiates polygons into a HashSet with the proper space for what we are about to add

            //Then we merge results
            for (Set<Polygon> partialRoads : partialResults) {
                polygons.addAll(partialRoads);
                partialRoads.clear();
            }
            //Cleanup
            partialResults.clear();
            futures.clear();
            //endregion
            System.out.println("- Finished deserializing polygons!");
        } catch (Exception e) {
            System.out.println("Error reading Polygons! " + e.getMessage());
        }
        //endregion


        //Inserts into parser
        parser.setNodes(ID2Node);
        parser.setRoads(roads);
        parser.setPolygons(polygons);

        //Closes input and checks for errors
        assert parser != null && parser.getNodes() != null && parser.getRoads() != null && parser.getPolygons() != null;

        //Adds roads back into nodes adjacency list
        for (Road road : parser.getRoads()) {
            for (Node node : road.getNodes()) {
                node.addEdge(road);
            }
        }
    }

    /// Deserializes a single node chunk of a ".bin" file
    private TLongObjectHashMap<Node> deserializeNodeChunk(String path) throws IOException {
        FileChannel fileChannel = new RandomAccessFile(path, "r").getChannel();
        MappedByteBuffer inputBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        int nodeCount = inputBuffer.getInt();

        TLongObjectHashMap<Node> ID2Node = new TLongObjectHashMap<>();
        ID2Node.ensureCapacity(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            long ID = inputBuffer.getLong();
            float x = inputBuffer.getFloat();
            float y = inputBuffer.getFloat();

            String city = readString(inputBuffer);
            String houseNumber = readString(inputBuffer);
            short postcode = inputBuffer.getShort();
            String street = readString(inputBuffer);

            ID2Node.put(ID, new Node(x, y, city, houseNumber, postcode, street));
        }
        return ID2Node;
    }

    /// Deserializes a single road chunk of a ".bin" file
    private Set<Road> deserializeRoadChunk(String path, TLongObjectHashMap<Node> ID2Node) throws IOException {
        FileChannel fileChannel = new RandomAccessFile(path, "r").getChannel();
        MappedByteBuffer inputBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        int roadCount = inputBuffer.getInt();

        Set<Road> roads = new HashSet<>(roadCount);
        for (int i = 0; i < roadCount; i++) {
            //Finds all nodes in road
            List<Node> nodesInRoad = new ArrayList<>();
            int nodeCount = inputBuffer.getInt();
            for (int j = 0; j < nodeCount; j++) {
                long nodeID = inputBuffer.getLong();
                nodesInRoad.add(ID2Node.get(nodeID));
            }

            //Bools
            boolean walkable = inputBuffer.get() != 0;
            boolean bicycle = inputBuffer.get() != 0;
            boolean drivable = inputBuffer.get() != 0;
            boolean oneway = inputBuffer.get() != 0;

            int maxSpeed = inputBuffer.getInt();

            String roadType = readString(inputBuffer);
            String roadName = readString(inputBuffer);
            roads.add(new Road(nodesInRoad, walkable, bicycle, drivable, oneway, maxSpeed, roadType, roadName));
        }
        return roads;
    }

    private Set<Polygon> deserializePolygonChunk(String path) throws IOException {
        FileChannel fileChannel = new RandomAccessFile(path, "r").getChannel();
        MappedByteBuffer inputBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        int polygonCount = inputBuffer.getInt();

        Set<Polygon> polygons = new HashSet<>(polygonCount);
        for (int i = 0; i < polygonCount; i++) {
            //X Points
            int amountOfXPoints = inputBuffer.getInt();
            float[] xPoints = new float[amountOfXPoints];
            for (int x = 0; x < amountOfXPoints; x++) xPoints[x] = inputBuffer.getFloat();

            //Y Points
            int amountOfYPoints = inputBuffer.getInt();
            float[] yPoints = new float[amountOfYPoints];
            for (int y = 0; y < amountOfYPoints; y++) yPoints[y] = inputBuffer.getFloat();

            //Type
            String polygonType = readString(inputBuffer);
            polygons.add(new Polygon(xPoints, yPoints, polygonType));
        }
        return polygons;
    }

    /// Reads the string saved in the parser. Can return null if marked by "-1" in binary file (See write method)
    private String readString(MappedByteBuffer inputBuffer) {
        int length = inputBuffer.getInt();
        if (length < 0) return null;
        byte[] data = new byte[length];
        inputBuffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    /// Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
    private void saveParserToOBJ() {
        String filename = file.getName().split("\\.")[0];

        //Reverse HashMap needed to store node ID in roads. reverses the map so we can store the ID correctly
        TLongObjectHashMap<Node> ID2Node = parser.getNodes();
        TObjectLongHashMap<Node> node2ID = new TObjectLongHashMap(ID2Node.size());
        for (long ID : ID2Node.keys()) {
            node2ID.put(ID2Node.get(ID), ID);
        }

        //region Saves parser
        try {
            File folder = new File("data/generated/"+ filename);
            if (!folder.exists()) //noinspection ResultOfMethodCallIgnored
                folder.mkdir();
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data/generated/"+filename+"/parser.obj"));
            System.out.println("Saving parser...");
            outputStream.writeObject(parser);
            System.out.println("Finished saving parser!");
            System.out.println();
            outputStream.close();
        } catch (Exception e) {
            throw new ParserSavingException("Error saving parser to OBJ!: " + e.getMessage());
        }
        //endregion

        //region Nodes
        try {
            System.out.println("Saving nodes...");
            //List<Node> nodes = new ArrayList<>(parser.getNodes().valueCollection());
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

                FileChannel fileChannel = new RandomAccessFile(("data/generated/"+ filename +"/nodes_" + i + ".bin"), "rw").getChannel();
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
                }
                outputBuffer.force(); //Flushes to disk
                System.out.println("- Saved chunk " + i + " with " + (end - start) + " nodes!");
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
            int chunkSize = (int) Math.ceil((double) amountOfRoads / numberOfChunks); //Splits all nodes into chunks

            //Saves roads into chunks
            for (int i = 0; i < numberOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, roads.size());
                if (start >= end) break; //Edgecase

                List<Road> chunk = roads.subList(start, end);
                long totalBytes = Integer.BYTES + computeRoadChunkSize(chunk); //Int is for chunk size

                FileChannel fileChannel = new RandomAccessFile(("data/generated/"+ filename +"/roads_" + i + ".bin"), "rw").getChannel();
                MappedByteBuffer outputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, totalBytes);

                //Serializing chunk:
                outputBuffer.putInt(chunk.size()); //So we now how much to loop through later
                for (Road road : chunk) {
                    //Saves node ID for later
                    outputBuffer.putInt(road.getNodes().size()); //So we know how nodes are in road later
                    for (Node node : road.getNodes()) outputBuffer.putLong(node2ID.get(node));

                    //Boolean value, order is: 1. Walkable 2. Bicycle 3. Drivable
                    if (road.isWalkable()) outputBuffer.put((byte) 1);
                    else outputBuffer.put((byte) 0);
                    if (road.isBicycle()) outputBuffer.put((byte) 1);
                    else outputBuffer.put((byte) 0);
                    if (road.isDriveable()) outputBuffer.put((byte) 1);
                    else outputBuffer.put((byte) 0);
                    if (road.isOneway()) outputBuffer.put((byte) 1);
                    else outputBuffer.put((byte) 0);

                    //Maxspeed
                    outputBuffer.putInt(road.getMaxSpeed());

                    //RoadTime
                    if (road.getType() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = road.getType().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }

                    //RoadName
                    if (road.getRoadName() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = road.getRoadName().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }
                }
                outputBuffer.force(); //Flushes to disk
                System.out.println("- Saved chunk " + i + " with " + (end - start) + " roads!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving roads to OBJ!: " + e.getMessage());
        }
        //endregion

        //region Polygons
        try {
            System.out.println("Saving polygons...");
            List<Polygon> polygons = new ArrayList<>(parser.getPolygons()) ;
            int amountOfPolygons = polygons.size();
            int chunkSize = (int) Math.ceil((double) amountOfPolygons / numberOfChunks); //Splits all nodes into chunks

            //Saves polygons into chunks
            for (int i = 0; i < numberOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, polygons.size());
                if (start >= end) break; //Edgecase

                List<Polygon> chunk = polygons.subList(start, end);
                long totalBytes = Integer.BYTES + computePolygonChunkSize(chunk); //Int is for chunk size

                FileChannel fileChannel = new RandomAccessFile(("data/generated/"+ filename +"/polygons_" + i + ".bin"), "rw").getChannel();
                MappedByteBuffer outputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, totalBytes);

                //Serializing chunk:
                outputBuffer.putInt(chunk.size()); //So we now how much to loop through later
                for (Polygon polygon : chunk) {
                    //XY Points
                    outputBuffer.putInt(polygon.getXPoints().length);
                    for (float xPoint : polygon.getXPoints()) outputBuffer.putFloat(xPoint);

                    outputBuffer.putInt(polygon.getYPoints().length);
                    for (float yPoint : polygon.getYPoints()) outputBuffer.putFloat(yPoint);

                    //Type
                    if (polygon.getType() == null) outputBuffer.putInt(-1);
                    else {
                        byte[] data = polygon.getType().getBytes(StandardCharsets.UTF_8);
                        outputBuffer.putInt(data.length);
                        outputBuffer.put(data);
                    }
                }
                outputBuffer.force(); //Flushes to disk
                System.out.println("- Saved chunk " + i + " with " + (end - start) + " polygons!");
            }
        } catch (Exception e) {
            throw new ParserSavingException("Error saving polygons to OBJ!: " + e.getMessage());
        }
        //endregion
    }

    /// Computes how much space is needed to be allocated in the chunk
    private long computeNodeChunkSize(List<Long> nodeIDs, TLongObjectHashMap<Node> ID2Node) {
        long size = 0;
        for (Long ID : nodeIDs) {
            Node node = ID2Node.get(ID);
            size += Long.BYTES; //ID
            size += Float.BYTES*2; //XY
            if (node.getCity() != null) size += Integer.BYTES + node.getCity().getBytes(StandardCharsets.UTF_8).length; //Int is for reading amount of bytes
            else size += Integer.BYTES; //Space for "-1"
            if (node.getHouseNumber() != null) size += Integer.BYTES + node.getHouseNumber().getBytes(StandardCharsets.UTF_8).length;
            else size += Integer.BYTES; //Space for "-1"
            size += Short.BYTES; //Postcode
            if (node.getStreet() != null) size += Integer.BYTES + node.getStreet().getBytes(StandardCharsets.UTF_8).length;
            else size += Integer.BYTES; //Space for "-1"
        }
        return size;
    }

    /// Computes how much space is needed to be allocated in the chunk
    private long computeRoadChunkSize(List<Road> roads) {
        long size = 0;
        for (Road road : roads) {
            size += Integer.BYTES; //Length of road.getNodes
            size += road.getNodes().size() * Long.BYTES; size += 4; //foot, bicycle, isDriveable, oneway
            size += Integer.BYTES; //MaxSpeed
            if (road.getType() != null) size += Integer.BYTES + road.getType().getBytes(StandardCharsets.UTF_8).length; //RoadType
            else size += Integer.BYTES; //Space for "-1"
            if (road.getRoadName() != null) size += Integer.BYTES + road.getRoadName().getBytes(StandardCharsets.UTF_8).length; //RoadName
            else size += Integer.BYTES; //Space for "-1"
        }
        return size;
    }

    /// Computes how much space is needed to be allocated in the chunk
    private long computePolygonChunkSize(List<Polygon> polygons) {
        long size = 0;
        for (Polygon polygon : polygons) {
            size += Integer.BYTES; //Amount of xPoint floats
            size += Float.BYTES*polygon.getXPoints().length; //Makes space for all x points

            size += Integer.BYTES; //Amount of yPoint floats
            size += Float.BYTES*polygon.getYPoints().length; //Makes space for all y points

            if (polygon.getType() != null) size += Integer.BYTES + polygon.getType().getBytes(StandardCharsets.UTF_8).length;
            else size += Integer.BYTES; //space for "-1"
        }
        return size;
    }

    /// Creates a POI and stores it into its given Tile
    public POI createPOI(float localX, float localY, String name) {
        Tile tile = tilegrid.getTileFromXY(localX, localY);
        if (tile == null) return null; //No tile within point
        POI POI = new POI(localX, localY, name, tile);
        tile.addPOI(POI);
        return POI;
    }

    /// Fjern en given POI fra dens Tile
    public void removePOI(POI poi){
        Tile tile = tilegrid.getTileFromXY(poi.getX(), poi.getY());
        tile.getPOIs().remove(poi);

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
        parser.getNodes().valueCollection().removeIf(node -> node.getEdges().isEmpty() && !hasFullAddress(node)); //IMPORTANT: Removes nodes if they dont have any edges, since they now are useless
        System.gc();
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
         for (Polygon polygon : parser.getPolygons()) {
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
    private float[] getMinMaxCoords(Collection<Node> nodes) {
        float[] minMaxCoords = new float[4];
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        //Loops through each node and gets the minimum and maximum node
        for (Node node : nodes) {
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

    /// Inserts all streets and cities of the complex nodes to Tries
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

    /// Returns a list of city-nodes that are correlated with the given {@code prefix} from the trie
    public List<Node> getCitiesFromPrefix(String prefix) {
        return trieCity.keysWithPrefix(prefix);
    }

    /// Returns a list of street-nodes that are correlated with the given {@code prefix} from the trie
    public List<Node> getStreetsFromPrefix(String prefix) {
        return trieStreet.keysWithPrefix(prefix);
    }

    ///Checks whether the given {@code node} has a full address
    public boolean hasFullAddress(Node node) {
        return node.getCity() != null && node.getHouseNumber() != null && node.getPostcode() != 0 && node.getStreet() != null;
    }

    ///Sets the type of search algorithm that will be used. <br> true = Quickest Route <br> false = Shortest Route
    public void setSearchType(boolean quickestRoute) {
        Tile[][] grid = tilegrid.getGrid();
        for (int x = 0; x < numberOfTilesX; x++) {
            for (int y = 0; y < numberOfTilesY; y++) {
                for (Road road : grid[x][y].getRoads()) {
                    road.changeWeight(quickestRoute);
                }
            }
        }
        search.changeRouteSettings(quickestRoute);
    }
    //endregion

    //region Getters and setters
    public File getFile() { return file; }
    public Tilegrid getTilegrid() { return tilegrid; }
    public List<Road> getLatestRoute() { return latestRoute; }
    public void setLatestRoute(List<Road> route) { latestRoute = route; }
    //endregion
}