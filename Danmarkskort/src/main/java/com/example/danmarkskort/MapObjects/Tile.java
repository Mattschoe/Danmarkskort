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
    Set<MapObject> predefinedRelations, motorway, trunk, primary, secondary, tertiary, unclassified, residential, defaultRoad, buildings, area;

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
        defaultRoad = new HashSet<>();
        buildings = new HashSet<>();
        area = new HashSet<>();
        //endregion
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
     * @param levelOfDetail ranging from 0 to 4, where 0 being the minimum amount and 4 being the maximum amount of details.
     */
    public void draw(GraphicsContext graphicsContext, int levelOfDetail) {
        //Tegner tilet
        //graphicsContext.setStroke(Color.DARKORANGE);
        //graphicsContext.strokeRect(bounds[0],bounds[1], tileSize, tileSize);

        //HUSK: Altid tegn farve/baggrund før du tegner road på hvert level
        drawMotorway(graphicsContext);
        drawTrunk(graphicsContext);
        if (levelOfDetail > 0) {
            drawPrimary(graphicsContext);
            if (levelOfDetail > 1) {
                drawSecondary(graphicsContext);
                if (levelOfDetail > 2) {
                    drawTertiary(graphicsContext);
                    drawUnclassified(graphicsContext);
                    drawResidential(graphicsContext);
                    drawDefaultRoad(graphicsContext);
                    if (levelOfDetail > 3) {
                        drawArea(graphicsContext);
                        drawBuildings(graphicsContext);
                        draw(graphicsContext, levelOfDetail - 1);
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

    ///Roads without a type
    private void drawDefaultRoad(GraphicsContext graphicsContext) {
        for (MapObject mapObject : defaultRoad) {
            mapObject.draw(graphicsContext);
        }
    }

    ///Buildings
    private void drawBuildings(GraphicsContext graphicsContext) {
        for (MapObject mapObject : buildings) {
            mapObject.draw(graphicsContext);
        }
    }

    ///Area colors
    private void drawArea(GraphicsContext graphicsContext) {
        for (MapObject mapObject : area) {
            mapObject.draw(graphicsContext);
        }
    }
    //endregion


    /**
     * Initializes all the draw methods so we later can call them in {@link #draw(GraphicsContext, int)}
     */
    public void initializeDrawMethods() {
        for (MapObject mapObject : objectsInTile) {
            if (mapObject instanceof Road road) {
                String roadType = road.getRoadType();
                switch (roadType) {
                    case "motorway" -> motorway.add(mapObject);
                    case "trunk" -> trunk.add(mapObject);
                    case "primary" -> primary.add(mapObject);
                    case "secondary" -> secondary.add(mapObject);
                    case "tertiary" -> tertiary.add(mapObject);
                    case "unclassified" -> unclassified.add(mapObject);
                    case "residential" -> residential.add(mapObject);
                    default -> defaultRoad.add(mapObject);
                }
            } else if (mapObject instanceof Polygon polygon) {
                String polygonType = polygon.getType();
                switch (polygonType) {
                    case "building" -> buildings.add(mapObject);
                    default -> area.add(mapObject);
                }
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
