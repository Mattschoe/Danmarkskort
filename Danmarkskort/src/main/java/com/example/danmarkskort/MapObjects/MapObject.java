package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.awt.*;

/**
 * A MapObject is object that is represented and drawn on the map
 */
public interface MapObject {
    public void draw(GraphicsContext graphicsContext);
}
