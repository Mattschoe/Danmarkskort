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
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();
    }

    ///Start a route from the Node {@code from} to the Node {@code to}
    public void route(Node from, Node to) {
        startNode = from;
        endNode = to;
        assert startNode != null && endNode != null;

        startNode.setPartOfRoute(true);
        endNode.setPartOfRoute(true);

        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        priorityQueue = new java.util.PriorityQueue<>();
        cameFrom = new HashMap<>();

        priorityQueue.add(startNode);
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            System.out.println("Is at: " + currentNode);
            if (currentNode == endNode) {
                System.out.println("Reached EndNode!");
                break; //Reached endNode
            }
            for (Road road : currentNode.getEdges()) {
                relax(road, road.getStartOrEndNodeFromRoad(currentNode));
            }
        }
        drawPath();
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
        /*
        List<Node> path = new ArrayList<>();

        //Loops back through the map of nodes until we have a reverse list of route
        Node currentNode = endNode;
        while (cameFrom.containsKey(currentNode)) {
            path.add(currentNode);
            currentNode = cameFrom.get(currentNode);
        }
        path.add(currentNode); //Adds the start node since it isn't included in the loop
        Collections.reverse(path);

        //Runs through every node in path. For every node it checks if the road is fully represented in the path, and if yes, it sets it as part of the route.
        System.out.print("Path: ");
        for (int i = 0; i < path.size(); i++) {
            currentNode = path.get(i);
            System.out.print(currentNode + " -> ");
            for (Road road : currentNode.getEdges())   {
                if (path.contains(road.getOppositeNode(currentNode))) road.setPartOfRoute(true);
            }
        }
        */
    }
}
