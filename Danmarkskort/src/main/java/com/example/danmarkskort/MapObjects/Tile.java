package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Tile implements MapObject{
    List<MapObject> objectsInTile;
    double[] bounds;

    public Tile(double minX, double minY, double maxX, double maxY) {
        objectsInTile = new ArrayList<>();
        bounds = new double[4];
        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = maxX;
        bounds[3] = maxY;
    }

    public void addMapObject(MapObject object) {
        objectsInTile.add(object);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        graphicsContext.setStroke(Color.DARKORANGE);
        graphicsContext.strokeRect(bounds[0],bounds[1],bounds[2],bounds[3]);

        for (MapObject mapObject : objectsInTile) {
            mapObject.draw(graphicsContext);
        }
    }


    //region getters and setters
    public List<MapObject> getObjectsInTile() {
        return objectsInTile;
    }
    //endregion
}
