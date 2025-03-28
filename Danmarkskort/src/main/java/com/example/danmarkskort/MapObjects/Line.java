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

    /**
     * A {@link Line} is a connection between a {@link Node} and another {@link Node}. A {@link Line} resides within a {@link Road} class
     * @param start node
     * @param end node
     */
    public Line(Node start, Node end) {
        assert start != null && end != null;
        this.start = start;
        this.end = end;
    }

    /**
     * Draws a line between the two points provided in the class' constructor
     * @param graphicsContext the style and context for stroke
     */
    public void draw(GraphicsContext graphicsContext) {
        assert start != null && end != null;
        graphicsContext.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }

    //region getters and setters
    public Node getStart() { return start; }
    public Node getEnd() { return end; }
    //endregion
}