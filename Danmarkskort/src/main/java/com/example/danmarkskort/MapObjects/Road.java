package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.ColorSheet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static com.example.danmarkskort.ColorSheet.*;

/**
 * A Road is a way that doesn't contain the same start and endNode. A Road is marked if it's drivable.
 * Formally a Road consists of a list of Nodes that represent this Roads place in XY space.
 * A Road is also used as an edge in a graph, where the list of nodes are condensed into a start- and endNode
 */
public class Road implements Serializable, MapObject {
    @Serial private static final long serialVersionUID = 2430026592275563830L;

    //region Fields
    private final List<Node> nodes;
    private final boolean foot;
    private final boolean bicycle;
    private boolean isDrivable;
    private int maxSpeed;
    private String roadType;
    private String roadName;
    private float[] boundingBox;

    private ColorSheet cs;
    private String palette;
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
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDrivable, int maxSpeed, String roadType, String roadName) {
        this.nodes = nodes;
        this.foot = foot;
        this.bicycle = bicycle;
        this.isDrivable = isDrivable;
        this.maxSpeed = maxSpeed;
        this.roadType = roadType;
        this.roadName = roadName;

        for (Node node : nodes) {
            node.addEdge(this);
        }
        calculateWeight();
        calculateBoundingBox();

        assignColorSheetProp();
        this.palette = "default";
        determineVisuals();
    }

    /** ROAD WITHOUT MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDrivable, String roadType, String roadName) {
        this.nodes = nodes;
        this.foot = foot;
        this.bicycle = bicycle;
        this.isDrivable = isDrivable;
        this.roadType = roadType;
        this.roadName = roadName;
        for (Node node : nodes) {
            node.addEdge(this);
        }
        calculateWeight();
        calculateBoundingBox();

        assignColorSheetProp();
        this.palette = "default";
        determineVisuals();
    }
    //endregion

    //region Methods
    /** Draws the road.
     *  @param gc the GraphicsContext in which the road will be drawn
     */
    public void draw(GraphicsContext gc) {
        assert gc != null;
        if (!isDrivable) return; //TODO %% Skipper lige ikke-bil veje for nu

        if (partOfRoute) gc.setStroke(Color.RED);
        else gc.setStroke(color);
        gc.setLineWidth(lineWidth/Math.sqrt(gc.getTransform().determinant()));


        //Loops through the nodes drawing the lines between them
        Node startNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            Node endNode = nodes.get(i);
            gc.strokeLine(startNode.getX(), startNode.getY(), endNode.getX(), endNode.getY());
            startNode = endNode;
        }
    }

    private void assignColorSheetProp() {
        cs = switch(roadType) {
            case "coastline" -> ROAD_COASTLINE;
            case "primary"   -> ROAD_PRIMARY;
            case "secondary" -> ROAD_SECONDARY;
            case "tertiary"  -> ROAD_TERTIARY;
            case "cycleway"  -> ROAD_CYCLEWAY;
            case "track", "path" -> ROAD_TRACK_PATH;
            case "tree_row"  -> ROAD_TREE_ROW;
            case "route"     -> ROAD_ROUTE;
            default          -> ROAD_DEFAULT;
        };
    }

    private void determineVisuals() {
        color = cs.handlePalette(palette);
        lineWidth = 1;
    }

    /// Determines the Road's color and line-width
    /*private void determineVisuals() {
        if (roadType.equals("coastline")) {
            color = cs.get(0);
            lineWidth = 1.5f;
        }
        else if (roadType.equals("primary")) {
            color = cs.get(1);
            lineWidth = 1.5f;
        }
        else if (roadType.equals("secondary")) {
            color = cs.get(2);
            lineWidth = 1.5f;
        }
        else if (roadType.equals("tertiary")) {
            color = cs.get(3);
            lineWidth = 1.5f;
        }
        else if (roadType.equals("cycleway")) {
            color = cs.get(4);
            lineWidth = 1.1f;
        }
        else if (roadType.equals("track") || roadType.equals("path")) {
            color = cs.get(5);
            lineWidth = 1f;
        }
        else if (roadType.equals("tree_row")) {
            color = cs.get(6);
            lineWidth = 1f;
        }
        else if (roadType.equals("route")) {
            color = cs.get(7);
            lineWidth = 0f;
        }
        else {
            //default
            color = cs.get(8);
            lineWidth = 1f;
        }

        /*
         * Udover de roads vi farver, fandt jeg (Olli) en masse andre roadTypes, heriblandt
         *      unclassified, residential, service, footway, power, cliff og proposed,
         * som jeg har valgt at bare ladet blive farvet default-grå. Så sker der ikke *alt*
         * for meget for øjnene :)
         *
    }
    */
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

    ///TO DO: THIS METHOD NEEDS TO BE FIXED TO ADJUST FOR SPEEDLIMIT BUT IT HASS TO ACCOUNT FOR WHERE THE NODES ARE LOCATED IN XY SPACE}
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
    public String getRoadName() { return roadName; }
    public boolean hasRoadType() { return !roadType.isEmpty(); }
    public boolean hasMaxSpeed() { return maxSpeed != 0; }
    public boolean isWalkable() { return foot; }
    public boolean isBicycle() { return bicycle; }
    ///Returns whether the given node is either the start or the endNode of this road
    public boolean isStartOrEndNode(Node node) { return nodes.getFirst().equals(node) || nodes.getLast().equals(node); }

    /**
     * Returns the opposite of the Node given. So if given the roads startNode it will return the roads endNode (and reverse).
     * @param node HAS TO BE EITHER THE ROADS START- OR END-NODE. WILL RETURN NULL ELSE
     */
    public Node getOppositeNode(Node node) {
        assert isStartOrEndNode(node);
        if (node.equals(nodes.getFirst())) return nodes.getLast();
        if (node.equals(nodes.getLast())) return nodes.getFirst();
        return null;
    }

    public Node getStart() { return nodes.getFirst(); }
    public Node getEnd() { return nodes.getLast(); }

    public void setType(String type) {
        roadType = type;
        determineVisuals();
    }
    public float getWeight() { return weight; }
    @Override
    public float[] getBoundingBox() { return boundingBox; }
    ///Returns either the start- or endNode. Which one is decided from the given {@code node}'s XY
    public Node getStartOrEndNodeFromRoad(Node node) {
        Node startNode = nodes.getFirst();
        Node endNode = nodes.getLast();

        if (node.equals(startNode)) return startNode;
        if (node.equals(endNode)) return endNode;

        float nodeX = node.getX();
        float nodeY = node.getY();

        double distanceToStart = Math.sqrt(Math.pow(startNode.getX() - nodeX, 2) + Math.pow(startNode.getY() - nodeY, 2));
        double distanceToEnd = Math.sqrt(Math.pow(endNode.getX() - nodeX, 2) + Math.pow(endNode.getY() - nodeY, 2));

        if (distanceToStart < distanceToEnd) return startNode;
        else return endNode;
    }

    public void setPartOfRoute(boolean partOfRoute) { this.partOfRoute = partOfRoute; }

    public void setPalette(String palette) {
        this.palette = palette;
        determineVisuals();
    }
    //endregion
}
