package com.example.danmarkskort.MapObjects;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.ObjectStreamClass;
import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

public class Line implements Serializable {
    @Serial private static final long serialVersionUID = -9178696453904098837L;
    private Node start, end;
    private double weight;
    private boolean partOfRoute;
    //endregion

    //region Constructor(s)
    /** A {@link Line} is a connection between a {@link Node} and another {@link Node}. A {@link Line} resides within a {@link Road} class
     *  @param start node
     *  @param end node
     */
    public Line(Node start, Node end) {
        assert start != null && end != null;
        this.start = start;
        this.end = end;

        start.addLine(this);
        end.addLine(this);
        calculateWeight();
    }
    //endregion

    //region Methods
    /**
     * Draws a line between the two points provided in the class' constructor
     * @param graphicsContext the style and context for stroke
     */
    public void draw(GraphicsContext graphicsContext) {
        assert start != null && end != null;
        if (partOfRoute) {
            graphicsContext.setLineWidth(0.01);
            graphicsContext.setStroke(Color.RED);
        }
        graphicsContext.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public void calculateWeight() {
        double deltaX = end.getX() - start.getX();
        double deltaY = end.getY() - start.getY();
        weight = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    //endregion

    //region Getters and setters
    ///Maybe change to haversine later, i just didn't want to store a XY AND a lat/long in nodes since that would take quite a lot of space

    //region getters and setters
    public double getWeight() { return weight; }
    ///Returns the opposite of the node given as parameter. Will return null if it isn't a part of the line
    public Node getOppositeNode(Node node) {
        if (node == start) return end;
        else if (node == end) return start;
        else return null;
    }
    public void setPartOfRoute(boolean partOfRoute) { this.partOfRoute = partOfRoute; }
    //endregion
}