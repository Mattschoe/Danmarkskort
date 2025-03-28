package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class Polygon implements Serializable{
    @Serial private static final long serialVersionUID = 1444149606229887777L;
    //region fields
    private final List<Node> nodes;
    private double[] xPoints;
    private double[] yPoints;
    private int nodeSize; //Øh? Er denne her nødvendig?
    ///The type of polygon, fx: "Building", "Coastline", etc.
    private final String type;
    private double[] boundingBox;
    //endregion

    /**
     * A {@link Polygon} is a collection of {@link Node}'s with the same start- and end {@link Node}
     * @param nodes the collection of nodes belonging to the Polygon
     */
    public Polygon(List<Node> nodes, String type) {
        assert nodes.size() != 1;
        this.nodes = nodes;

        if (type == null) {
            this.type = "";
        } else {
            this.type = type;
        }

        createArrays();
        calculateBoundingBox();
    }

    ///Skaber to Arrays til stroke- og fillPolygon-metoderne der kaldes ved tegning
    public void createArrays() {
        nodeSize = nodes.size();

        xPoints = new double[nodeSize];
        yPoints = new double[nodeSize];

        for (int i = 0; i < nodeSize; i++) {
            xPoints[i] = nodes.get(i).getX();
            yPoints[i] = nodes.get(i).getY();
        }
    }

    public void draw(GraphicsContext gc, boolean drawLines) {
        Color color = switch(type) {
            //Værdier fra "natural"-tag
            case "water"     -> Color.CORNFLOWERBLUE;
            case "heath"     -> Color.SANDYBROWN;
            case "coastline" -> Color.PERU;

            //Værdier fra "landuse"-tag
            case "forest"            -> Color.DARKOLIVEGREEN;
            case "industrial"        -> Color.LIGHTYELLOW;
            case "residential"       -> Color.BURLYWOOD;
            case "brownfield"        -> Color.BURLYWOOD;
            case "grass"             -> Color.DARKSEAGREEN;
            case "landuse"           -> Color.DARKVIOLET;
            case "allotments"        -> Color.BURLYWOOD;
            case "recreation_ground" -> Color.LIGHTCORAL;
            case "construction"      -> Color.LIGHTSLATEGREY;
            case "military"          -> Color.GOLDENROD;
            case "basin"             -> Color.LIGHTBLUE;
            case "cemetery"          -> Color.GREY;

            //Værdier fra "leisure"-tag
            case "marina"        -> Color.STEELBLUE;
            case "sports_centre" -> Color.ROSYBROWN;
            case "yes"           -> Color.DARKSEAGREEN;
            case "picnic_table"  -> Color.PLUM;
            case "playground"    -> Color.THISTLE;
            case "garden"        -> Color.DARKSEAGREEN;
            case "pitch"         -> Color.LIGHTCORAL;
            case "track"         -> Color.CORAL;
            case "leisure"       -> Color.BLACK;
            case "park"          -> Color.DARKSEAGREEN;
            case "swimming_pool" -> Color.LIGHTBLUE;

            //Andre værdier
            case "amenity"    -> Color.LIGHTGREY;
            case "building"   -> Color.DARKGREY;
            case "surface"    -> Color.TAN;
            case "Cityringen" -> Color.TRANSPARENT;
            default -> Color.rgb(0, 74, 127, 0.1);
        };

        gc.setStroke(color.darker().darker());
        gc.setFill(color);

        if (drawLines) gc.strokePolygon(xPoints, yPoints, nodeSize);
        gc.fillPolygon(xPoints, yPoints, nodeSize);
    }

    private void calculateBoundingBox() {
        boundingBox = new double[4];
        boundingBox[0] = Double.POSITIVE_INFINITY; //minX
        boundingBox[1] = Double.POSITIVE_INFINITY; //minY
        boundingBox[2] = Double.NEGATIVE_INFINITY; //maxX
        boundingBox[3]= Double.NEGATIVE_INFINITY; //maxY

        //Finds the lowest and highest X
        for (double x : xPoints) {
            if (x < boundingBox[0]) boundingBox[0] = x;
            if (x > boundingBox[2]) boundingBox[2] = x;
        }

        //Finds the lowest and highest Y
        for (double y : yPoints) {
            if (y < boundingBox[1]) boundingBox[1] = y;
            if (y > boundingBox[3]) boundingBox[3] = y;
        }
    }

    //region getters
    public List<Node> getNodes() { return nodes; }
    public String getType() { return type; }
    public double[] getBoundingBox() { return boundingBox; }
    //endregion
}