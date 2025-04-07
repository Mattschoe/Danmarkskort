package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Road implements Serializable, MapObject {
    @Serial private static final long serialVersionUID = 2430026592275563830L;

    //region Fields
    private final List<Node> nodes;
    private final Set<Line> lines;
    private final boolean foot;
    private final boolean bicycle;
    private int maxSpeed;
    private String roadType;
    private double[] boundingBox;
    private transient Color color;
    private double lineWidth;
    //endregion

    //region Constructor(s)
    /** ROAD WITH MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param maxSpeed the max speed on the road
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, int maxSpeed, String roadType) {
        this.nodes = nodes;
        this.lines = new HashSet<>();
        createLines();

        this.foot = foot;
        this.bicycle = bicycle;
        this.maxSpeed = maxSpeed;
        this.roadType = roadType;

        calculateBoundingBox();
        determineVisuals();
    }

    /** ROAD WITHOUT MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     *  @param nodes the collection of nodes
     *  @param foot if the road is walkable or not Should be true by default
     *  @param bicycle if road the is rideable on bike. Should be true by default
     *  @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, String roadType) {
        this.nodes = nodes;
        this.lines = new HashSet<>();
        createLines();

        this.foot = foot;
        this.bicycle = bicycle;
        this.roadType = roadType;

        calculateBoundingBox();
        determineVisuals();
    }
    //endregion

    //region Methods
    /// Creates the lines between the {@link Node}'s (Used later for drawing)
    private void createLines() {
        //Tegner en linje fra den første node til den sidste i rækkefølge. (No?) Slutter af med at lave en linje mellem den sidste og første
        Node currentNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            lines.add(new Line(currentNode, nodes.get(i)));
            currentNode = nodes.get(i);
        }
        //lines.add(new Line(currentNode, nodes.getLast())); //Tror ikke det her skal bruges i Roads
    }

    /** Draws the road. This method excludes roads like metros which are underground. See {@link #drawMetro(Canvas)} for the ability to draw the metro
     *  @param gc the GraphicsContext in which the road will be drawn
     */
    public void draw(GraphicsContext gc) {
        assert gc != null;

        gc.setStroke(color);
        gc.setLineWidth(lineWidth/Math.sqrt(gc.getTransform().determinant()));

        for (Line line : lines) {
            line.draw(gc);
        }
    }

    /// Determines the Road's color and line-width
    private void determineVisuals() {
        if (roadType.equals("route")) {
            color = Color.TRANSPARENT;
            lineWidth = 1;
        }
        else if (roadType.equals("coastline")) {
            color = Color.BLACK;
            lineWidth = 1.5;
        }
        else {
            color = Color.rgb(100, 100, 100);
            lineWidth = 1;
        }
    }

    private void calculateBoundingBox() {
        boundingBox = new double[4];
        boundingBox[0] = Double.POSITIVE_INFINITY; //minX
        boundingBox[1] = Double.POSITIVE_INFINITY; //minY
        boundingBox[2] = Double.NEGATIVE_INFINITY; //maxX
        boundingBox[3]= Double.NEGATIVE_INFINITY; //maxY

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

    /// Draws the metro, bus-routes, etc.
    @Deprecated public void drawMetro(Canvas mapCanvas) {}

    /// Calculates maxSpeed if the tag wasn't present in the OSM-file
    @Deprecated private void calculateSpeed() {}
    //endregion

    //region Getters and setters
    public Set<Line>  getLines()           { return lines;    }
    public boolean    isWalkable()         { return foot;     }
    public boolean    isCyclable()         { return bicycle;  }
    public int        getMaxSpeed()        { return maxSpeed; }
    public List<Node> getNodes()           { return nodes;    }
    public String     getType()            { return roadType; }
    public boolean    hasRoadType()        { return !roadType.isEmpty(); }

    public void setType(String type) {
        roadType = type;
        determineVisuals();
    }

    @Override
    public double[] getBoundingBox() { return boundingBox; }
    //endregion
}
