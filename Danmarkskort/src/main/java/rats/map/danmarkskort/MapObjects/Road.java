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
    private final boolean isDriveable;
    private final boolean isOneway;
    private int maxSpeed;
    private final String roadType;
    private final String roadName;
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
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDriveable, boolean isOneway, int maxSpeed, String roadType, String roadName) {
        this.nodes = nodes;
        this.foot = foot;
        this.bicycle = bicycle;
        this.isDriveable = isDriveable;
        this.isOneway = isOneway;
        this.maxSpeed = maxSpeed;
        this.roadType = roadType.intern();
        this.roadName = roadName.intern();

        calculateWeight(true);
        calculateBoundingBox();

        assignColorSheet();
        this.palette = "default";
        determineVisuals();
    }

    /** ROAD WITHOUT MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, boolean isDriveable, String roadType, String roadName) {
        this.nodes = nodes;
        this.foot = foot;
        this.bicycle = bicycle;
        this.isDriveable = isDriveable;
        this.isOneway = false;
        this.roadType = roadType.intern();
        this.roadName = roadName.intern();

        calculateWeight(true);
        calculateBoundingBox();

        assignColorSheet();
        this.palette = "default";
        determineVisuals();
    }
    //endregion

    //region Methods
    /**
     * Draws the road.
     * @param gc the GraphicsContext in which the road will be drawn
     */
    @Override
    public void draw(GraphicsContext gc) {
        if (!isDriveable) return; //Skips if non-drivable road

        if (partOfRoute) {
            gc.setStroke(Color.RED);
            lineWidth = 3f;
        }
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

    private void assignColorSheet() {
        cs = switch(roadType) {
            case "coastline" -> ROAD_COASTLINE;
            case "primary"   -> ROAD_PRIMARY;
            case "secondary" -> ROAD_SECONDARY;
            case "tertiary"  -> ROAD_TERTIARY;
            case "cycleway"  -> ROAD_CYCLEWAY;
            case "track", "path" -> ROAD_TRACK_PATH;
            case "tree_row"  -> ROAD_TREE_ROW;
            case "route"     -> ROAD_ROUTE;
            case "river" -> POLY_WATERWAY;
            default          -> ROAD_DEFAULT;
        };
    }

    private void determineVisuals() {
        color = cs.handlePalette(palette);

        lineWidth = switch(roadType) {
            case "coastline" -> 2f;
            case "primary"   -> 1.9f;
            case "secondary" -> 1.8f;
            case "tertiary"  -> 1.7f;
            default -> 1f;
        };
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

    /**
     * Calculates the weight of the Road. The {@code weight} being defined by the parameter
     * @param quickestRoute chooses the method of calculating the weight. <br> True = Quickest <br> False = Shortest <br>
     */
    private void calculateWeight(boolean quickestRoute) {
        //Loops through all the nodes in the road calculating the total distance between them all
        double distance = 0;
        Node currentNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            Node nextNode = nodes.get(i);
            distance += Math.hypot((currentNode.getX() - nextNode.getX()), (currentNode.getY() - nextNode.getY()));
            currentNode = nextNode;
        }

        if (quickestRoute) {
            //Calculates the time it takes to cross the distance via distance/maxSpeed
            if (maxSpeed > 0) weight = (float) distance / maxSpeed;
            else {
                //Else we see if we can calculate the weight from the road-type (Motorvej/Motortrafikvej), if not, we set the standard speed as 50
                if (roadType.equals("motorway")) weight = (float) distance / 130;
                else if (roadType.equals("trunk")) weight = (float) distance / 80;
                else weight = (float) distance / 50;
            }
        } else {
            //Shortest path
            weight = (float) distance;
        }
    }

    //endregion

    //region Getters and setters
    /// Returns whether this piece of road is driveable or not (not in this case means walkable/cyclable)
    public boolean isDriveable() { return isDriveable;  }
    public int getMaxSpeed() { return maxSpeed; }
    public List<Node> getNodes() { return nodes;    }
    public String getType() { return roadType; }
    public String getRoadName() { return roadName; }
    public boolean hasMaxSpeed() { return maxSpeed != 0; }
    public boolean isWalkable() { return foot; }
    public boolean isBicycle() { return bicycle; }
    public boolean isOneway() { return isOneway; }
    public float getWeight() { return weight; }
    public void setPartOfRoute(boolean partOfRoute) { this.partOfRoute = partOfRoute; }
    /**
     * Returns the opposite of the Node given. So if given the roads startNode it will return the roads endNode (and reverse).
     * @param node HAS TO BE EITHER THE ROADS START- OR END-NODE. WILL RETURN NULL ELSE
     */
    public Node getOppositeNode(Node node) {
        if (node.equals(nodes.getFirst())) return nodes.getLast();
        if (node.equals(nodes.getLast())) return nodes.getFirst();
        return null;
    }
    ///Returns the last node in the road. Used for going the right of way if the road is oneway in searching
    public Node getEndNode() { return nodes.getLast(); }
    @Override public float[] getBoundingBox() { return boundingBox; }
    /// Returns either the start- or endNode. Which one is decided from the given {@code node}'s XY
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
    public void setPalette(String palette) {
        this.palette = palette;
        determineVisuals();
    }
    ///Sets the type of search algorithm that will be used. <br> true = Quickest Route <br> false = Shortest Route
    public void changeWeight(boolean quickestRoute) {
        calculateWeight(quickestRoute);
    }
    //endregion
}
