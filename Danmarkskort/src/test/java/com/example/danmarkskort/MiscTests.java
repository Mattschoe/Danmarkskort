package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MiscTests {
    Canvas canvas;

    @BeforeEach
    protected void setup() {
        canvas = new Canvas(600, 400);
    }

    @Test
    protected void changePaletteTest() {
        Model model = Model.getInstance("data/bornholm.zip", canvas, false);
        assertNotNull(model);

        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("midnight");
                if (mo instanceof Polygon p) p.setPalette("midnight");
            }
        }

        for (Tile tile : model.getTilegrid().getGridList()) {
            for (MapObject mo : tile.getObjectsInTile()) {
                if (mo instanceof Road road) road.setPalette("basic");
                if (mo instanceof Polygon p) p.setPalette("basic");
            }
        }
    }

    @Test
    protected void drawNodesTest() {
        Model model = Model.getInstance("data/bornholm.zip", canvas, false);
        assertNotNull(model);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        //Tegner alt i en Viewport på 10,000 x 10,000 pixels, med max detaljer
        model.getTilegrid().drawVisibleTiles(gc, new float[]{0, 0, 10_000, 10_000}, 5);

        //Kalder ekstra draw metoder for coverage :^)
        for (Tile tile : model.getTilegrid().getGridList()) {
            tile.draw(gc);
            tile.drawNodes(gc);
            tile.drawPOIs(gc);
        }
    }
}