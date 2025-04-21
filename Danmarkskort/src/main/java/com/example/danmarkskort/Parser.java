package com.example.danmarkskort;

import com.example.danmarkskort.Exceptions.MapObjectOutOfBoundsException;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import gnu.trove.map.hash.TLongObjectHashMap;

public class Parser implements Serializable {
    @Serial private static final long serialVersionUID = 8838055424703291984L;

    //region Fields
    private transient TLongObjectHashMap<Node> id2Node; //map for storing a Node and the id used to refer to it
    private transient TLongObjectHashMap<Road> id2Road;
    private transient TLongObjectHashMap<Polygon> id2Polygon;
    private transient Set<Road> roads;
    private final File file; //The file that's loaded in
    ///\[0] = minLat <br> \[1] = minLong <br> \[2] = maxLat <br> \[3] = maxLong
    private final double[] bounds;

    private int failedWays;
    private int failedRelations;
    private int failedNodes;
    private int outOfBoundsNodes;
    //endregion

    //region Constructor(s)
    /**
     * Checks what filetype the filepath parameter is and calls the appropriate method
     * @param file the file that needs to be processed.
     */
    public Parser(File file) throws NullPointerException, IOException, XMLStreamException, FactoryConfigurationError {
        this.file = file;
        id2Node = new TLongObjectHashMap<>(66_289_558);
        //id2Node = new HashMap<>(49_721_049);
        id2Road = new TLongObjectHashMap<>(2_214_235);
        id2Polygon = new TLongObjectHashMap<>(6_168_995);
        bounds = new double[4];

        failedWays = 0; failedNodes = 0; failedRelations = 0; outOfBoundsNodes = 0;

        String filename = getFile().getName();
        //Switch case with what filetype the file is and call the appropriate method:
        if (filename.endsWith(".zip")) {
            parseZIP(filename);
        } else if (filename.endsWith(".osm")) {
            parseOSM(file);
        }
        System.out.println("Finished parsing file. With: " + failedNodes + " nodes | " + failedWays + " ways | " + failedRelations + " relations, that failed! And with" + outOfBoundsNodes + " nodes out of bounds!");
    }
    //endregion

