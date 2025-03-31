package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serial;
import java.io.Serializable;

public class Line implements Serializable {
    @Serial private static final long serialVersionUID = -9178696453904098837L;

    //region Fields
    private Node start, end;
    //endregion

    //region Constructor(s)
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
    //endregion

    //region Methods
    /**
     * Draws a line between the two points provided in the class' constructor
     * @param graphicsContext the style and context for stroke
     */
    public void drawLine(GraphicsContext graphicsContext) {
        assert start != null && end != null;
        graphicsContext.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }
    //endregion

    //region Getters and setters
    public Node getStart() { return start; }
    public Node getEnd() { return end; }
    //endregion
}