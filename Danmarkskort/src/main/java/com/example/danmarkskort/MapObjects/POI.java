package com.example.danmarkskort.MapObjects;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

///A Point Of Interest (POI) is a specific point made by the user. It contains the node that's closest to that POI, a coordinate, and a name
public class POI implements MapObject {
    Node closestNodeToPOI;
    float x, y;
    String name;

    /**
     *
     * @param name the name of the POI, usually given by the user
     * @param tile the tile that the POI locates in
     */
    public POI(float x, float y, String name, Tile tile) {
        this.x = x;
        this.y = y;
        this.name = name;
        closestNodeToPOI = findClosestNode(tile);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        graphicsContext.setStroke(Color.RED);
        graphicsContext.setLineWidth(0.025);
        graphicsContext.strokeLine(x, y, x, y);
    }

    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }

    private Node findClosestNode(Tile tile) {
        double closestDistance = Double.MAX_VALUE;
        Node closestNode = null;
        for (Node node : tile.getNodesInTile()) {
            float nodeX = node.getX();
            float nodeY = node.getY();
            double distance = Math.sqrt(Math.pow((nodeX - x), 2) + Math.pow((nodeY - x), 2)); //Afstandsformlen ser cooked ud i Java wth -MN
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        assert closestNode != null;
        return closestNode;
    }


}
