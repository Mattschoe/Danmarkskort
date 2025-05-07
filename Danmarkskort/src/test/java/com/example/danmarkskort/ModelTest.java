package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.POI;
import javafx.scene.canvas.Canvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest extends ApplicationTest {
    Canvas canvas;
    Model model;

    @BeforeEach
    protected void setup() {
        canvas = new Canvas(600, 400);
    }

    @AfterEach
    protected void afterEach() {
        canvas = null;
        model = null;
    }

    @Test
    public void modelFindsFileCorrectly() {
        File objFile = new File("data/small.osm");
        assertTrue(objFile.exists());
        assertDoesNotThrow(() -> {
            model = new Model(objFile.getPath(), canvas, false);
        });
    }

    @Test
    protected void createOBJTest() {
        model = new Model("data/small.osm", canvas, true);

        //Tester at diverse filer er blevet skabt
        File file;
        file = new File("data/generated/small/parser.obj");
        assertTrue(file.exists());

        file = new File("data/generated/small/nodes_0.bin");
        assertTrue(file.exists());

        file = new File("data/generated/small/roads_0.bin");
        assertTrue(file.exists());

        file = new File("data/generated/small/polygons_0.bin");
        assertTrue(file.exists());

        //Sletter alle filer i den skabte mappe, og til sidst også selve mappen
        File folder = new File("data/generated/small");
        String[] files = folder.list();

        long startTime = System.nanoTime();
        //noinspection StatementWithEmptyBody
        while (System.nanoTime() - startTime < 2_000_000_000) {/* wait */}

        //noinspection DataFlowIssue
        for (String s : files) {
            file = new File(folder.getPath(), s);
            assertTrue(file.delete());
        }
        assertTrue(folder.delete());
    }

    @Test
    protected void loadOSMFileTest() {
        assertDoesNotThrow(() ->
            model = new Model("data/small.osm", canvas, false)
        );
    }

    @Test
    protected void loadZIPFileTest() {
        assertDoesNotThrow(() ->
            model = new Model("data/small.zip", canvas, false)
        );
    }

    @Test
    protected void loadOBJFileTest() {
        assertDoesNotThrow(() ->
            model = new Model("data/generated/bornholm/parser.obj", canvas, false)
        );
    }

    @Test
    protected void findPreexistingOBJTest() {
        model = new Model("data/bornholm.zip", canvas, false);
        assertTrue(model.getFile().getPath().endsWith(".obj"));
    }

    @Disabled @Test
    protected void loadStandardMapTest() {
        assertDoesNotThrow(() ->
            model = new Model("data/StandardMap/parser.obj", canvas, false)
        );
    }

    @Test
    protected void createPOITest() {
        Canvas canvas = new Canvas(600, 400);
        Model model = new Model("data/small.osm", canvas, false);
        POI poi = model.createPOI(408.02264f, 386.6936f, "Torvegade 27");

        float x = 408.02264404296875f;
        float y = 386.693603515625f;

        assertEquals(x, poi.getX());
        assertEquals(y, poi.getY());
        assertEquals("Torvegade 27, 1400 København K", poi.getNodeAddress());
        System.out.println(Arrays.toString(poi.getBoundingBox()));
        //assertEquals(new float[]{x,y,x,y});

        poi.draw(canvas.getGraphicsContext2D());
    }
}
