package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Road implements Serializable {
    @Serial private static final long serialVersionUID = 2430026592275563830L;
    //region Fields
    private List<Node> nodes;
    private Set<Line> lines;
    private boolean foot;
    private boolean bicycle;
    private int maxSpeed;
    private String roadType;
    //endregion
    // private static final long serialVersionUID;

    /**
     * A {@link Road} is a collection of {@link Node}'s without the same start and end node.
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

    public Road(List<Node> nodes, boolean foot, boolean bicycle, String roadType) {
        this.nodes = nodes;
        lines = new HashSet<>();
        this.foot = foot;
        this.bicycle = bicycle;
        this.roadType = roadType;
        createLines();
    }

    ///Creates the lines between the {@link Node}'s (Used later for drawing)
    private void createLines() {
        //Tegner en linje fra den første node til den sidste i rækkefølge. Slutter af med at lave en linje mellem den sidste og første
        Node startNode = nodes.getFirst();
        for (int i = 1; i < nodes.size(); i++) {
            lines.add(new Line(startNode, nodes.get(i)));
            startNode = nodes.get(i);
        }
        lines.add(new Line(startNode, nodes.getLast()));
    }

    public void drawRoad(Canvas mapCanvas) {
        assert mapCanvas != null;
        GraphicsContext graphicsContext = mapCanvas.getGraphicsContext2D();
        if (roadType.equals("coastline")) {
            graphicsContext.setStroke(Color.ORANGE);
            graphicsContext.setLineWidth(2/Math.sqrt(graphicsContext.getTransform().determinant()));
        }
        else {
            graphicsContext.setStroke(Color.WHITE.darker().darker());
            graphicsContext.setLineWidth(1/Math.sqrt(graphicsContext.getTransform().determinant()));
        }

        for (Line line : lines) {
            line.drawLine(graphicsContext);
        }
    }

    //region getters
    public Set<Line>  getLines() { return lines;    }
    public boolean  isWalkable() { return foot;     }
    public boolean  isCyclable() { return bicycle;  }
    public int     getMaxSpeed() { return maxSpeed; }
    public String  getRoadType() { return roadType; }
    public List<Node> getNodes() { return nodes;    }
    //endregion

    ///Tom metode for at regne maxspeed hvis tagget mangler
    private void calculateSpeed(){
        //tom metode er tom
    }
}
