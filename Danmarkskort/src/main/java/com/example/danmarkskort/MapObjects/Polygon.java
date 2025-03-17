package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.*;

public class Polygon implements Serializable {
    //region fields
    private List<Node> nodes;
    private Set<Line> lines;
    private String type; //The type of polygon, fx: "Building", "Coastline", etc.
    //endregion

    /**
     * A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     * @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.nodes = nodes;
        lines = new HashSet<>();
        createLines();
    }

    /**
     * Creates the lines between nodes (Used later for drawing)
     */
    private void createLines() {
        //Tegner en linje fra den første node til den sidste i rækkefølge. Slutter af med at lave en linje mellem den sidste og første
        Node startNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            lines.add(new Line(startNode, nodes.get(i)));
            startNode = nodes.get(i);
        }
        lines.add(new Line(startNode, nodes.getLast()));
    }

    /**
     * Draws the polygon (Building) with the settings given in the {@code graphicsContext}
     * @param graphicsContext the settings/format to tell the method how to draw
     */
    public void drawPolygon(GraphicsContext graphicsContext) {
        for (Line line : lines) {
            line.drawLine(graphicsContext);
        }
    }

    //region getters
    public Set<Line> getLines() {
        return lines;
    }
    public List<Node> getNodes() { return nodes; }
    public String getType() {
        return type;
    }
    //endregion
}
