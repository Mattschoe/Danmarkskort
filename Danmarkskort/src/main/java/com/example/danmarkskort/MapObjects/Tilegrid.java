package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

///A Tile-grid stores the relevant tile-grid and a bunch of draw methods.
public class Tilegrid {
    //region Fields
    private final Tile[][] grid;
    ///The bounds of the tile-grid map (aka the coordinate of the first and last grid) <br> \[0] = minX <br> \[1] = minY <br> \[2] = maxX <br> \[3] = maxY
    private final double[] tileGridBounds;
    int tileSize;
    int numberOfTilesX, numberOfTilesY;

    List<Tile> visibleTiles;
    //endregion

    public Tilegrid(Tile[][] grid, double[] tileGridBounds, int tileSize) {
        this.grid = grid;
        this.tileGridBounds = tileGridBounds;
        this.tileSize = tileSize;

        numberOfTilesX = (int) Math.ceil((tileGridBounds[2] - tileGridBounds[0]) / tileSize);
        numberOfTilesY = (int) Math.ceil((tileGridBounds[3] - tileGridBounds[1]) / tileSize);

        initializePredefinedRelations();
    }

    public void drawAllTiles(GraphicsContext graphicsContext, int levelOfDetail) {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                grid[0][0].draw(graphicsContext, levelOfDetail);
            }
        }
    }

    /**
     * Draws the {@code visibleTiles} given the Level of detail
     * @param levelOfDetail ranging from 1 to 5, where 1 being the minimum amount and 5 being the maximum amount of details.
     */
    public void drawVisibleTiles(GraphicsContext graphicsContext, int levelOfDetail) {
        // drawPredefinedRelations(); //OBS det her betyder at vi tegner selvom det ikke kan ses, skal måske ændres senere

        for (Tile tile : visibleTiles) {
            tile.draw(graphicsContext);
        }
    }

    ///All big relations (Predifined in parser, fx: Sjælland, Jylland, osv.)
    private void drawPredefinedRelations() {
        //Gem de vigtige relations
    }

    private void initializePredefinedRelations() {}

    /**
     * All the tiles currently in view
     * @param viewport an array of length 4 with the following specifics: <br> [0] = minX <br> [1] = minY <br> [2] = maxX <br> [3] = maxY
     * @return all the tiles that are visible given the {@code canvasBounds}
     */
    public List<Tile> getTilesInView(double[] viewport) {
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
    public double[] getBoundingBox() { return tileGridBounds; }
    public int getTileSize() { return tileSize; }
    public List<Tile> getVisibleTiles() { return visibleTiles; }
    public void updateVisibleTiles(List<Tile> visibleTiles) { this.visibleTiles = visibleTiles; }
    public int getNumberOfTilesX() { return numberOfTilesX; }
    public int getNumberOfTilesY() { return numberOfTilesY; }
    //endregion



}
