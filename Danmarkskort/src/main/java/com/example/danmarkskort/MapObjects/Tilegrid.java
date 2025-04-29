package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.Exceptions.MapObjectOutOfBoundsException;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

///A Tile-grid stores the relevant tile-grid and a bunch of draw methods.
public class Tilegrid implements Serializable {
    //region Fields
    private final Tile[][] grid;
    ///The bounds of the tile-grid map (aka the coordinate of the first and last grid) <br> \[0] = minX <br> \[1] = minY <br> \[2] = maxX <br> \[3] = maxY
    private final float[] tileGridBounds;
    int tileSize;
    int numberOfTilesX, numberOfTilesY;
    Tile zoomedOutTile;

    List<Tile> visibleTiles;
    //endregion

    public Tilegrid(Tile[][] grid, float[] tileGridBounds, int tileSize, int numberOfTilesX, int numberOfTilesY) {
        this.grid = grid;
        this.tileGridBounds = tileGridBounds;
        this.tileSize = tileSize;
        this.numberOfTilesX = numberOfTilesX;
        this.numberOfTilesY = numberOfTilesY;

        initializeZoomedOutTile();
    }

    /**
     * Draws the {@code visibleTiles} given the Level of detail
     * @param levelOfDetail ranging from 0 to 4, where 0 being the minimum amount and 4 being the maximum amount of details.
     */
    public void drawVisibleTiles(GraphicsContext graphicsContext, float[] viewport, int levelOfDetail) {
        if (levelOfDetail < 1) zoomedOutTile.draw(graphicsContext);
        else {
            visibleTiles = getTilesInView(viewport);

            //We first draw colors so we don't overlap roads
            for (Tile tile : visibleTiles) {
                if (tile.isEmpty()) continue;
                if (levelOfDetail > 1) tile.drawArea(graphicsContext);
            }

            //Then we go through each tile and draw their different roads/buildings given the levelOfDetail
            for (Tile tile : visibleTiles) {
                if (tile.isEmpty()) continue;
                drawLOD1(tile, graphicsContext);
                if (levelOfDetail > 1) {
                    drawLOD2(tile, graphicsContext);
                    if (levelOfDetail > 2) {
                        drawLOD3(tile, graphicsContext);
                        if (levelOfDetail > 3) {
                            drawLOD4(tile, graphicsContext);
                            if (levelOfDetail > 4) {
                                drawLOD5(tile, graphicsContext);
                            }
                        }
                    }
                }
            }
        }
    }

    //region private draw methods
    private void drawLOD1(Tile tile, GraphicsContext graphicsContext) {
        tile.drawMotorway(graphicsContext);
        tile.drawTrunk(graphicsContext);
        tile.drawCoastline(graphicsContext);
        tile.drawPrimary(graphicsContext);
    }

    private void drawLOD2(Tile tile, GraphicsContext graphicsContext) {
        tile.drawSecondary(graphicsContext);
    }

    private void drawLOD3(Tile tile, GraphicsContext graphicsContext) {
        tile.drawTertiary(graphicsContext);
    }

    private void drawLOD4(Tile tile, GraphicsContext graphicsContext) {
        tile.drawResidential(graphicsContext);
        tile.drawUnclassified(graphicsContext);
        tile.drawDefaultRoad(graphicsContext);
        tile.drawPOIs(graphicsContext);
    }

    private void drawLOD5(Tile tile, GraphicsContext graphicsContext) {
        tile.drawBuildings(graphicsContext);
        tile.drawPOIs(graphicsContext);
    }

    //endregion

    ///Adds the given mapObject to the tile that fits the given x, y coordinate. x, y should be the coordinate of the mapObject
    public Tile getTileFromXY(float x, float y) {
        for (int i = 0; i < numberOfTilesX; i++) {
            for (int j = 0; j < numberOfTilesY; j++) {
                if (grid[i][j].contains(x, y)) {
                    return grid[i][j];
                }
            }
        }
        return null;
    }

    ///Initializes the tile that's drawn when were zoomed all the way out
    private void initializeZoomedOutTile() {
        Set<MapObject> motorway = new HashSet<>();
        Set<MapObject> trunk = new HashSet<>();
        Set<MapObject> coastline = new HashSet<>();

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                motorway.addAll(grid[x][y].getMotorway());
                trunk.addAll(grid[x][y].getTrunk());
                coastline.addAll(grid[x][y].getCoastline());
            }
        }
        zoomedOutTile = new Tile(tileGridBounds[0], tileGridBounds[1], tileGridBounds[2], tileGridBounds[3], tileSize);
        zoomedOutTile.setMotorway(motorway);
        zoomedOutTile.setTrunk(trunk);
        zoomedOutTile.setCoastline(coastline);
    }

    /**
     * All the tiles currently in view
     * @param viewport an array of length 4 with the following specifics: <br> [0] = minX <br> [1] = minY <br> [2] = maxX <br> [3] = maxY
     * @return all the tiles that are visible given the {@code canvasBounds}
     */
    private List<Tile> getTilesInView(float[] viewport) {
        List<Tile> visibleTiles = new ArrayList<>();

        //Converts viewport's bounding box to tile indices
        int startTileX = (int) ((viewport[0] - tileGridBounds[0]) / tileSize); //Upper left of canvas
        int startTileY = (int) ((viewport[1] - tileGridBounds[1]) / tileSize); //Upper left of canvas
        int endTileX = (int) Math.ceil((viewport[2] - tileGridBounds[0]) / tileSize); //Lower right of canvas
        int endTileY = (int) Math.ceil((viewport[3] - tileGridBounds[1]) / tileSize); //Lower right of canvas
        

        //Clamps them so they are within bounds (Or avoids overflow errors if no tiles are within bounds)
        startTileX = Math.max(startTileX, 0);
        startTileY = Math.max(startTileY, 0);
        endTileX = Math.min(endTileX, numberOfTilesX);
        endTileY = Math.min(endTileY, numberOfTilesY);

        //Adds every visible tile into the List of visible tiles
        for (int i = startTileX; i < endTileX; i++) {
            for (int j = startTileY; j < endTileY; j++) {
                visibleTiles.add(grid[i][j]);
            }
        }
        return visibleTiles;
    }

    //region getters and setters
    public Tile[][] getGrid() { return grid; }
    public float[] getBoundingBox() { return tileGridBounds; }
    public int getTileSize() { return tileSize; }
    public List<Tile> getVisibleTiles() { return visibleTiles; }
    public void updateVisibleTiles(List<Tile> visibleTiles) { this.visibleTiles = visibleTiles; }
    public int getNumberOfTilesX() { return numberOfTilesX; }
    public int getNumberOfTilesY() { return numberOfTilesY; }

    public List<Tile> getGridList() {
        List<Tile> tiles = new ArrayList<>();
        for (Tile[] tileRow : grid) { tiles.addAll(Arrays.asList(tileRow)); }
        return tiles;
    }
    //endregion
}
