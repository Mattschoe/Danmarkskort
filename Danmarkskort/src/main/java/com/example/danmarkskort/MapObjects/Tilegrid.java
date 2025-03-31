package com.example.danmarkskort.MapObjects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

///A Tile-grid stores the relevant tile-grid and a bunch of draw methods.
public class Tilegrid {
    private final Tile[][] grid;
    private final double[] tileGridBounds;
    int tileSize;
    List<Tile> visibleTiles;

    public Tilegrid(Tile[][] grid, double[] tileGridBounds) {
        this.grid = grid;
        this.tileGridBounds = tileGridBounds;
        tileSize = 10;
    }

    //region getters and setters
    public Tile[][] getGrid() { return grid; }
    public double[] getBoundingBox() { return tileGridBounds; }
    public int getTileSize() { return tileSize; }
    public List<Tile> getVisibleTiles() { return visibleTiles; }
    public void updateVisibleTiles(List<Tile> visibleTiles) { this.visibleTiles = visibleTiles; }
    //endregion



}
