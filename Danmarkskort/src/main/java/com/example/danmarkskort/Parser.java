package com.example.danmarkskort;

import com.example.danmarkskort.Exceptions.MapObjectOutOfBoundsException;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import gnu.trove.map.hash.TLongObjectHashMap;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Parser implements Serializable {
    @Serial private static final long serialVersionUID = 8838055424703291984L;

    //region Fields
    private transient TLongObjectHashMap<Node> id2Node; //map for storing a Node and the id used to refer to it
    private transient TLongObjectHashMap<Road> id2Road;
    private transient TLongObjectHashMap<Polygon> id2Polygon;
    private transient Set<Road> roads; //Used when loading standard file
    private transient Set<Polygon> polygons; //Used when loading standard file
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
        id2Road = new TLongObjectHashMap<>(2_214_235);
        id2Polygon = new TLongObjectHashMap<>(6_168_995);
        bounds = new double[4];

        failedWays = 0; failedNodes = 0; failedRelations = 0; outOfBoundsNodes = 0;

        parseOSM(file);

        System.out.println("Finished parsing file! ("+ failedNodes +" node(s), "+ failedWays +" way(s) and "+ failedRelations +" relation(s) failed, and "+ outOfBoundsNodes +" node(s) are out of bounds)");
    }
    //endregion

    //region Methods
    /**
     * Parses an .osm-file depending on the value of the start-tags encountered when the file is read line-by-line.
     * @param file the file to be parsed
     * @throws IOException if file isn't found
     * @throws XMLStreamException if an error with the reader occurs
     */
    public void parseOSM(File file) throws IOException, XMLStreamException {
        setBounds(); //This has to be called first so we can check if nodes are in DKK

        XMLStreamReader input;
        if (file.getAbsolutePath().endsWith(".zip")) {
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
            zipStream.getNextEntry();
            input = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(zipStream));
        }
        else {
            input = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //ny XMLStreamReader
        }

        //Gennemgår hver tag og parser de tags vi bruger
        while (input.hasNext()) {
            int nextTag = input.next();
            if (nextTag == XMLStreamConstants.START_ELEMENT) {
                String tagName = input.getLocalName();

                //End of OSM
                if (tagName.equals("node")) {
                    try { parseNode(input); } catch (MapObjectOutOfBoundsException e) {
                        outOfBoundsNodes++;
                    } catch (Exception e) {
                        failedNodes++;
                    }
                } else if (tagName.equals("way")) {
                    try { parseWay(input); } catch (Exception e) {failedWays++;
                    }
                } else if (tagName.equals("relation")) {
                    try { parseRelation(input); } catch (Exception e) {
                        failedRelations++;
                    }
                }
            }
        }
        //Counts references and splits nodes
        countNodeReferences();
        splitRoads();
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
            Node complexNode = new Node(lat, lon, city, houseNumber, postcode, street);
            id2Node.put(id, complexNode);
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
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("relation")) break;

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
                    if (key.equals("amenity") || key.equals("building") || key.equals("surface")) {
                        type = key;
                    }
                    else if (key.equals("landuse") || key.equals("leisure") || key.equals("natural") || key.equals("route")) {
                        type = val;
                    }
                }
            }
        }

        for (long memberID : members) {
            Polygon polygon = id2Polygon.get(memberID);
            Road road = id2Road.get(memberID);
            if (polygon != null && polygon.getType().isEmpty()) polygon.setType(type);
            else if (road != null && road.getType().isEmpty()) road.setType(type);
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
        boolean isCycle = false; //Is true if the endNode and startNode is equal

        //Runs through every node and tag contained in that way
        while (input.hasNext()) {
            int nextInput = input.next();

            //Hvis det er en node gemmer vi den
            if (nextInput == XMLStreamConstants.START_ELEMENT) {
                String inputName = input.getLocalName();
                if (inputName.equals("nd")) {
                    long nodeReference = Long.parseLong(input.getAttributeValue(null, "ref"));
                    Node node = id2Node.get(nodeReference);

                    //Makes sure that it doesn't point towards a null Node, then adds it to the list of currently looked at nodes
                    if (node != null) nodesInWay.add(node);
                } else if (inputName.equals("tag")) {
                    if (nodesInWay.getFirst().equals(nodesInWay.getLast())) isCycle = true; //If last and first node is the same we have a cycle
                    
                    parseTags(wayID, input, nextInput, nodesInWay, isCycle); //End of nodes, so we start parsing tags (if applicable), otherwise this method moves on to creating the object
                    return; //Returns out of method so we don't keep looping through OSM file
                }
            } else if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) {
                //If no tags we first check if it's a cycle and simple parse it directly to Polygon or road
                if (nodesInWay.getFirst().equals(nodesInWay.getLast())) isCycle = true;
                if (isCycle) id2Polygon.put(wayID, parsePolygon(nodesInWay, new HashMap<>()));
                else id2Road.put(wayID, parseRoad(nodesInWay, new HashMap<>()));
                return;
            }
        }
    }

    /**
     * Reads all tags in Way. After parsing we check if the Way is a Road or a Polygon, and then calls the parser for the object.
     * @param wayID the ID of the Way
     * @param input the XMLStreamReader that's currently hovering on the first "tag" tag
     * @param firstTag the int of the first tag (to avoid skipping it)
     * @param nodesInWay the nodes associated with the way
     * @throws XMLStreamException if error with streaming
     */
    private void parseTags(long wayID, XMLStreamReader input, int firstTag, List<Node> nodesInWay, boolean isCycle) throws XMLStreamException {
        if (nodesInWay.size() == 1) {
            throw new IllegalArgumentException("Only 1 node in way");
        }
        boolean road = false, building = false;

        Map<String, String> tagsKeyToValue = new HashMap<>();
        int nextInput = firstTag;
        while (input.hasNext()) {
            //No more tags so we parse Polygon or road
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) {
                if (road) id2Road.put(wayID, parseRoad(nodesInWay, tagsKeyToValue));
                else if (building) id2Polygon.put(wayID, parsePolygon(nodesInWay, tagsKeyToValue));
                else if (isCycle) id2Polygon.put(wayID, parsePolygon(nodesInWay, tagsKeyToValue)); //If it's a cycle with no "highway" tag, it's very likely a building
                else id2Road.put(wayID, parseRoad(nodesInWay, tagsKeyToValue)); //If we don't register if building or road, we parse as road (Lot of roads don't have "Highway" tags)
                return; //Returns out se we don't keep looping through OSM file
            }
            if (nextInput == XMLStreamConstants.START_ELEMENT) {
                String key = input.getAttributeValue(null, "k");
                String value = input.getAttributeValue(null, "v");
                if (key != null && value != null) {
                    if (key.equals("highway")) road = true;
                    else if (key.equals("building")) building = true;
                    tagsKeyToValue.put(key, value); //Puts all tags inside
                }
            }
            nextInput = input.next();
        }
    }

    /**
     * Parses a {@link Polygon} a Polygon is a subset of way. then returns it. Method is called in {@link #parseTags(long, XMLStreamReader, int, List, boolean)}
     * @param nodesInPolygon the nodes related to the to-be-parsed Polygon
     * @param tagsInWay the tags related to the Polygon
     * @return Road which should then be stored in the map {@code id2Polygon} for further reference
     */
    private Polygon parsePolygon(List<Node> nodesInPolygon, Map<String, String> tagsInWay) {
        assert nodesInPolygon != null;

        for (String key : tagsInWay.keySet()) {
            String value = tagsInWay.get(key);
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
        return new Polygon(nodesInPolygon, ""); //No type
    }

    /**
     * Parses a {@link Road} and returns it. A road is a subset of Way. Method is called in {@link #parseTags(long, XMLStreamReader, int, List, boolean)}
     * @param nodes the nodes related to the to-be-parsed Road
     * @param tagsInWay the tags that are related to this Road
     * @return Road which should then be stored in the map {@code id2Road} for further reference
     */
    private Road parseRoad(List<Node> nodes, Map<String, String> tagsInWay) {
        //region node parameters
        boolean foot = true;
        boolean bicycle = true;
        boolean drivable = false;
        int maxSpeed = 0;
        String roadType = "";
        String roadName = "";
        boolean hasMaxSpeed = false;
        //endregion

        for (String key : tagsInWay.keySet()) {
            String value = tagsInWay.get(key);
            switch (key) {
                case "highway", "natural", "area:highway" -> {
                    roadType = value;
                    drivable = !value.equals("footway") && !value.equals("bridleway") && !value.equals("steps") && !value.equals("corridor") && !value.equals("path") && !value.equals("cycleway");
                }
                case "maxspeed" -> {
                    maxSpeed = Integer.parseInt(value);
                    hasMaxSpeed = true;
                }
                case "bicycle" -> bicycle = value.equals("true");
                case "foot" -> foot = value.equals("yes");
                case "route" -> roadType = key;
                case "name" -> roadName = value;
            }
        }

        //Instantierer en ny Road en road og tager stilling til om den har en maxSpeed eller ej.
        Road road;
        if (hasMaxSpeed) road = new Road(nodes, foot, bicycle, drivable, maxSpeed, roadType, roadName);
        else road = new Road(nodes, foot, bicycle, drivable, roadType, roadName);
        return road;
    }

    /// Sets the standard bounds to the middle of DK
    private void setBounds() {
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
            currentRoad.add(nodes.getFirst()); //Adds first node to avoid edge-case where start node is an intersection
            for (int i = 1; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                currentRoad.add(node);

                if (node.isIntersection() || i == nodes.size() - 1) {
                    //We hit an intersection, or the end, so we make a road
                    Road newRoad;
                    if (road.hasMaxSpeed()) newRoad = new Road(new ArrayList<>(currentRoad), road.isWalkable(), road.isBicycle(), road.isDriveable(), road.getMaxSpeed(), road.getType(), road.getRoadName());
                    else newRoad = new Road(new ArrayList<>(currentRoad), road.isWalkable(), road.isBicycle(), road.isDriveable(), road.getType(), road.getRoadName());

                    for (Node roadNode : newRoad.getNodes()) {
                        roadNode.addEdge(newRoad);
                    }
                    roads.add(newRoad);

                    //Starts a new segment from the intersection
                    currentRoad.clear();
                    currentRoad.add(node);
                }
            }
            id2Road.remove(ID);
        }
    }

    ///Counts the amount of times a node in {@code id2Roads} is referenced in the OSM data and saves that in each node as an "edge"
    private void countNodeReferences() {
        for (Road road : id2Road.valueCollection()) {
            for (Node node : road.getNodes()) {
                node.addEdge();
            }
        }
    }
    //endregion


    //region GETTERS AND SETTERS
    public File getFile() { return file; }
    public TLongObjectHashMap<Node> getNodes() { return id2Node; }
    public Set<Road> getRoads() { return roads; }
    public Collection<Polygon> getPolygons() { if (polygons == null) return id2Polygon.valueCollection(); return polygons; }
    public void setNodes(TLongObjectHashMap<Node> nodes) { id2Node = nodes; }
    public void setRoads(Set<Road> roads) { this.roads = roads; }
    public void setPolygons(Set<Polygon> polygons) { this.polygons = polygons; }
    public Set<Node> getAddressNodes() {
        Set<Node> addressNodes = new HashSet<>();
        for (Node node : id2Node.valueCollection()) {
            if (node.getCity() == null) continue;
            if (node.getHouseNumber() == null) continue;
            if (node.getStreet() == null) continue;
            else if (node.getPostcode() != 0) addressNodes.add(node);
        }
        return addressNodes;
    }
    //endregion
}