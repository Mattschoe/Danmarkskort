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
    private double length;
    private double weight;

    //region Fields
    private final Node start, end;
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

    }
    //endregion

    //region Methods
    /**
     * Draws a line between the two points provided in the class' constructor
     * @param graphicsContext the style and context for stroke
     */
    public void draw(GraphicsContext graphicsContext) {
        assert start != null && end != null;
        graphicsContext.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }
    //endregion

    //region Getters and setters
    public void calcHaversineDistance() {
        // distance between latitudes and longitudes
        double dLon= Math.toRadians(end.getY() - start.getY());
       double dLat= Math.toRadians(end.getX()- start.getX());

        double lat1 = Math.toRadians(start.getX());
        double lat2 = Math.toRadians(end.getX());

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371; //jordens radius i km
        double c = 2 * Math.asin(Math.sqrt(a));
        weight= rad * c;

    }

  public void assignWeight(){
        this.weight= this.length;
    }

    //region getters and setters
    public Node getStart() { return start; }
    public Node getEnd() { return end; }
    //endregion
}