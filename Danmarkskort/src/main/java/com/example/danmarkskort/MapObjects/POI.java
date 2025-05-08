package com.example.danmarkskort.MapObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class POI implements MapObject {
    Node closestNodeToPOI;
    Node closestNodeWithRoad;
    float x, y;
    String name;

    @SuppressWarnings("DataFlowIssue")
    private static final Image pinImage = new Image(
        POI.class.getResource("/com/example/danmarkskort/pin.png").toExternalForm()
    );

    /** A Point Of Interest (POI) is a specific point made by the user. It contains the address node that's closest to that POI, a node that has a road closest to the POI, a coordinate, and a name
     * @param name the name of the POI, given by the user
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
        double size = 0.06;
        graphicsContext.drawImage(pinImage, x - size / 2, y - size, size, size);
    }

    @Override
    public float[] getBoundingBox() {
        return new float[]{x, y, x, y};
    }

    /// Finds the Node closest to the POI. The Node HAS to have a full address to be accepted
    private Node findClosestNode(Tile tile) {
        double closestDistance = Double.MAX_VALUE;
        Node closestNode = null;
        for (Node node : tile.getNodesInTile()) {
            if (!node.hasFullAddress()) continue; //Skips if node doesn't have full address

            double distance = Math.hypot((node.getX() - x), (node.getY() - y));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        assert closestNode != null;
        return closestNode;
    }

    /// Finds the closest Node that has a Road connected to it
    private Node findClosestNodeWithRoad(Tile tile) {
        double closestDistance = Double.MAX_VALUE;
        Node closestNode = null;

        for (Node node : tile.getNodesInTile()) {
            if (!node.hasDrivableEdges()) continue; //Skips if node doesn't have any edges that are drivable.

            double distance = Math.hypot((node.getX() - x), (node.getY() - y));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        assert closestNode != null;
        return closestNode;
    }

    //region Getters and setters
    /// Returns the name of this POI
    public String getName() { return name;}
    public Node getClosestNodeWithRoad() { return closestNodeWithRoad; }

    /// Returns the Node's address as a full string. Used for showing to user on UI. If the Node doesn't have a full address, we return the XY
    public String getNodeAddress() { return closestNodeToPOI.getAddress(); }
    public float getX() { return x; }
    public float getY() { return y; }
    //endregion
}
