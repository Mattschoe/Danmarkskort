package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

/// A MapObject is object that is represented and drawn on the map
public interface MapObject {
    void draw(GraphicsContext graphicsContext);
    /** Returns an array of type double, of length 4. This is the bounding box of the mapObject
     * [0] = minX
     * [1] = minY
     * [2] = maxX
     * [3] = maxY
     */
    float[] getBoundingBox();
}
