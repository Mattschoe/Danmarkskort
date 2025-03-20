package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class Polygon implements Serializable {
    @Serial private static final long serialVersionUID = 1444149606229887777L;
    //region fields
    private final List<Node> nodes;
    private double[] xPoints;
    private double[] yPoints;
    private int nSize;
    private final String type; //The type of polygon, fx: "Building", "Coastline", etc.
    //endregion

    /**
     * A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     * @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.nodes = nodes;
        if (type == null) this.type = "";
        else this.type = type;

        createArrays();
    }

    ///Skaber to Arrays til stroke- og fillPolygon-metoderne der kaldes ved tegning
    public void createArrays() {
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
            //Værdier fra "natural"-tag
            case "building"  -> Color.DARKGRAY;
            case "water"     -> Color.CORNFLOWERBLUE;
            case "heath"     -> Color.CHARTREUSE;
            case "coastline" -> Color.PERU;

            //Værdier fra "landuse"-tag
            case "forest"            -> Color.GREEN;
            case "industrial"        -> Color.YELLOW;
            case "residential"       -> Color.BURLYWOOD;
            case "brownfield"        -> Color.SADDLEBROWN;
            case "grass"             -> Color.GREENYELLOW;
            case "landuse"           -> Color.DARKVIOLET;
            case "allotments"        -> Color.HOTPINK;
            case "recreation_ground" -> Color.LIGHTCORAL;
            case "construction"      -> Color.TOMATO;
            case "military"          -> Color.SPRINGGREEN;
            case "basin"             -> Color.CYAN;
            case "cemetery"          -> Color.CRIMSON;

            //Standardværdi
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