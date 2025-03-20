package com.example.danmarkskort.MVC;

import com.example.danmarkskort.AddressSearch.Search;
import com.example.danmarkskort.Exceptions.ParserSavingException;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.Parser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Model {
    //region Fields
    private File file;
    private Parser parser;
    GraphicsContext graphicsContext;
    Canvas canvas;
    File outputFile; //The output .obj file
    //endregion

    /**
     * Checks what filetype the filepath parameter is. Calls {@link #parseOBJ()} if it's a .obj file, if not, it creates a new {@link Parser} class and propagates the responsibility
     * @param filePath the path where the file that needs parsing is loaded (ex.: "/data/small.osm")
     */
    public Model(String filePath, Canvas canvas) {
        assert canvas != null;
        file = new File(filePath);
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        assert file.exists();

        //If .obj file
        if (filePath.endsWith(".obj")) {
            try {
                parseOBJ();
            } catch (Exception e) {
                System.out.println("Error loading .obj!: " + e.getMessage());
                System.out.println("Stacktrace:");
                e.getStackTrace();
            }
        } else {
            //If anything else it creates a new parser and tries saves it as .obj
            try {
                parser = new Parser(file);
                saveParserToOBJ();
            } catch (ParserSavingException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Error loading in the parser: " + e.getMessage() + " | with the exception being: " + e.getClass());
            }
        }
        assert parser != null;

        //TESTING
        Search search = new Search(getAllNodesWithStreetAddresses(parser.getNodes().values()));
        System.out.println(search.findNode("Bergthorasgade").getAddress()[3] + " " + search.findNode("Bergthorasgade").getAddress()[1] + " " + search.findNode("Bergthorasgade").getAddress()[2] + " " + search.findNode("Bergthorasgade").getAddress()[0]);
    }

    /**
     * Parses a .obj file. This method is called in the Parser constructer if the givin filepath ends with .obj
     */
    private void parseOBJ() throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        parser = (Parser) input.readObject();
        input.close();
    }

    /**
     * Saves the parser to a .obj file so it can be called later. Method is called in {@link #Model} if the file isn't a .obj
     */
    private void saveParserToOBJ() {
        outputFile = new File(file+".obj");
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFile));
            outputStream.writeObject(parser);
            outputStream.close();
        } catch (IOException e) {
            throw new ParserSavingException("Error saving parser as .obj! Error Message: " + e.getMessage());
        }

    }

    //region getters and setters

    /**
     * Gives all nodes that contains an address
     * @param allNodes all nodes parsed in the parser
     * @return all nodes with a street address (f.ex: "Decembervej")
     */
    public Set<Node> getAllNodesWithStreetAddresses(Collection<Node> allNodes) {
        Set<Node> nodesWithStreetAddresses = new HashSet<>();
        for (Node node : allNodes) {
            try {
                assert node.getAddress()[3] != null;
                nodesWithStreetAddresses.add(node);
            } catch (NullPointerException _) {} //Doesn't have a street address
        }
        return nodesWithStreetAddresses;
    }
    public Parser getParser() {
        return parser;
    }
    //endregion
}
