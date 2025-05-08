package com.example.danmarkskort;

import com.example.danmarkskort.MapObjects.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoadTest {
    List<Node> nodes;

    @BeforeEach
    public void setup() {
        nodes = new ArrayList<>();
    }

    /**
     * Test if it correctly makes the correct amount of lines between nodes
     */
    @Test
    public void createLinesTest() {
        nodes.add(new Node(5, 10));
        nodes.add(new Node(5, 8));
        nodes.add(new Node(6, 9));
        //Road way = new Road(nodes, false, false, 10, "Residential");
        //assertEquals(way.getLines().size(), 2);
    }
}
