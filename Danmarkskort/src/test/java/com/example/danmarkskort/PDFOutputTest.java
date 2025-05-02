package com.example.danmarkskort;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PDFOutputTest {

    @Test
    public void createValidPdfTest() {
        Node node_1 = new Node(1, 1);
        Node node_2 = new Node(1, 2);
        Node node_3 = new Node(2, 2);

        List<Node> nodes_1 = List.of(node_1, node_2);
        List<Node> nodes_2 = List.of(node_2, node_3);

        Road road_1 = new Road(nodes_1, true, true, true, "primary", "Road 1");
        Road road_3 = new Road(nodes_1, true, true, true, "primary", "Road 1");
        Road road_2 = new Road(nodes_2, true, true, true, "primary", "Road 2");

        List<Road> route = List.of(road_1, road_3, road_2);

        try {
            PDFOutput.generateRoute(route, true);
        } catch (Exception e) {
            System.out.println(e.getClass() +" --> "+ e.getMessage());
        }

        File file = new File("output/Road-1-to-Road-2.pdf");
        assertTrue(file.exists());
    }

    @Test
    public void createInvalidPdfTest() {
        List<Node> nodes_1 = new ArrayList<>();
        nodes_1.add(new Node(1, 1));
        nodes_1.add(new Node(1, 2));

        List<Node> nodes_2 = new ArrayList<>();
        nodes_2.add(new Node(1, 2));
        nodes_2.add(new Node(2, 2));

        Road road_1 = new Road(nodes_1, true, true, true, "primary", "");
        Road road_2 = new Road(nodes_2, true, true, true, "primary", "Road 2");
        Road road_3 = new Road(nodes_2, true, true, true, "primary", "");

        List<Road> route = List.of(road_1, road_2, road_3);

        assertThrows(RuntimeException.class, () ->
            PDFOutput.generateRoute(route, false)
        );

        File file = new File("output/Nameless-place-to-nameless-place.pdf");
        assertTrue(file.exists());
        assertTrue(file.delete());
    }
}
