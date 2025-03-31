package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Tile implements MapObject{
    private List<MapObject> objectsInTile;
    private double[]        bounds;
    private int             tileSize;

    public Tile(double minX, double minY, double maxX, double maxY, int tileSize) {
        objectsInTile = new ArrayList<>();
        this.tileSize = tileSize;
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
        //Tegner tilet
        //graphicsContext.setStroke(Color.DARKORANGE);
        //graphicsContext.strokeRect(bounds[0],bounds[1], tileSize, tileSize);

        //Tegner objekterne i tilet
        for (MapObject mapObject : objectsInTile) {
            mapObject.draw(graphicsContext);
        }
    }

    //region Getters and setters
    public List<MapObject> getObjectsInTile() { return objectsInTile; }

    @Override
    public double[] getBoundingBox() { return bounds; }
    //endregion
}
