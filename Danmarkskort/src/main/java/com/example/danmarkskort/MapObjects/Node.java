package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Node implements Serializable, MapObject, Comparable<Node> {
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private float x, y;
    private String city;
    private String houseNumber;
    private short postcode;
    private String street;
    private int distanceTo;
    private Set<Road> edges;

    //endregion

    //region Constructor(s)
    /** A {@link Node} is a point in a (x, y) space. {@link Node} calculates the (x, y) point
     *  itself in the {@link #calculateXY} method when being instantiated
     */
    public Node(double latitude, double longitude) {
        distanceTo = Integer.MAX_VALUE;
        edges = new HashSet<>();
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
    public Node(double latitude, double longitude, String city, String houseNumber, short postcode, String street) {
        distanceTo = Integer.MAX_VALUE;
        edges = new HashSet<>();
        this.city = city;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.street = street;
        calculateXY(latitude, longitude);
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
        float xNorm = (float) ((longitude - minLon) / (maxLon - minLon));
        float yNorm = (float) ((latitude - minLat) / (maxLat - minLat));
        x = xNorm * width;
        y = (1 - yNorm) * height; //Makes sure Y isn't mirrored
    }

    public void draw(GraphicsContext graphicsContext) {
    }

    /**Compares the node given as parameter with this node.
     * @return 0 if they have equal distance <br> a value less than zero if this node is less than the other node <br> a value more than zero if this node is greater than the other node
     */
    @Override
    public int compareTo(Node otherNode) {
        return Integer.compare(this.distanceTo, otherNode.distanceTo);
    }
    //endregion

    //region Getters and setters
    public float getX() { return x; }
    public float getY() { return y; }
    public void setDistanceTo(int distanceTo) { this.distanceTo = distanceTo; }
    public int getDistanceTo() { return distanceTo; }
    ///Adds the Road as an edge to this node
    public void addEdge(Road road) { edges.add(road); }
    public Set<Road> getEdges() { return edges; }
    ///Returns whether this node is an intersection or not
    public boolean isIntersection() { return edges.size() > 1; }

    //region Address
    public String getCity() { return city; }
    public short getPostcode() { return postcode; }
    public String getStreet() { return street; }
    public String getHouseNumber() { return houseNumber; }
    ///Combines all other getAddress getters together to one whole string. Useful for UI
    public String getAddress() { return (street + " " + houseNumber + ", " + postcode + " " + city); }
    public boolean hasFullAddress()  { return street != null && houseNumber != null && postcode != 0 && city != null; }
    //endregion

    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }
    //endregion
}