    //region Methods
    /**
     * Creates zipInputStream to read .osm.ZIP-file. Gets ZIP-entry in String format to use in
     * parseOSM().
     * @param filename the name of the input file
     * @throws IOException if the file isn't found
     */
    public void parseZIP(String filename) throws IOException, XMLStreamException {
        File zipFile = new File(filename);
        File extractedFile = null;

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".osm")) {
                    extractedFile = new File(zipFile.getParent(), entry.getName());
                    try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                    }
                    zipInputStream.closeEntry();
                    break;
                }
            }
        }

        if (extractedFile == null) {
            throw new FileNotFoundException("No .osm file found in the ZIP archive.");
        }

        parseOSM(extractedFile); // Now pass the correct extracted file

    }

    /**
     * Parses an .osm-file depending on the value of the start-tags encountered when the file is read line-by-line.
     * @param file the file to be parsed
     * @throws IOException if file isn't found
     * @throws XMLStreamException if an error with the reader occurs
     */
    public void parseOSM(File file) throws IOException, XMLStreamException {
        setStandardBounds(); //This has to be called first so we can check if nodes are in DKK
        XMLStreamReader input = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //ny XMLStreamReader
        //Gennemgår hver tag og parser de tags vi bruger
        while (input.hasNext()) {
            int nextTag = input.next();
            if (nextTag == XMLStreamConstants.START_ELEMENT) {
                String tagName = input.getLocalName();

                //End of OSM
                if (tagName.equals("bounds")) parseBounds(input);
                else if (tagName.equals("node")) {
                    try { parseNode(input); } catch (MapObjectOutOfBoundsException e) {
                        outOfBoundsNodes++;
                    } catch (Exception e) {
                        failedNodes++;
                    }
                } else if (tagName.equals("way")) {
                    try { parseWay(input); } catch (Exception e) {
                        failedWays++;
                    }
                } else if (tagName.equals("relation")) {
                    try { parseRelation(input); } catch (Exception e) {
                        failedRelations++;
                    }
                }
            }
        }
        splitRoads();
    }


    /**
     * Saves the OSM-file's bounds-coordinates (so that View can zoom in to these on startup) <br>
     * [0] = minLat <br> [1] = minLong <br> [2] = maxLat <br> [3] = maxLong
     */
    private void parseBounds(XMLStreamReader input) {
        bounds[0] = Double.parseDouble(input.getAttributeValue(0)); //Min. latitude
        bounds[1] = Double.parseDouble(input.getAttributeValue(1)); //Min. longitude
        bounds[2] = Double.parseDouble(input.getAttributeValue(2)); //Max. latitude
        bounds[3] = Double.parseDouble(input.getAttributeValue(3)); //Max. longitude
    }

    /**
     * Parses a {@link Node} from XMLStreamReader.next() and then adds it to id2Node
     * @throws XMLStreamException if there is an error with the {@code XMLStreamReader}
     */
    private void parseNode(XMLStreamReader input) throws XMLStreamException {
        //Saves the guaranteed values
        long id = Long.parseLong(input.getAttributeValue(null, "id"));
        double lat = Double.parseDouble(input.getAttributeValue(null, "lat"));
        double lon = Double.parseDouble(input.getAttributeValue(null, "lon"));
        if (lat < bounds[0] || lat > bounds[2] || lon < bounds[1] || lon > bounds[3]) throw new MapObjectOutOfBoundsException("Node is out of bounds!");

        int nextInput = input.next();
        //If simple node, saves it and returns
        if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("node")) {
            id2Node.put(id, new Node(lat, lon)); //Instansierer new node (node containing no child-elements)
            return;
        }

        //Complex node
        String city = null;
        String houseNumber = null;
        short postcode = 0;
        String street = null;
        while (input.hasNext()) {
            //End of Node
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("node")) {
                break;
            }

            if (nextInput == XMLStreamConstants.START_ELEMENT && input.getLocalName().equals("tag")) {
                String key = input.getAttributeValue(null, "k");
                String value = input.getAttributeValue(null, "v");
                if (key == null || value == null) continue;
                if (key.equals("addr:city")) {
                    city = value;
                } else if (key.equals("addr:housenumber")) {
                    houseNumber = value;
                } else if (key.equals("addr:postcode")) {
                    postcode = Short.parseShort(value);
                } else if (key.equals("addr:street")) {
                    street = value;
                }
            }
            nextInput = input.next();
        }

        //Creates a complex 'Node' unless it doesn't have any of the elements of a complex 'Node', then it just makes a simple one (Mayb change later)
        if (city == null && houseNumber == null && postcode == 0 && street == null) {
            id2Node.put(id, new Node(lat, lon)); //Instantiates a new node (node containing no child-elements)
        } else {
            id2Node.put(id, new Node(lat, lon, city, houseNumber, postcode, street));
        }
    }

    private void parseRelation(XMLStreamReader input) throws XMLStreamException {
        //The relation's "fields"
        List<Long> members = new ArrayList<>();
        String type = "";

        //Runs through every child of the relation until a relation end-element is encountered
        while (input.hasNext()) {
            int nextInput = input.next();

            //Checks whether we've found the end element and terminates the while-loop if so
            if (nextInput == XMLStreamConstants.END_ELEMENT
                    && input.getLocalName().equals("relation")) { break; }

            //Handles the relation's children
            if (nextInput == XMLStreamConstants.START_ELEMENT) {
                //All 'member'-tags are added to the list of the Relation's members
                if (input.getLocalName().equals("member")) {
                    members.add(Long.valueOf(input.getAttributeValue(null, "ref")));
                }
                //For all 'tag'-tags temporarily save key and value, and do certain things in certain cases...
                else if (input.getLocalName().equals("tag")) {
                    String key = input.getAttributeValue(null, "k");
                    String val = input.getAttributeValue(null, "v");
                    if (key == null || val == null) continue; //If k or v are null we skip the element
                    else if (key.equals("amenity") || key.equals("building") || key.equals("surface")) {
                        type = key;
                    }
                    else if (key.equals("landuse") || key.equals("leisure") || key.equals("natural") || key.equals("route")) {
                        type = val;
                    }
                }
            }
        }

        for (long memberID : members) {
            if (id2Polygon.containsKey(memberID)) {
                Polygon member = id2Polygon.get(memberID);
                if (member.getType().isEmpty()) {
                    member.setType(type);
                }
            }
            else if (id2Road.containsKey(memberID)) {
                Road member = id2Road.get(memberID);
                if (member.getType().isEmpty()) member.setType(type);
            }
        }
    }

    /**
     * Decides whether a way is a road or a polygon and calls the appropriate method to instantiate either
     * a road or a polygon.
     * If nd with reference to Node is found, store the Node referenced in HashSet of Nodes.
     * @param input an XMLStreamReader starting from input line found in parseOSM()
     * @throws XMLStreamException if there is an error with the reader
     */
    private void parseWay(XMLStreamReader input) throws XMLStreamException {
        List<Node> nodesInWay = new ArrayList<>();
        long wayID = Long.parseLong(input.getAttributeValue(null, "id"));

        //Runs through every node and tag contained in that way
        while (input.hasNext()) {
            int nextInput = input.next();

            //End of element
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) break;

            //Hvis det er en node gemmer vi den, og evt. parser en Polygon
            if (nextInput == XMLStreamConstants.START_ELEMENT) {
                if (input.getLocalName().equals("nd")) {
                    long nodeReference = Long.parseLong(input.getAttributeValue(null, "ref"));

                    Node node = id2Node.get(nodeReference);

                    //Makes sure that it doesn't point towards a null Node
                    if (node == null) break;

                    //If same node is referenced twice, parses 'Way' as 'Polygon'
                    if (nodesInWay.contains(node)) {
                        nodesInWay.add(node);
                        id2Polygon.put(wayID, parsePolygon(input, nodesInWay));
                        return;
                    } else {
                        nodesInWay.add(node); //Adding node to currently looked at nodes
                    }
                } else if (input.getLocalName().equals("tag")) {
                    //When reaching "tag" elements, we know it isn't a Polygon (no "Node" is mentioned twice), and therefore we parse it as a Road
                    id2Road.put(wayID, parseRoad(input, nextInput, nodesInWay));
                    return;
                }
            }
        }
    }

    /**
     * Parses a {@link Polygon} a Polygon is a subset of way. then returns it.
     * @param input the XMLStreamReader that currently is sitting at the beginning of the to-be-parsed Polygon
     * @param nodesInPolygon the nodes related to the to-be-parsed Polygon
     * @return Road which should then be stored in the map {@code id2Polygon} for further reference
     */
    private Polygon parsePolygon(XMLStreamReader input, List<Node> nodesInPolygon) throws XMLStreamException {
        assert nodesInPolygon != null;

        while (input.hasNext()) {
            //End of tag
            int nextInput = input.next();
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) break;

            if (nextInput == XMLStreamConstants.START_ELEMENT) {
                String key = input.getAttributeValue(null, "k"); //får fat i "k" attribute som fx "maxSpeed"
                String value = input.getAttributeValue(null, "v"); // får fat i "v" attribute som fx 30 (hvis det er maxSpeed)
                if (key == null || value == null) continue; //Sørger lige for at hvis der ikke er nogle k or v at vi skipper den
                switch (key) {
                    case "aeroway", "disused:landuse", "landuse", "leisure", "man_made", "natural", "place":
                        return new Polygon(nodesInPolygon, value);
                    case "amenity", "area:highway", "attraction", "barrier", "boundary", "bridge:support", "building",
                         "fence_type", "highway", "historic", "indoor", "military", "playground", "power",
                         "surface", "tourism", "waterway":
                        return new Polygon(nodesInPolygon, key);
                }
                if (value.equals("Cityringen")) return new Polygon(nodesInPolygon, value); //TODO %% Find en bedre måde at IKKE tegne Cityringen
            }
        }
        return new Polygon(nodesInPolygon, "");
    }

    /**
     * Parses a {@link Road} and returns it. A road is a subset of Way. Method is called in {@link #parseWay(XMLStreamReader)}
     * @param input the XMLStreamReader that currently is sitting at the beginning of the to-be-parsed Road
     * @param firstTag the first tag, discovered in {@link #parseWay(XMLStreamReader)}
     * @param nodes the nodes related to the to-be-parsed Road
     * @return Road which should then be stored in the map {@code id2Road} for further reference
     */
    private Road parseRoad(XMLStreamReader input, int firstTag, List<Node> nodes) throws XMLStreamException {
        //region node parameters
        boolean foot = true;
        boolean bicycle = true;
        boolean drivable = false;
        int maxSpeed = 0;
        String roadType = "";
        String roadName = "";
        boolean hasMaxSpeed = false;
        //endregion

        //Loops through tags and saves them
        int nextInput = firstTag;
        while (input.hasNext()) {
            //End of Road
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) break;

            //Tries and saves the important tags
            if (nextInput == XMLStreamConstants.START_ELEMENT && input.getLocalName().equals("tag"))  {
                String key = input.getAttributeValue(null, "k"); //for fat i "k" attribute som fx "maxSpeed"
                String value = input.getAttributeValue(null, "v"); // for fat i "v" attribute som fx 30 (hvis det er maxSpeed)
                if (key == null || value == null) continue; //Sørger lige for at hvis der ikke er nogle k or v at vi skipper den
                if (key.equals("highway") || key.equals("natural") || key.equals("area:highway")) {     //find ud af typen af highway
                    roadType = value;
                    if (value.equals("footway") || value.equals("bridleway") || value.equals("steps") || value.equals("corridor") || value.equals("path") || value.equals("cycleway")) drivable = false;
                    else drivable = true;
                } else if (key.equals("maxspeed")) {
                    maxSpeed = Integer.parseInt(value);
                    hasMaxSpeed = true;
                } else if (key.equals("bicycle")) {
                    bicycle = value.equals("true");
                } else if (key.equals("foot")) {
                    foot = value.equals("yes");
                } else if (key.equals("route")) {
                    roadType = key;
                } else if (key.equals("name")) {
                    roadName = value;
                }
            }
            nextInput = input.next(); //Moves on to the next "tag" element
        }

        //Instantierer en ny Road en road og tager stilling til om den har en maxSpeed eller ej.
        Road road;
        if (hasMaxSpeed) road = new Road(nodes, foot, bicycle, drivable, maxSpeed, roadType, roadName);
        else road = new Road(nodes, foot, bicycle, drivable, roadType, roadName);
        return road;
    }

    /// Sets the standard bounds to the middle of DK
    private void setStandardBounds() {
        bounds[0] = 54.481528;
        bounds[1] = 7.679673;
        bounds[2] = 57.995290;
        bounds[3] = 15.708697; //Bornholm gør at DK er mega lang :(, once again et giga Bornholm L
    }

    ///Splits all roads into multiple each time there is intersection
    private void splitRoads() {
        roads = new HashSet<>(id2Road.size());
        for (long ID : id2Road.keys()) {
            Road road = id2Road.get(ID);
            List<Node> nodes = road.getNodes();
            List<Node> currentRoad = new ArrayList<>();

            //Runs through every node, checks if intersection, and makes a road on every intersection. Road 'ABCDE' therefore becomes 'ABC' & 'CDE' if 'C' is an intersection
            currentRoad.add(nodes.getFirst()); //Adds first node to avoid edgecase where start node is an intersection
            for (int i = 1; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                currentRoad.add(node);

                if (node.isIntersection() || i == nodes.size() - 1) {
                    //We hit an intersection, or the end, so we make a road
                    if (road.hasMaxSpeed()) roads.add(new Road(new ArrayList<>(currentRoad), road.isWalkable(), road.isBicycle(), road.isDrivable(), road.getMaxSpeed(), road.getType(), road.getRoadName()));
                    else roads.add(new Road(new ArrayList<>(currentRoad), road.isWalkable(), road.isBicycle(), road.isDrivable(), road.getType(), road.getRoadName()));

                    //Starts a new segment from the intersection
                    currentRoad.clear();
                    currentRoad.add(node);
                }
            }
            id2Road.remove(ID);
        }
    }
    //endregion


    //region GETTERS AND SETTERS
    public File getFile() { return file; }
    public TLongObjectHashMap<Node> getNodes() { return id2Node; }
    public Set<Road> getRoads() { return roads; }
    public TLongObjectHashMap<Polygon> getPolygons() { return id2Polygon; }
    public void setNodes(TLongObjectHashMap<Node> nodes) { id2Node = nodes; }
    public void setRoads(TLongObjectHashMap<Road> roads) { id2Road = roads; }
    public void setPolygons(TLongObjectHashMap<Polygon> polygons) { id2Polygon = polygons; }
    public double[] getBounds() { return bounds; }
    //endregion
}