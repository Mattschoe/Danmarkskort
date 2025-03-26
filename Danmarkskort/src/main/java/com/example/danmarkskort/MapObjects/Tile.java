package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Tile implements MapObject{
    List<MapObject> objectsInTile;

    public Tile() {
        objectsInTile = new ArrayList<>();
    }

    public void addMapObject(MapObject object) {
        objectsInTile.add(object);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        graphicsContext.strokeLine(0, 0, 100, 100);


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
