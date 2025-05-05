package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.*;

public class Tile implements MapObject, Serializable {
    List<MapObject> objectsInTile;
    ///\[0] = minX <br> \[1] = minY <br> \[2] = maxX <br> \[3] = maxY <br>
    float[] bounds;
    int tileSize;
    Set<MapObject> predefinedRelations, motorway, trunk, primary, secondary, tertiary, unclassified, residential, defaultRoad, buildings, area, coastline;
    Set<Node> nodes;
    Set<Road> roads;
    Set<POI> POIs;

    //region Constructor(s)
    public Tile(float minX, float minY, float maxX, float maxY, int tileSize) {
        objectsInTile = new ArrayList<>();
        this.tileSize = tileSize;
        bounds = new float[4];
        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = maxX;
        bounds[3] = maxY;

        //region Initialization of MapObject sets
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
        coastline = new HashSet<>();
        nodes = new HashSet<>();
        POIs = new HashSet<>();
        roads = new HashSet<>();
        //endregion
    }
    //endregion

    //region (Public) Methods
    public void addMapObject(MapObject object) { objectsInTile.add(object); }

    public void addPOI(POI POI) { POIs.add(POI); }

    ///Draws everything in the tile
    @Override
    public void draw(GraphicsContext graphicsContext) {
        for (MapObject object : motorway ) {
            object.draw(graphicsContext);
        }
        for (MapObject object : trunk) {
            object.draw(graphicsContext);
        }
        for (MapObject object : primary) {
            object.draw(graphicsContext);
        }
        for (MapObject object : secondary) {
            object.draw(graphicsContext);
        }
        for (MapObject object : tertiary) {
            object.draw(graphicsContext);
        }
        for (MapObject object : unclassified) {
            object.draw(graphicsContext);
        }
        for (MapObject object : residential) {
            object.draw(graphicsContext);
        }
        for (MapObject object : defaultRoad) {
            object.draw(graphicsContext);
        }
        for (MapObject object : buildings) {
            object.draw(graphicsContext);
        }
        for (MapObject object : area) {
            object.draw(graphicsContext);
        }
        for (MapObject object : coastline) {
            object.draw(graphicsContext);
        }
        for (MapObject object : nodes) {
            object.draw(graphicsContext);
        }
        for (MapObject object : POIs) {
            object.draw(graphicsContext);
        }
    }

    //region draw-methods
    /// All big Motorways
    public void drawMotorway(GraphicsContext graphicsContext) {
        for (MapObject mapObject : motorway) {
            mapObject.draw(graphicsContext);
        }
    }
    /// All important roads that aren't motorways
    public void drawTrunk(GraphicsContext graphicsContext) {
        for (MapObject mapObject : trunk) {
            mapObject.draw(graphicsContext);
        }
    }
    /// All next important roads (Big town to big town)
    public void drawPrimary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : primary) {
            mapObject.draw(graphicsContext);
        }
    }
    /// All next most important roads (town to town)
    public void drawSecondary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : secondary) {
            mapObject.draw(graphicsContext);
        }
    }
    /// All next most important roads
    public void drawTertiary(GraphicsContext graphicsContext) {
        for (MapObject mapObject : tertiary) {
            mapObject.draw(graphicsContext);
        }
    }
    /// Least most important roads
    public void drawUnclassified(GraphicsContext graphicsContext) {
        for (MapObject mapObject : unclassified) {
            mapObject.draw(graphicsContext);
        }
    }
    /// Roads which serve acces to housing
    public void drawResidential(GraphicsContext graphicsContext) {
        for (MapObject mapObject : residential) {
            mapObject.draw(graphicsContext);
        }
    }

    /// Roads without a type
    public void drawDefaultRoad(GraphicsContext graphicsContext) {
        for (MapObject mapObject : defaultRoad) {
            mapObject.draw(graphicsContext);
        }
    }

    /// Buildings
    public void drawBuildings(GraphicsContext graphicsContext) {
        for (MapObject mapObject : buildings) {
            mapObject.draw(graphicsContext);
        }
    }

    /// Area colors
    public void drawArea(GraphicsContext graphicsContext) {
        for (MapObject mapObject : area) {
            mapObject.draw(graphicsContext);
        }
    }

    /// Area colors
    public void drawCoastline(GraphicsContext graphicsContext) {
        for (MapObject mapObject : coastline) {
            mapObject.draw(graphicsContext);
        }
    }

    public void drawNodes(GraphicsContext graphicsContext) {
        for (MapObject mapObject : nodes) {
            mapObject.draw(graphicsContext);
        }
    }

    public void drawPOIs(GraphicsContext graphicsContext) {
        for (POI POI : POIs) {
            POI.draw(graphicsContext);
        }
    }
    //endregion

    /// Initializes all the draw methods so we later can call them in
    public void initializeDrawMethods() {
        for (MapObject mapObject : objectsInTile) {
            if (mapObject instanceof Road road) {
                String roadType = road.getType();
                roads.add(road);
                switch (roadType) {
                    case "motorway" -> motorway.add(mapObject);
                    case "trunk" -> trunk.add(mapObject);
                    case "primary" -> primary.add(mapObject);
                    case "secondary" -> secondary.add(mapObject);
                    case "tertiary" -> tertiary.add(mapObject);
                    case "unclassified" -> unclassified.add(mapObject);
                    case "residential" -> residential.add(mapObject);
                    case "coastline" -> coastline.add(mapObject);
                    default -> defaultRoad.add(mapObject);
                }
            } else if (mapObject instanceof Polygon polygon) {
                String polygonType = polygon.getType();
                if (polygonType.equals("building")) { buildings.add(mapObject); }
                else { area.add(mapObject); }
            } else if (mapObject instanceof Node node) {
                nodes.add(node);
            }
        }
    }

    /// Returns whether a given point is within this tile
    public boolean contains(float x, float y) {
        return x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3];
    }
    //endregion

    //region Getters and setters
    public List<MapObject> getObjectsInTile() { return objectsInTile; }
    public Set<Node> getNodesInTile() { return nodes; }
    public Set<MapObject> getMotorway() { return motorway; }
    public Set<MapObject> getTrunk() { return trunk; }
    public Set<MapObject> getCoastline() { return coastline; }
    public Set<Road> getRoads() { return roads; }
    public Set<POI> getPOIs() { return POIs; }
    @Override public float[] getBoundingBox() { return bounds; }
    public boolean isEmpty() { return objectsInTile.isEmpty(); }
    public void setMotorway(Set<MapObject> motorway) { this.motorway = motorway; }
    public void setTrunk(Set<MapObject> trunk) { this.trunk = trunk; }
    public void setCoastline(Set<MapObject> coastline) { this.coastline = coastline; }
    //endregion
}
