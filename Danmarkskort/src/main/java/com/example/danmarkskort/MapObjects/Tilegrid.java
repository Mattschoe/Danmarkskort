package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;

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

    public void draw(GraphicsContext graphicsContext, int LevelOfDetail) {
        drawPredefinedRelations(); //OBS det her betyder at vi tegner selvom det ikke kan ses, skal måske ændres senere

        for (Tile tile : visibleTiles) {
            tile.draw(graphicsContext);
        }
    }

    ///All big relations (Predifined in parser, fx: Sjælland, Jylland, osv.)
    private void drawPredefinedRelations() {
        //Gem de vigtige relations
    }

    //region getters and setters
    public Tile[][] getGrid() { return grid; }
    public double[] getBoundingBox() { return tileGridBounds; }
    public int getTileSize() { return tileSize; }
    public List<Tile> getVisibleTiles() { return visibleTiles; }
    public void updateVisibleTiles(List<Tile> visibleTiles) { this.visibleTiles = visibleTiles; }
    //endregion



}
