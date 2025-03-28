package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Road implements Serializable, MapObject {
    //region Fields
    @Serial private static final long serialVersionUID = 2430026592275563830L;
    private final List<Node> nodes;
    private final Set<Line> lines;
    private final boolean foot;
    private final boolean bicycle;
    private int maxSpeed;
    private final String roadType;
    private double[] boundingBox;
    //endregion

    /**
     * ROAD WITH MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     * @param nodes the collection of nodes
     * @param foot if the road is walkable or not Should be true by default
     * @param bicycle if road the is rideable on bike. Should be true by default
     * @param maxSpeed the max speed on the road
     * @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, int maxSpeed, String roadType) {
        this.nodes = nodes;
        lines = new HashSet<>();
        this.foot = foot;
        this.bicycle = bicycle;
        this.maxSpeed = maxSpeed;
        this.roadType = roadType;
        createLines();
        updateBoundingBox();
    }

    /**
     * ROAD WITHOUT MAXSPEED. A {@link Road} is a collection of {@link Node}'s without the same start and end node.
     * @param nodes the collection of nodes
     * @param foot if the road is walkable or not Should be true by default
     * @param bicycle if road the is rideable on bike. Should be true by default
     * @param roadType the type of road
     */
    public Road(List<Node> nodes, boolean foot, boolean bicycle, String roadType) {
        this.nodes = nodes;
        lines = new HashSet<>();
        this.foot = foot;
        this.bicycle = bicycle;
        this.roadType = roadType;
        createLines();
        updateBoundingBox();
    }

    ///Creates the lines between the {@link Node}'s (Used later for drawing)
    private void createLines() {
        //Tegner en linje fra den første node til den sidste i rækkefølge. (No?) Slutter af med at lave en linje mellem den sidste og første
        Node currentNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            lines.add(new Line(currentNode, nodes.get(i)));
            currentNode = nodes.get(i);
        }
        //lines.add(new Line(startNode, nodes.getLast())); Tror ikke det her skal bruges i Roads
    }

    /**
     * Draws the road on a given canvas. This method excludes roads like metro's which are underground. See {@link #drawMetro(Canvas)} for the ability to draw the metro
     * @param graphicsContext the graphicsContext where the road will be drawn on
     */
    public void draw(GraphicsContext graphicsContext) {
        assert graphicsContext != null;
        switch (roadType) {
            case "route":
                graphicsContext.setStroke(Color.DARKRED);
                graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant())); break;
            case "coastline":
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(1.5/Math.sqrt(graphicsContext.getTransform().determinant())); break;
            default:
                graphicsContext.setStroke(Color.WHITE.darker().darker());
                graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant())); break;
        }

        for (Line line : lines) {
            line.draw(graphicsContext);
        }
    }

    /**
     * Draws the metro
     * @param mapCanvas
     */
    @Deprecated
    public void drawMetro(Canvas mapCanvas) {}

    private void updateBoundingBox() {
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

    //region getters
    public Set<Line>  getLines() { return lines;    }
    public boolean  isWalkable() { return foot;     }
    public boolean  isCyclable() { return bicycle;  }
    public int     getMaxSpeed() { return maxSpeed; }
    public String  getRoadType() { return roadType; }
    public List<Node> getNodes() { return nodes;    }
    public boolean hasRoadType() { return !roadType.isEmpty(); }
    @Override
    public double[] getBoundingBox() { return boundingBox; }
    //endregion

    ///Tom metode for at regne maxspeed hvis tagget mangler
    @Deprecated private void calculateSpeed(){
        //tom metode er tom
    }
}
