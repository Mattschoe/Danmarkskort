import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    Parser parser;
    String filename;
    File testFile;
    String[] address;
    List<Node> nodes;

    @BeforeEach
    public void setUp() throws XMLStreamException, IOException, ClassNotFoundException {
        filename = "../Danmarkskort/data/Test1.osm";
        testFile = new File(filename);
        parser = new Parser(testFile);
    }

    // --------------------------------- Test af indlæsning ------------------------------------

    @Test
    public void kanIndlaeseFil() {
        try {
            parser.parseOSM(testFile);
            assertTrue(true);
        } catch (IOException e) {
            fail(e);
        } catch (XMLStreamException e) {
            fail();
        }

    }

    @Test
    public void kanIndlaeseRigtigTypeFil() throws FileNotFoundException, IOException {
        // Kan indlæse OSM-fil
        if (filename.endsWith(".osm") || filename.endsWith(".obj") || filename.endsWith(".txt")) {
           try {
               parser.parseOSM(testFile);
               assertTrue(true);
           } catch (IOException | XMLStreamException e) {
               fail(e);
           }
        } else {
            throw new IOException("Wrong filetype!");
        }
    }



    @Test
    public void kanUnzippeFil() throws XMLStreamException, IOException, ClassNotFoundException {
        parser.parseZIP("../Danmarkskort/data/Test1.zip");

        Parser parser2 = new Parser(testFile);

        assertEquals(parser.getRoads().keySet(), parser2.getRoads().keySet());

    }









    // --------------------------------- Test af parsing ------------------------------------

    @Test
    public void kanAflaeseOSMFil() throws XMLStreamException, IOException {
        //Hvis test-filens content er lig den indlæste fil
        parser.parseOSM(testFile);

        if (parser.getFile().equals(testFile)) {
            assertTrue(true);
        } else {
            fail();
        }
    }

    @Test
    public void kanParseBounds() throws XMLStreamException, IOException {
        //Kan aflæse maxlat, minlat, maxlon og minlon

        parser.parseOSM(testFile);
        double[] bounds = parser.getBounds();

        if (bounds[0] == 55.6804000 && bounds[1] == 55.6631000 && bounds[2] == 12.6031000 && bounds[3] == 12.5730000) {
            assertTrue(true);
        }
        fail();
    }

    @Test
    public void kanParseNodes() throws XMLStreamException, IOException {
        //Kan aflæse lan, lon id korrekt
        parser.parseOSM(new File("data/test1.2.osm"));
        Map<Long, Node> Id2node = parser.getNodes();
        int numberOfCorrectNodes = 0;

        for (long id : Id2node.keySet()) {
            if (id == 125403) {
                if (!(Id2node.get(id).getX() == 0.56 * 12.5871796 && Id2node.get(id).getY() == -55.6753313)) {
                    numberOfCorrectNodes++;
                }
            } else if (id == 706639) {
                if (!(Id2node.get(id).getX() == 0.56 * 12.5790260 && Id2node.get(id).getY() == -55.6799858)) {
                    numberOfCorrectNodes++;
                }
            } else if (id == 1418594827) {
                if (!(Id2node.get(id).getX() == 0.56 * 12.5877190 && Id2node.get(id).getY() == -55.6645970)) {
                    numberOfCorrectNodes++;
                }
            } else if (id == 1520199040) {
                if (!(Id2node.get(id).getX() == 0.56 * 12.5849000 && Id2node.get(id).getY() == -55.6801210)) {
                    numberOfCorrectNodes++;
                }
            }
        }

            if (numberOfCorrectNodes == 4) {
                assertTrue(true);
            } else {
                fail();
            }

        }


    @Test
    public void kanParseNodesMedAdresser() throws XMLStreamException, IOException {
        parser.parseOSM(testFile);
        Map<Long, Node> Id2node = parser.getNodes();

        int numberOfCorrectAddresses = 0;

        for (long id : Id2node.keySet()) {

            if (id == 1418594827) {
                address = Id2node.get(id).getAddress();
                if (address[0].equals("København S")) {
                    numberOfCorrectAddresses++;
                }
                if (address[1].equals("83")) {
                    numberOfCorrectAddresses++;
                }
                if (address[2].equals("2300")) {
                    numberOfCorrectAddresses++;
                }
                if (address[3].equals("Njalsgade")) {
                    numberOfCorrectAddresses++;
                }
            } else if (id == 1520199040) {
                address = Id2node.get(1520199040L).getAddress();
                if (address[0].equals("København K")) {
                    numberOfCorrectAddresses++;
                }
                if (address[1].equals("21A")) {
                    numberOfCorrectAddresses++;
                }
                if (address[2].equals("1050")) {
                    numberOfCorrectAddresses++;
                }
                if (address[3].equals("Kongens Nytorv")) {
                    numberOfCorrectAddresses++;
                }

            }
        }
        assert(numberOfCorrectAddresses == 8);
    }



    @Test
    public void kanParseRoads() throws XMLStreamException, IOException {

        parser.parseOSM(testFile);
        Map<Long, Road> Id2road = parser.getRoads();
        Map<Long, Node> Id2node = parser.getNodes();

        int numberOfCorrectRoads = 0;

        for (long id : Id2road.keySet()) {

            if (id == 1794682) {
                nodes = Id2road.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(268694131L) == node || Id2node.get(314658577L) == node) {
                        numberOfCorrectRoads++;
                    }
                }
            } else if (id == 1881367) {
                nodes = Id2road.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(8088617L) == node || Id2node.get(8088618L) == node || Id2node.get(20908335L) == node || Id2node.get(2584405866L) == node || Id2node.get(8088619L) == node || Id2node.get(8088620L) == node) {
                        numberOfCorrectRoads++;
                    }
                }
            } else if (id == 1880991) {
                nodes = Id2road.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(8085618L) == node || Id2node.get(3518714170L) == node || Id2node.get(3207917500L) == node || Id2node.get(8085619L) == node) {
                        numberOfCorrectRoads++;
                    }

                    if (Id2road.get(id).getRoadType().equals("highway")) {
                        numberOfCorrectRoads++;
                    }

                        if (Id2road.get(id).getMaxSpeed() == 50) {
                            numberOfCorrectRoads++;
                        }


                }
            } else if (id == 1881915) {
                nodes = Id2road.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(8099298L) == node || Id2node.get(2920731844L) == node || Id2node.get(7302090L) == node || Id2node.get(8115539L) == node) {
                        numberOfCorrectRoads++;
                    }

                    if (Id2road.get(id).getRoadType().equals("highway")) {
                        numberOfCorrectRoads++;
                    }


                    if (Id2road.get(id).getMaxSpeed() == 40) { //Mulighed for at en road ikke har en maxspeed?
                            numberOfCorrectRoads++;
                    }


                }
            }

            if (numberOfCorrectRoads == 20) {
                assertTrue(true);
            } else {
                fail();
            }

        }

    }

    @Test
    public void kanParsePolygoner() throws XMLStreamException, IOException {

        parser.parseOSM(testFile);
        Map<Long, Road> Id2road = parser.getRoads();
        Map<Long, Node> Id2node = parser.getNodes();
        Map<Long, Polygon> Id2polygon = parser.getPolygons();

        int numberOfCorrectPolygons = 0;

        for (long id : Id2polygon.keySet()) {

            if (id == 25466133) {
                nodes = Id2polygon.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(564838218L) == node ||
                            Id2node.get(564838219L) == node ||
                            Id2node.get(564828504L) == node ||
                            Id2node.get(3092851662L) == node ||
                            Id2node.get(3092851661L) == node ||
                            Id2node.get(3092851660L) == node ||
                            Id2node.get(564828457L) == node ||
                            Id2node.get(564838214L) == node ||
                            Id2node.get(1859585501L) == node ||
                            Id2node.get(1859585498L) == node ||
                            Id2node.get(3093671661L) == node ||
                            Id2node.get(3093671660L) == node ||
                            Id2node.get(3093671664L) == node ||
                            Id2node.get(3093671665L) == node ||
                            Id2node.get(277497309L) == node ||
                            Id2node.get(277497310L) == node ||
                            Id2node.get(277497303L) == node ||
                            Id2node.get(277497304L) == node ||
                            Id2node.get(277497305L) == node ||
                            Id2node.get(277497306L) == node ||
                            Id2node.get(3093671666L) == node ||
                            Id2node.get(3093671667L) == node ||
                            Id2node.get(3093671663L) == node ||
                            Id2node.get(3093671662L) == node ||
                            Id2node.get(564838220L) == node) {
                        numberOfCorrectPolygons++;
                    }

                    if (Id2polygon.get(id).getType().equals("building")) {
                        numberOfCorrectPolygons++;
                    }

                }
            } else if (id == 25520717) {
                nodes = Id2polygon.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(278114496L) == node || Id2node.get(278114497L) == node || Id2node.get(278114498L) == node || Id2node.get(278114499L) == node) {
                        numberOfCorrectPolygons++;
                    }

                    if (Id2polygon.get(id).getType().equals("building")) {
                        numberOfCorrectPolygons++;
                    }

                }
            } else if (id == 26084902) {
                nodes = Id2polygon.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(285455225L) == node ||
                            Id2node.get(527857414L) == node ||
                            Id2node.get(527857444L) == node ||
                            Id2node.get(527857437L) == node ||
                            Id2node.get(527857440L) == node ||
                            Id2node.get(285455250L) == node ||
                            Id2node.get(527857442L) == node ||
                            Id2node.get(527857438L) == node ||
                            Id2node.get(527857422L) == node ||
                            Id2node.get(527857424L) == node ||
                            Id2node.get(285455242L) == node ||
                            Id2node.get(527857423L) == node ||
                            Id2node.get(527857413L) == node ||
                            Id2node.get(527857412L) == node ||
                            Id2node.get(527857418L) == node ||
                            Id2node.get(285455233L) == node ||
                            Id2node.get(527857417L) == node ||
                            Id2node.get(527857411L) == node ||
                            Id2node.get(527857410L) == node ||
                            Id2node.get(527857415L) == node) {
                        numberOfCorrectPolygons++;
                    }

                    if (Id2polygon.get(id).getType().equals("natural")) {
                        numberOfCorrectPolygons++;
                    }

                }
            } else if (id == 277422094) {
                nodes = Id2polygon.get(id).getNodes();
                for (Node node : nodes) {
                    if (Id2node.get(2819167793L) == node || Id2node.get(2819167794L) == node || Id2node.get(2819167795L) == node) {
                        numberOfCorrectPolygons++;
                    }

                    if (Id2polygon.get(id).getType().equals("natural")) {
                        numberOfCorrectPolygons++;
                    }

                    }
                }

            }

            if (numberOfCorrectPolygons == 59) {
                assertTrue(true);
            } else {
                fail();
            }

        }
    }



