import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoadTest {
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
        Road way = new Road(nodes, false, false, 10, "Residential");
        assertEquals(way.getLines().size(), 2);
    }
}
