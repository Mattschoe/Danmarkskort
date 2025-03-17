import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

public class LineTest {
    HashSet<Node> nodes;

    @BeforeEach
    public void setup() {
        nodes = new HashSet<>();
    }


    /**
     * Test if the lines are correctly matched up
     */
    @Deprecated
    @Test
    public void checkLinesHaveCorrectNodeXY() {
        //Creates a way that stores the line
        nodes.add(new Node(5, 10));
        nodes.add(new Node(5, 8));
        nodes.add(new Node(5, 9));
        Road way = new Road(nodes, false, false, 10, "Residential");
    }
}
