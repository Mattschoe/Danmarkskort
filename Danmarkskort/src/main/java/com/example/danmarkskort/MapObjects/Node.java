package com.example.danmarkskort.MapObjects;

import com.example.danmarkskort.Exceptions.InvalidAddressException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;

public class Node implements Serializable, MapObject {
    @Serial private static final long serialVersionUID = 1444149606229887777L;

    //region Fields
    private float x, y;
    private String city;
    private String houseNumber;
    private short postcode;
    private String street;
    //endregion

    //region Constructor(s)
    /** A {@link Node} is a point in a (x, y) space. {@link Node} calculates the (x, y) point
     *  itself in the {@link #calculateXY} method when being instantiated
     */
    public Node(double latitude, double longitude) {
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
        calculateXY(latitude, longitude);
        this.city = city;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.street = street;
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
    //endregion

    //region Getters and setters
    /** Address array where the Node stores the address (if it has one). Remember to check for null-errors! <br>
     *  address[0] = City, fx: "København S"<br>
     *  address[1] = House-number, fx: "2" <br>
     *  address[2] = postcode, fx: "2860" <br>
     *  address[3] = street, fx: "Decembervej"
     */
    public String getCity() { return city; }
    public short getPostcode() { return postcode; }
    public float getX() { return x; }
    public float getY() { return y; }
    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }
    //endregion
}