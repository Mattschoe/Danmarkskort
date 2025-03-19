package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.List;

public class Polygon implements Serializable {
    //region fields
    private final List<Node> nodes;
    private double[] xPoints;
    private double[] yPoints;
    private int nSize;
    private String type = ""; //The type of polygon, fx: "Building", "Coastline", etc.
    //endregion

    /**
     * A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     * @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.nodes = nodes;
        if (type != null) this.type = type;
        createArrays();
    }

    public void createArrays() {
        nodes.add(nodes.getFirst());
        nSize = nodes.size();

        xPoints = new double[nSize];
        yPoints = new double[nSize];

        for (int i = 0; i < nSize; i++) {
            xPoints[i] = nodes.get(i).getX();
            yPoints[i] = nodes.get(i).getY();
        }
    }

    public void drawPolygon(GraphicsContext gc) {
        Color color = switch(type) {
            case "building"  -> Color.DARKGRAY;
          //case "tree_row"  -> Color.GREENYELLOW;
          //case "tree"      -> Color.LIGHTGREEN;
          //case "tee"       -> Color.DARKVIOLET;
            case "water"     -> Color.CORNFLOWERBLUE;
          //case "rock"      -> Color.BLACK;
            case "heath"     -> Color.CHARTREUSE;
          //case "natural"   -> Color.FUCHSIA;
            case "coastline" -> Color.PERU;

            case "forest"            -> Color.GREEN;
            case "industrial"        -> Color.YELLOW;
            case"residential"        -> Color.BURLYWOOD;
            case "brownfield"        -> Color.SADDLEBROWN;
            case "grass"             -> Color.GREENYELLOW;
            case "landuse"           -> Color.DARKVIOLET;
            case "allotments"        -> Color.HOTPINK;
            case "recreation_ground" -> Color.LIGHTCORAL;
            case "construction"      -> Color.TOMATO;
            case "military"          -> Color.SPRINGGREEN;
            case "basin"             -> Color.CYAN;
            case "cemetery"          -> Color.CRIMSON;
            default -> Color.rgb(0, 74, 127, 0.2);
        };
        gc.setStroke(color.darker().darker());
        gc.setFill(color);
        gc.strokePolygon(xPoints, yPoints, nSize);
        gc.fillPolygon(xPoints, yPoints, nSize);
    }

    //region getters
    public List<Node> getNodes() { return nodes; }
    public String getType() { return type; }
    //endregion
}
