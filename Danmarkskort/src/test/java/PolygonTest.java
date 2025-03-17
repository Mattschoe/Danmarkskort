import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolygonTest {
    HashSet<Node> nodes;

    @BeforeEach
    public void setup() {
        nodes = new HashSet<>();
    }

    /**
     * Test if it correctly makes the correct amount of lines between nodes
     */
    @Test
    public void createLinesTest() {
        nodes.add(new Node(5, 10));
        nodes.add(new Node(5, 8));
        nodes.add(new Node(6, 9));
        //Polygon polygon = new Polygon(nodes);
        //assertEquals(polygon.getLines().size(), 2);
    }

}
