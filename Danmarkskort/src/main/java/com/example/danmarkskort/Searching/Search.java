package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import javafx.scene.canvas.GraphicsContext;

import java.util.*;
import java.util.PriorityQueue;

public class Search {
    Node startNode;
    Node endNode;
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();

        priorityQueue = new java.util.PriorityQueue<>();
        cameFrom = new HashMap<>();
    }

    ///Start a route from the Node {@code from} to the Node {@code to}
    public void route(Node from, Node to) {
        startNode = from;
        endNode = to;
        assert startNode != null && endNode != null;

        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        priorityQueue.add(startNode);
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            if (currentNode == endNode) break; //Reached endNode
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
