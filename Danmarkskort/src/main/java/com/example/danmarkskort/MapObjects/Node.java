package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.Exceptions.InvalidAddressException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

public class Node implements Serializable, MapObject, Comparable<Node> {
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private double x, y;
    private String[] address;
    private int distanceTo;
    private HashMap<Node, Integer> adjacentNodes = new HashMap<>();
    private LinkedList<Node> shortestPath = new LinkedList<>();
    //endregion

    //region Constructor(s)
    /** A {@link Node} is a point in a (x, y) space. {@link Node} calculates the (x, y) point
     *  itself in the {@link #calculateXY} method when being instantiated
     */
    public Node(double latitude, double longitude) {
        distanceTo = Integer.MAX_VALUE;
        calculateXY(latitude, longitude);
    }

    /**
     * A Node that contains an address
     * @param latitude
     * @param longitude
     * @param city
     * @param houseNumber
     * @param postcode
     * @param street
     */
    public Node(double latitude, double longitude, String city, String houseNumber, int postcode, String street) {
        distanceTo = Integer.MAX_VALUE;
        calculateXY(latitude, longitude);
        address = new String[4];
        saveAddress(city, houseNumber, postcode, street);
    }
    //endregion

    //region Methods
    /** Calculates X and Y from Latitude and Longitude using same method as teacher
     *  @param latitude same as constructor
     *  @param longitude same as constructor
     */
    private void calculateXY(double latitude, double longitude) {
        //Bounds of DK (ish)
        double minLat = 54.5;
        double maxLat = 57.8;
        double minLon = 8.0;
        double maxLon = 12.5;

        int width = 400;
        int height = 600;

        //Calculates XY
        double xNorm = ((longitude - minLon) / (maxLon - minLon));
        double yNorm = ((latitude - minLat) / (maxLat - minLat));
        x = xNorm * width;
        y = (1 - yNorm) * height; //Makes sure Y isn't mirrored
    }

    public void draw(GraphicsContext graphicsContext) {

    }

    /** Parses address, checks its correct and saves it in a 4 size array
     *  @param city same as constructor
     *  @param houseNumber same as constructor
     *  @param postcode same as constructor
     *  @param street same as constructor
     */
    private void saveAddress(String city, String houseNumber, int postcode, String street) {
        address[0] = city;
        address[1] = houseNumber;
        address[2] = String.valueOf(postcode);
        address[3] = street;

        //If the address doesn't follow guidelines
        if (address[2].length() > 4) {
            throw new InvalidAddressException(address);
        }
    }

    @Override
    public int compareTo(Node otherNode) {
        return Integer.compare(this.distanceTo, otherNode.distanceTo);
    }


    public void addDestination(Node destination, int distance) {
        adjacentNodes.put(destination, distance);
    }

    public void addAdjacentNode(Node node, int distance) {
        adjacentNodes.put(node, distance);
    }

    //endregion



    //region Getters and setters
    /** Address array where the Node stores the address (if it has one). Remember to check for null-errors! <br>
     *  address[0] = City, fx: "København S"<br>
     *  address[1] = House-number, fx: "2" <br>
     *  address[2] = postcode, fx: "2860" <br>
     *  address[3] = street, fx: "Decembervej"
     */
    public String[] getAddress() { return address; }
    public HashMap<Node, Integer> getAdjacentNodes(){ return adjacentNodes; }
    public double   getX()       { return x; }
    public double   getY()       { return y; }
    public void setDistanceTo(int distanceTo) { this.distanceTo = distanceTo; }
    @Override
    public double[] getBoundingBox() {
        return new double[]{x, y, x, y};
    }
    //endregion
}