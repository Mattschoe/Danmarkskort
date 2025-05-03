package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Model;
import javafx.scene.canvas.Canvas;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    Canvas canvas;

    @BeforeEach
    public void setup() {
        canvas = new Canvas(400, 600);
    }

    @AfterEach
    public void afterEach() {
        System.gc();
    }

    @Test
    public void generateOBJTest() {
        Model.getInstance("./data/small.osm", canvas, true);

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

        //noinspection DataFlowIssue
        for (String s : files) {
            file = new File(folder.getPath(), s);
            assertTrue(file.delete());
        }
        assertTrue(folder.delete());
    }

    @Test
    protected void loadOBJFileTest() {
        Model.getInstance("data/generated/small/parser.obj", canvas, false);
    }

    /// Checks if model can correctly load .obj to a parser class
    @Test
    public void loadParserAsOBJ() {
        File objFile = new File("./data/small.osm.obj");

        Model createObjFileModel = Model.getInstance("./data/Bornholm.zip", canvas, false);
        Model createParserFromObjModel = Model.getInstance(objFile.getPath(), canvas, false);
        //assertNotNull(createParserFromObjModel.getParser());
    }

    /*
     * Same as {@link #saveParserAsOBJ()} just doesn't delete the OBJ file again. Should be marked @Disabled as standard since it doesn't dele the file again
     *
    //@Disabled
    @Test
    public void createOBJFile() {
        Model model = Model.getInstance("./data/mapOfDenmark.osm", canvas);
        model.saveParserToOBJ();
        File file = new File("./data/mapOfDenmark.osm.obj");
        assertTrue(file.exists());
    }*/

    /*@Test
    public void modelFindsFileCorrectly() {
        File objFile = new File("./data/small.osm.obj");
        assertTrue(objFile.exists());
    }*/
}
