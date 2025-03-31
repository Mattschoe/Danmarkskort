package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Road implements Serializable {
    @Serial private static final long serialVersionUID = 2430026592275563830L;

    //region Fields
    private final List<Node> nodes;
    private final Set<Line> lines;
    private final boolean foot;
    private final boolean bicycle;
    private int maxSpeed;
    private String roadType;
    //endregion

    //region Constructor(s)
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
    }
    //endregion

    //region Methods
    /// Creates the lines between the {@link Node}'s (Used later for drawing)
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
     * Draws the road on a given canvas. This method excludes roads like metros which are underground. See {@link #drawMetro(Canvas)} for the ability to draw the metro
     * @param gc the GraphicContext where the road will be drawn on
     */
    public void drawRoad(GraphicsContext gc) {
        assert gc != null;

        switch (roadType) {
            case "route":
                gc.setStroke(Color.DARKRED);
                gc.setLineWidth(1/Math.sqrt(gc.getTransform().determinant())); break;
            case "coastline":
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1.5/Math.sqrt(gc.getTransform().determinant())); break;
            default:
                gc.setStroke(Color.rgb(114, 114, 114));
                gc.setLineWidth(1/Math.sqrt(gc.getTransform().determinant())); break;
        }

        for (Line line : lines) {
            line.drawLine(gc);
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
    public void       setType(String type) { roadType = type; }
    public boolean    hasRoadType()        { return !roadType.isEmpty(); }
    //endregion
}