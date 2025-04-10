package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.ColorSheet;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.danmarkskort.ColorSheet.*;

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

    private ColorSheet cs;
    private String palette;
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
    public Road(List<Node> nodes, boolean foot, boolean bicycle, String roadType) {
        this.nodes = nodes;
        this.lines = new HashSet<>();
        createLines();

        this.foot = foot;
        this.bicycle = bicycle;
        this.roadType = roadType;
        calculateBoundingBox();

        assignColorSheetProp();
        this.palette = "default";
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
            lineWidth = 1.5;
        }
        else if (roadType.equals("primary")) {
            color = cs.get(1);
            lineWidth = 1.5;
        }
        else if (roadType.equals("secondary")) {
            color = cs.get(2);
            lineWidth = 1.5;
        }
        else if (roadType.equals("tertiary")) {
            color = cs.get(3);
            lineWidth = 1.5;
        }
        else if (roadType.equals("cycleway")) {
            color = cs.get(4);
            lineWidth = 1.1;
        }
        else if (roadType.equals("track") || roadType.equals("path")) {
            color = cs.get(5);
            lineWidth = 1;
        }
        else if (roadType.equals("tree_row")) {
            color = cs.get(6);
            lineWidth = 1;
        }
        else if (roadType.equals("route")) {
            color = cs.get(7);
            lineWidth = 0;
        }
        else {
            //default
            color = cs.get(8);
            lineWidth = 1;
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

    public void setPalette(String palette) {
        this.palette = palette;
        determineVisuals();
    }

    @Override
    public double[] getBoundingBox() { return boundingBox; }
    //endregion
}
