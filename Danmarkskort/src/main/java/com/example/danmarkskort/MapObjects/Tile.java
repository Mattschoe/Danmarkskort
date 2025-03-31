package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tile implements MapObject{
    List<MapObject> objectsInTile;
    double[] bounds;
    int tileSize;
    Set<MapObject> predefinedRelations, motorway, trunk, primary, secondary, tertiary, unclassified, residential, buildings;

    public Tile(double minX, double minY, double maxX, double maxY, int tileSize) {
        objectsInTile = new ArrayList<>();
        this.tileSize = tileSize;
        bounds = new double[4];
        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = maxX;
        bounds[3] = maxY;

        //region Initialization of Mapobject sets
        predefinedRelations = new HashSet<>();
        motorway = new HashSet<>();
        trunk = new HashSet<>();
        primary = new HashSet<>();
        secondary = new HashSet<>();
        tertiary = new HashSet<>();
        unclassified = new HashSet<>();
        residential = new HashSet<>();
        buildings = new HashSet<>();
        //endregion
        initializeDrawMethods();
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

    /**
     * Draws the {@code visibleTiles} given the Level of detail
     * @param LevelOfDetail ranging from 1 to 5, where 1 being the minimum amount and 5 being the maximum amount of details.
     */
    public void draw(int LevelOfDetail) {
        //Level 1:
        drawPredefinedRelations();
        drawMotorway();
        if (LevelOfDetail > 1) {
            drawTrunk();
            drawPrimary();
            if (LevelOfDetail > 2) {
                drawSecondary();
                if (LevelOfDetail > 3) {
                    drawTertiary();
                    drawUnclassified();
                    drawResidential();
                    if (LevelOfDetail > 4) {
                        drawBuildings();
                    }
                }
            }
        }
    }

    //region private draw methods
    ///All big relations (Predifined in parser, fx: Sjælland, Jylland, osv.)
    private void drawPredefinedRelations() {}
    ///All big Motorways
    private void drawMotorway() {}
    ///All important roads that aren't motorways
    private void drawTrunk() {}
    ///All next important roads (Big town to big town)
    private void drawPrimary() {}
    ///All next most important roads (town to town)
    private void drawSecondary() {}
    ///All next most important roads
    private void drawTertiary() {}
    ///Least most important roads
    private void drawUnclassified() {}
    ///Roads which serve acces to housing
    private void drawResidential() {}
    ///Buildings
    private void drawBuildings() {}
    //endregion


    /**
     * Initializes all the draw methods so we later can call them in {@link #draw(int)}
     */
    private void initializeDrawMethods() {

    }

    //region getters and setters
    public List<MapObject> getObjectsInTile() {
        return objectsInTile;
    }
    @Override
    public double[] getBoundingBox() { return bounds; }
    //endregion
}
