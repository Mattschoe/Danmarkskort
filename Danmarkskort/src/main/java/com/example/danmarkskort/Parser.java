package com.example.danmarkskort;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Polygon;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Parser implements Serializable {
    //region fields
    @Serial private static final long serialVersionUID = 8838055424703291984L;
    Map<Long, Node> id2Node; //map for storing a Node and the id used to refer to it
    Map<Long, Road> id2Road;
    Map<Long, Polygon> id2Polygon;
    File file; //The file that's loaded in
    double[] bounds; //OSM-filens bounds, dvs. de længst væk koordinater hvor noget tegnes
    //endregion

    /**
     * Checks what filetype the filepath parameter is and calls the appropriate method
     * @param file the file that needs to be processed.
     */
    public Parser(File file) throws NullPointerException, IOException, XMLStreamException, FactoryConfigurationError {
        this.file = file;
        id2Node = new HashMap<>();
        id2Road = new HashMap<>();
        id2Polygon = new HashMap<>();
        bounds = new double[4];

        String filename = getFileName();
        //Switch case with what filetype the file is and call the appropriate method:
        if (filename.endsWith(".zip")) {
            parseZIP(filename);
        } else if (filename.endsWith(".osm")) {
            parseOSM(file);
        }
    }

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
                       // fos.flush(); // Ensure all data is written before closing
                    }
                    zipInputStream.closeEntry();
                   // System.out.println("Extracted file path: " + extractedFile.getAbsolutePath());
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
        XMLStreamReader input = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //ny XMLStreamReader
        //Gennemgår hver tag og parser de tags vi bruger
        while (input.hasNext()) {
            int nextTag = input.next();
            if (nextTag == XMLStreamConstants.START_ELEMENT) {
                String tagName = input.getLocalName();

                //End of OSM
                if (tagName.equals("relation")) return;
                if (tagName.equals("bounds")) parseBounds(input);
                if (tagName.equals("node")) {
                    try {
                        parseNode(input);
                    } catch (Exception e) {
                        System.out.println("Failed creating Node! with input: " + input);
                    }
                } else if (tagName.equals("way")) {
                    try {
                        parseWay(input);
                    } catch (Exception e) {
                        System.out.println("Failed creating Way! with input: " + input);
                    }
                }
            }
        }
    }

    ///Saves the OSM-file's bounds-coordinates (so that View can zoom in to these on startup)
    private void parseBounds(XMLStreamReader input) {
        bounds[0] = Double.parseDouble(input.getAttributeValue(0)); //Min. latitude
        bounds[1] = Double.parseDouble(input.getAttributeValue(1)); //Min. longitude
        bounds[2] = Double.parseDouble(input.getAttributeValue(2)); //Max. latitude
        bounds[3] = Double.parseDouble(input.getAttributeValue(3)); //Max. longitude
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

                    //Makes sure that it doesn't point towards a null Node
                    if (id2Node.get(nodeReference) == null) break;

                    //If same node is referenced twice, parses 'Way' as 'Polygon'
                    if (nodesInWay.contains(id2Node.get(nodeReference))) {
                        nodesInWay.add(id2Node.get(nodeReference));
                        id2Polygon.put(wayID, parsePolygon(input, nodesInWay));
                        return;
                    } else {
                        nodesInWay.add(id2Node.get(nodeReference)); //Adding node to currently looked at nodes
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
                String key = input.getAttributeValue(null, "k"); //for fat i "k" attribute som fx "maxSpeed"
                String value = input.getAttributeValue(null, "v"); // for fat i "v" attribute som fx 30 (hvis det er maxSpeed)
                if (key == null || value == null) continue; //Sørger lige for at hvis der ikke er nogle k or v at vi skipper den
                switch (key) {
                    case "landuse", "leisure", "natural":
                        return new Polygon(nodesInPolygon, value);
                    case "amenity", "building", "surface":
                        return new Polygon(nodesInPolygon, key);
                    case "place":
                        if(value.equals("island")) { return new Polygon(nodesInPolygon, value); }
                        break;
                }
            }
        }
        return new Polygon(nodesInPolygon, null);
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
        int maxSpeed = 0;
        String roadType = "";
        boolean hasMaxSpeed = false;
        //endregion

        //Loops through tags and saves them
        int nextInput = firstTag;
        while (input.hasNext()) {
            //End of Road
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("way")) {
                break;
            }

            //Tries and saves the important tags
            if (nextInput == XMLStreamConstants.START_ELEMENT && input.getLocalName().equals("tag"))  {
                String key = input.getAttributeValue(null, "k"); //for fat i "k" attribute som fx "maxSpeed"
                String value = input.getAttributeValue(null, "v"); // for fat i "v" attribute som fx 30 (hvis det er maxSpeed)
                if (key == null || value == null) continue; //Sørger lige for at hvis der ikke er nogle k or v at vi skipper den
                if (key.equals("highway") || key.equals("natural")) {
                    roadType = value;
                } else if (key.equals("maxspeed")) {
                    maxSpeed = Integer.parseInt(value);
                    hasMaxSpeed = true;
                } else if (key.equals("bicycle")) {
                    bicycle = value.equals("true");
                } else if (key.equals("foot")) {
                    foot = value.equals("yes");
                } else if (key.equals("route")) {
                    roadType = key;
                }
            }
            nextInput = input.next(); //Moves on to the next "tag" element
        }

        //Instansierer en ny Road en road og tager stilling til om den har en maxSpeed eller ej.
        Road road;
        if (hasMaxSpeed){
            road = new Road(nodes, foot, bicycle, maxSpeed, roadType);
        } else {
            road = new Road(nodes, foot, bicycle, roadType);
        }
        return road;
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

        int nextInput = input.next();
        //If simple node, saves it and returns
        if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("node")) {
            id2Node.put(id, new Node(lat, lon)); //Instansierer new node (node containing no child-elements)
            return;
        }

        //Complex node
        String city = null;
        String houseNumber = null;
        int postcode = 0;
        String street = null;
        while (input.hasNext()) {
            //End of Road
            if (nextInput == XMLStreamConstants.END_ELEMENT && input.getLocalName().equals("node")) {
                break;
            }

            if (nextInput == XMLStreamConstants.START_ELEMENT && input.getLocalName().equals("tag")) {
                String key = input.getAttributeValue(null, "k");
                String value = input.getAttributeValue(null, "v");
                if (key == null || value == null) continue;
                if (key.equals("addr:city")) {
                    city = value;
                } else if (key.equals("addr:houseNumber")) {
                    houseNumber = value;
                } else if (key.equals("addr:postcode")) {
                    postcode = Integer.parseInt(value);
                } else if (key.equals("addr:street")) {
                    street = value;
                }
            }
            nextInput = input.next();
        }

        //Creates a complex 'Node' unless it doesn't have any of the elements of a complex 'Node', then it just makes a simple one (Mayb change later)
        if (city == null && houseNumber == null && postcode == 0 && street == null) {
            id2Node.put(id, new Node(lat, lon)); //Instansierer new node (node containing no child-elements)
        } else {
            id2Node.put(id, new Node(lat, lon, city, houseNumber, postcode, street));
        }
    }

    //GETTERS AND SETTERS
    public String getFileName() { return file.getName(); }
    public File getFile() { return file; }
    public Map<Long, Node> getNodes() { return id2Node; }
    public Map<Long, Road> getRoads() { return id2Road; }
    public Map<Long, Polygon> getPolygons() { return id2Polygon; }
    public double[] getBounds() { return bounds; }
}