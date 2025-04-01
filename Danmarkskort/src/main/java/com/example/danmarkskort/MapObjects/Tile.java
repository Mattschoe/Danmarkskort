package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    public void draw(GraphicsContext graphicsContext, int LevelOfDetail) {
        //Level 1:
        drawMotorway(graphicsContext);
        if (LevelOfDetail > 1) {
            drawTrunk(graphicsContext);
            drawPrimary(graphicsContext);
            if (LevelOfDetail > 2) {
                drawSecondary(graphicsContext);
                if (LevelOfDetail > 3) {
                    drawTertiary(graphicsContext);
                    drawUnclassified(graphicsContext);
                    drawResidential(graphicsContext);
                    if (LevelOfDetail > 4) {
                        drawBuildings(graphicsContext);
                    }
                }
            }
        }
    }

    //region private draw methods
    ///All big Motorways
    private void drawMotorway(GraphicsContext graphicsContext) {
        for (MapObject mapObject : motorway) {
            mapObject.draw(graphicsContext);
        }
    }
    ///All important roads that aren't motorways
    private void drawTrunk(GraphicsContext graphicsContext) {
        for (MapObject mapObject : trunk) {
            mapObject.draw(graphicsContext);
        }
    }
    ///All next important roads (Big town to big town)
    private void drawPrimary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : primary) {
            mapObject.draw(graphicsContext);
        }
    }
    ///All next most important roads (town to town)
    private void drawSecondary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : secondary) {
            mapObject.draw(graphicsContext);
        }
    }
    ///All next most important roads
    private void drawTertiary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : tertiary) {
            mapObject.draw(graphicsContext);
        }
    }
    ///Least most important roads
    private void drawUnclassified(GraphicsContext graphicsContext) {
        for (MapObject mapObject : unclassified) {
            mapObject.draw(graphicsContext);
        }
    }
    ///Roads which serve acces to housing
    private void drawResidential(GraphicsContext graphicsContext) {
        for (MapObject mapObject : residential) {
            mapObject.draw(graphicsContext);
        }
    }
    ///Buildings
    private void drawBuildings(GraphicsContext graphicsContext) {
        for (MapObject mapObject : buildings) {
            mapObject.draw(graphicsContext);
        }
    }
    //endregion


    /**
     * Initializes all the draw methods so we later can call them in {@link #draw(GraphicsContext, int)}
     */
    private void initializeDrawMethods() {
        for (MapObject mapObject : objectsInTile) {
            if (mapObject instanceof Road road && road.hasRoadType()) {
                String roadType = road.getRoadType();
                if (roadType.equals("motorway")) {
                    motorway.add(mapObject);
                } else if (roadType.equals("trunk")) {
                    trunk.add(mapObject);
                } else if (roadType.equals("primary")) {
                    primary.add(mapObject);
                } else if (roadType.equals("secondary")) {
                    secondary.add(mapObject);
                } else if (roadType.equals("tertiary")) {
                    tertiary.add(mapObject);
                } else if (roadType.equals("unclassified")) {
                    unclassified.add(mapObject);
                } else if (roadType.equals("residential")) {
                    residential.add(mapObject);
                }
            } else if (mapObject instanceof Polygon polygon && polygon.hasType()) {
                buildings.add(polygon);
            }
        }
    }

    public void addMapObject(MapObject object) {
        objectsInTile.add(object);
    }

    //region getters and setters
    public List<MapObject> getObjectsInTile() {
        return objectsInTile;
    }
    @Override
    public double[] getBoundingBox() { return bounds; }
    //endregion
}
