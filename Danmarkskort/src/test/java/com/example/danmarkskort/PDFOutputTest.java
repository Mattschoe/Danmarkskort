package com.example.danmarkskort;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PDFOutputTest {

    /// Tests that PDFOutput's generateRoute-function creates a PDF given valid input
    @Test public void createValidPdfTest() {
        Node node_1 = new Node(1, 1);
        Node node_2 = new Node(1, 2);
        Node node_3 = new Node(2, 2);

        List<Node> nodes_1 = List.of(node_1, node_2);
        List<Node> nodes_2 = List.of(node_2, node_3);

        Road road_1 = new Road(nodes_1, true, true, true, "primary", "Road 1");
        Road road_3 = new Road(nodes_1, true, true, true, "primary", "Road 3");
        Road road_2 = new Road(nodes_2, true, true, true, "primary", "Road 2");

        List<Road> route = List.of(road_2, road_1, road_3);

        try {
            PDFOutput.generateRoute(route, false);
        } catch (Exception e) {
            System.out.println(e +": "+ e.getMessage());
        }

        File file = new File("output/Road-1-to-Road-2.pdf");
        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    /// Tests that PDFOutput's generateRoute-function throws an exception on invalid input
    @Test public void createInvalidPdfTest() {
        List<Node> nodes_1 = new ArrayList<>();
        nodes_1.add(new Node(50, 50));
        nodes_1.add(new Node(65, 17));

        List<Node> nodes_2 = new ArrayList<>();
        nodes_2.add(new Node(30, 60));
        nodes_2.add(new Node(30, 89));

        Road road_1 = new Road(nodes_1, true, true, true, "primary", "");
        Road road_2 = new Road(nodes_2, true, true, true, "primary", "   ");

        List<Road> route = List.of(road_1, road_2);

        assertThrows(RuntimeException.class, () ->
            PDFOutput.generateRoute(route, false)
        );

        File file = new File("output/Nameless-place-to-nameless-place.pdf");
        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    /// Tests that right/left turns are processed correctly in all four different vector-arrangements
    @Test public void turnWorksInAllFourCasesTest() {
        Node node_1 = new Node(1, 1);
        Node node_2 = new Node(1, 2);
        Node node_3 = new Node(2, 2);

        assertDoesNotThrow(() -> testTurn(node_1, node_2, node_2, node_3));
        assertDoesNotThrow(() -> testTurn(node_1, node_2, node_3, node_2));
        assertDoesNotThrow(() -> testTurn(node_2, node_1, node_3, node_2));
        assertDoesNotThrow(() -> testTurn(node_2, node_1, node_2, node_3));
    }

    /// Function tests whether a left turn is correctly assessed given an arrangement of nodes
    private static void testTurn(Node node_1, Node node_2, Node node_3, Node node_4) throws DocumentException, FileNotFoundException {
        List<Node> nodes_1 = List.of(node_1, node_2);
        List<Node> nodes_2 = List.of(node_3, node_4);

        Road road_1 = new Road(nodes_1, true, true, true, "primary", "Road 1");
        Road road_2 = new Road(nodes_2, true, true, true, "primary", "Road 2");

        List<Road> route = List.of(road_2, road_1);

        String turn = PDFOutput.generateRoute(route, false);
        assertEquals("Turn left", turn);
    }
}
