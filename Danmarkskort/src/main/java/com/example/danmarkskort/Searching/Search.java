package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.PriorityQueue;

public class Search {
    Node startNode;
    Node endNode;
    GraphicsContext graphicsContext;
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Node startNode, Node endNode, Collection<Node> nodes, GraphicsContext graphicsContext) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.graphicsContext = graphicsContext;
        assert startNode != null && endNode != null && !nodes.isEmpty() && graphicsContext != null;

        startNode.setPartOfRoute(true);
        endNode.setPartOfRoute(true);
        priorityQueue = new java.util.PriorityQueue<>();
        cameFrom = new HashMap<>();
        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        priorityQueue.add(startNode);
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            if (currentNode == endNode) {
                System.out.println("Reached endNode!");
                break;
            }
            for (Road road : currentNode.getRoads()) {
                relax(road, currentNode);
            }
        }
        drawPath();
    }

    private void relax(Road road, Node currentNode) {
        int newDistanceTo = (int) (currentNode.getDistanceTo() + road.getWeight());

        Node nextNode = road.getNext(currentNode);
        if (nextNode == null) return;
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

        for (int i = 0; i < path.size(); i++) {
            currentNode = path.get(i);
            for (Road road : currentNode.getRoads())   {
                if (path.contains(road.getNext(currentNode))) road.setPartOfRoute(true);
            }
        }
    }
}
