package com.example.danmarkskort.MapObjects;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

///A Point Of Interest (POI) is a specific point made by the user. It contains the node that's closest to that POI, a coordinate, and a name
public class POI implements MapObject {
    Node closestNodeToPOI;
    Node closestNodeWithRoad;
    float x, y;
    String name;

    /**
     *
     * @param name the name of the POI, usually given by the user
     * @param tile the tile that the POI is located in
     */
    public POI(float x, float y, String name, Tile tile) {
        this.x = x;
        this.y = y;
        this.name = name;
        closestNodeToPOI = findClosestNode(tile);
        closestNodeWithRoad = findClosestNodeWithRoad(tile);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        graphicsContext.setStroke(Color.RED);
        graphicsContext.setLineWidth(0.01);
        graphicsContext.strokeLine(x, y, x, y);
    }

    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }

    ///Finds the Node closest to the POI. The Node HAS to have a full address to be accepted
    private Node findClosestNode(Tile tile) {
        double closestDistance = Double.MAX_VALUE;
        Node closestNode = null;
        for (Node node : tile.getNodesInTile()) {
            if (!node.hasFullAddress()) continue; //Skips if node doesn't have full address
            double nodeX = node.getX();
            double nodeY = node.getY();
            double distance = Math.sqrt(Math.pow((nodeX - (double) x), 2) + Math.pow((nodeY - (double) y), 2)); //Afstandsformlen ser cooked ud i Java wth -MN
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        assert closestNode != null;
        return closestNode;
    }

    ///Finds the closest Node that has a Road connected to it
    private Node findClosestNodeWithRoad(Tile tile) {
        double closestDistance = Double.MAX_VALUE;
        Node closestNode = null;
        for (Node node : tile.getNodesInTile()) {
            if (node.getEdges().isEmpty()) continue; //Skips if node doesnt have any edges.
            double nodeX = node.getX();
            double nodeY = node.getY();
            double distance = Math.sqrt(Math.pow((nodeX - (double) x), 2) + Math.pow((nodeY - (double) y), 2)); //Afstandsformlen ser cooked ud i Java wth -MN
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        assert closestNode != null;
        return closestNode;
    }

    //region getters and setters
    ///Returns the name of this POI
    public String getName() { return name;}
    ///Returns the POIs closest Node
    public Node getClosestNodeToPOI() { return closestNodeToPOI; }
    public Node getClosestNodeWithRoad() { return closestNodeWithRoad; }
    ///Returns the Node's address as a full string. Used for showing to user on UI. If the Node doesn't have a full address, we return the XY
    public String getNodeAddress() {
        return closestNodeToPOI.getAddress();
    }
    public float getX() { return x; }
    public float getY() { return y; }
    //endregion
}
