package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import javafx.scene.canvas.GraphicsContext;

import java.sql.SQLOutput;
import java.util.*;
import java.util.PriorityQueue;

public class Search {
    Node startNode;
    Node endNode;
    Road route;
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();
    }

    /**
     * Start a route from the Node {@code from} to the Node {@code to}.
     * @return Road that is the route between the Node {@code from} and {@code to}
     */
    public Road route(Node from, Node to) {
        startNode = from;
        endNode = to;
        assert startNode != null && endNode != null;

        startNode.setPartOfRoute(true);
        endNode.setPartOfRoute(true);

        startNode.setDistanceTo(0);
        findPath();

        if (route == null) throw new RuntimeException("Couldn't find a route!");
        else return route;
    }

    private void findPath() {
        priorityQueue = new java.util.PriorityQueue<>();
        cameFrom = new HashMap<>();

        priorityQueue.add(startNode);
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            System.out.println("Is at: " + currentNode);
            if (currentNode == endNode) { //Reached endNode
                System.out.println("Reached EndNode!");
                drawPath(); //Only draws path if we actually found a path.
                break;
            }
            for (Road road : currentNode.getEdges()) {
                relax(road, road.getStartOrEndNodeFromRoad(currentNode));
            }
        }
    }

    ///Relaxes the edge. {@code currentNode} HAS to be either the roads start- or endNode, otherwise an error will be thrown.
    private void relax(Road road, Node currentNode) {
        double newDistanceTo = currentNode.getDistanceTo() + road.getWeight();
        Node nextNode = road.getOppositeNode(currentNode);
        if (nextNode.getDistanceTo() > newDistanceTo) {
            nextNode.setDistanceTo(newDistanceTo);
            cameFrom.put(nextNode, currentNode);
            priorityQueue.add(nextNode);
        }
    }

    private void drawPath() {
        List<Node> path = new ArrayList<>();

        //Loops back through the map of nodes until we have a reverse list of route
        Node currentNode = endNode;
        while (cameFrom.containsKey(currentNode)) {
            path.add(currentNode);
            currentNode = cameFrom.get(currentNode);
        }
        path.add(currentNode); //Adds the start node since it isn't included in the loop
        Collections.reverse(path);

        route = new Road(path, false, false, true, "Route", "Route");
        route.setPartOfRoute(true);
    }
}
