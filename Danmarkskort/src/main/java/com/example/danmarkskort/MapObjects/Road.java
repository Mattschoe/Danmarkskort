package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

///A Road is a way that doesn't contain the same start and endNode. A Road is marked if it's drivable. Formally a Road consists of a list of Nodes that represent this Roads place in XY space
public class Road implements Serializable, MapObject {
    @Serial private static final long serialVersionUID = 2430026592275563830L;

    //region Fields
    private final List<Node> nodes;
    private final boolean foot;
    private final boolean bicycle;
    private boolean isDrivable;
    private int maxSpeed;
    private String roadType;
    private float[] boundingBox;
    private transient Color color;
    private float lineWidth;
    private float weight;
    private boolean partOfRoute;
    //endregion

    //region Constructor(s)
    /** ROAD WITH MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param maxSpeed the max speed on the road
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDrivable, int maxSpeed, String roadType) {
        this.nodes = nodes;
        this.foot = foot;
        this.bicycle = bicycle;
        this.isDrivable = isDrivable;
        this.maxSpeed = maxSpeed;
        this.roadType = roadType;


        for (Node node : nodes) {
            node.addEdge(this);
        }
        calculateWeight();
        calculateBoundingBox();
        determineVisuals();
    }

    /** ROAD WITHOUT MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDrivable, String roadType) {
        this.nodes = nodes;
        calculateWeight();

        this.foot = foot;
        this.bicycle = bicycle;
        this.isDrivable = isDrivable;
        this.roadType = roadType;

        calculateBoundingBox();
        determineVisuals();
    }
    //endregion

    //region Methods
    /** Draws the road.
     *  @param gc the GraphicsContext in which the road will be drawn
     */
    public void draw(GraphicsContext gc) {
        assert gc != null;
        if (!isDrivable) return; //Skipper lige ikke-bil veje for nu

        if (partOfRoute) {
            gc.setStroke(Color.RED);
        } else {
            gc.setStroke(color);
        }
        gc.setLineWidth(lineWidth/Math.sqrt(gc.getTransform().determinant()));


        //Loops through the nodes drawing the lines between them
        Node startNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            Node endNode = nodes.get(i);
            gc.strokeLine(startNode.getX(), startNode.getY(), endNode.getX(), endNode.getY());
            startNode = endNode;
        }
    }

    /// Determines the Road's color and line-width
    private void determineVisuals() {
        if (roadType.equals("coastline")) {
            color = Color.BLACK;
            lineWidth = 1.5f;
        }
        else if (roadType.equals("primary")) {
            color = Color.DARKORANGE;
            lineWidth = 1.5f;
        }
        else if (roadType.equals("secondary")) {
            color = Color.DARKSLATEBLUE;
            lineWidth = 1.5f;
        }
        else if (roadType.equals("tertiary")) {
            color = Color.DARKGREEN;
            lineWidth = 1.5f;
        }
        else if (roadType.equals("cycleway")) {
            color = Color.DARKMAGENTA;
            lineWidth = 1.1f;
        }
        else if (roadType.equals("track") || roadType.equals("path")) {
            color = Color.SIENNA;
            lineWidth = 1f;
        }
        else if (roadType.equals("tree_row")) {
            color = Color.rgb(172, 210, 156);
            lineWidth = 1f;
        }
        else if (roadType.equals("route")) {
            color = Color.TRANSPARENT;
            lineWidth = 0f;
        }
        else {
            //default
            color = Color.rgb(100, 100, 100);
            lineWidth = 1f;
        }

        /*
         * Udover de roads vi farver, fandt jeg (Olli) en masse andre roadTypes, heriblandt
         *      unclassified, residential, service, footway, power, cliff og proposed,
         * som jeg har valgt at bare ladet blive farvet default-grå. Så sker der ikke *alt*
         * for meget for øjnene :)
         */
    }

    private void calculateBoundingBox() {
        boundingBox = new float[4];
        boundingBox[0] = Float.POSITIVE_INFINITY; //minX
        boundingBox[1] = Float.POSITIVE_INFINITY; //minY
        boundingBox[2] = Float.NEGATIVE_INFINITY; //maxX
        boundingBox[3]= Float.NEGATIVE_INFINITY; //maxY

        //Finds the lowest and highest XY
        for (Node node : nodes) {
            //X
            if (node.getX() < boundingBox[0]) boundingBox[0] = node.getX(); //Smaller X
            if (node.getX() > boundingBox[2]) boundingBox[2] = node.getX(); //Bigger X

            //Y
            if (node.getY() < boundingBox[1]) boundingBox[1] = node.getY(); //Smaller Y
            if (node.getY() > boundingBox[3]) boundingBox[3] = node.getY(); //Bigger Y
        }
    }

    /// Calculates maxSpeed if the tag wasn't present in the OSM-file
    private void calculateWeight() {
        float deltaX = nodes.getFirst().getX() - nodes.getLast().getX();
        float deltaY = nodes.getFirst().getY() - nodes.getLast().getY();
        weight = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    //endregion

    //region Getters and setters
    ///Returns whether this piece of road is drivable or not (not in this case means walkable/cyclable)
    public boolean isDrivable() { return isDrivable;  }
    public int getMaxSpeed() { return maxSpeed; }
    public List<Node> getNodes() { return nodes;    }
    public String getType() { return roadType; }
    public boolean hasRoadType() { return !roadType.isEmpty(); }
    public boolean hasMaxSpeed() { return maxSpeed != 0; }
    public boolean isWalkable() { return foot; }
    public boolean isBicycle() { return bicycle; }

    public void setType(String type) {
        roadType = type;
        determineVisuals();
    }
    public float getWeight() { return weight; }
    @Override
    public float[] getBoundingBox() { return boundingBox; }
    ///Returns the next node following the provided node
    public Node getNext(Node node) {
        if (node.equals(nodes.getLast())) return null;
        return nodes.get(nodes.indexOf(node) + 1);
    }
    public void setPartOfRoute(boolean partOfRoute) { this.partOfRoute = partOfRoute; }
    //endregion
}
