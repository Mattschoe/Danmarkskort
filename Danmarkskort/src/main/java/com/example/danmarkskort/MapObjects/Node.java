package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Node implements Serializable, MapObject, Comparable<Node> {
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private float x, y;
    private String city;
    private String houseNumber;
    private short postcode;
    private String street;
    private int distanceTo;


    private List<Road> roads;
    private boolean partOfRoute;
    //endregion

    //region Constructor(s)
    /** A {@link Node} is a point in a (x, y) space. {@link Node} calculates the (x, y) point
     *  itself in the {@link #calculateXY} method when being instantiated
     */
    public Node(double latitude, double longitude) {
        distanceTo = Integer.MAX_VALUE;
        roads = new ArrayList<>();
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
        roads = new ArrayList<>();
        calculateXY(latitude, longitude);
        this.city = city;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.street = street;
        if (city.equals("København K") && houseNumber.equals("5") && postcode == 1411 && street.equals("Langebrogade")) System.out.println("Korrekte: " + x + ", " + y);
        if (city.equals("København S") && houseNumber.equals("64") && postcode == 2300 && street.equals("Artillerivej")) System.out.println("Forkerte: " + x + ", " + y);
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
        if (partOfRoute) {
            graphicsContext.setStroke(Color.ORANGE);
            graphicsContext.setLineWidth(0.025);
            graphicsContext.strokeLine(x, y, x, y);
        }
    }


    /**Compares the node given as parameter with this node.
     * @return 0 if they have equal distance <br> a value less than zero if this node is less than the other node <br> a value more than zero if this node is greater than the other node
     */
    @Override
    public int compareTo(Node otherNode) {
        return Integer.compare(this.distanceTo, otherNode.distanceTo);
    }

    ///Adds a road to the roads that are connected to this node
    public void addRoad(Road road) {
        roads.add(road);
    }



    //endregion

    //region Getters and setters
    ///Returns a list of the roads that are connected to this node
    public List<Road> getRoads() { return roads; }
    public float getX() { return x; }
    public float getY() { return y; }
    public void setDistanceTo(int distanceTo) { this.distanceTo = distanceTo; }
    public int getDistanceTo() { return distanceTo; }

    //region Address
    public String getCity() { return city; }
    public short getPostcode() { return postcode; }
    public String getStreet() { return street; }
    public String getHouseNumber() { return houseNumber; }
    ///Combines all other getAddress getters together to one whole string. Useful for UI
    public String getAddress() { return (street + " " + houseNumber + ", " + postcode + " " + city); }
    public boolean hasFullAddress()  { return street != null && houseNumber != null && postcode != 0 && city != null; }
    //Endregion

    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }
    public void setPartOfRoute(boolean partOfRoute) { this.partOfRoute = partOfRoute; }
    public boolean isPartOfRoute() { return partOfRoute; }
    //endregion
}