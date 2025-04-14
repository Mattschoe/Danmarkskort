package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Line;
import com.example.danmarkskort.MapObjects.Node;
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
            for (Line line : currentNode.getLines()) {
                relax(line, currentNode);
            }
        }
        drawPath();
    }

    private void relax(Line line, Node currentNode) {
        Node oppositeNode = line.getOppositeNode(currentNode);
        int newDistanceTo = (int) (currentNode.getDistanceTo() + line.getWeight());

        if (oppositeNode.getDistanceTo() > newDistanceTo) {
            oppositeNode.setDistanceTo(newDistanceTo);
            cameFrom.put(oppositeNode, currentNode);
            priorityQueue.add(oppositeNode);
        }
    }

    private void drawPath() {
        List<Node> path = new ArrayList<>();

        Node currentNode = endNode;

        while (cameFrom.containsKey(currentNode)) {
            path.add(currentNode);
            currentNode = cameFrom.get(currentNode);
        }
        path.add(currentNode); //Adds the start node since it isn't included in the loop
        Collections.reverse(path);

        for (Node node : path) {
            node.setPartOfRoute(true);
            for (Line line : node.getLines()) {
                if (path.contains(line.getOppositeNode(node))) line.setPartOfRoute(true);
            }
        }


        /* while (currentNode != null) {
            Node currentCameFromNode = cameFrom.get(currentNode); //The node that currentNode came from
            //Finds the line that the currentCameFromNode is part of and draws it
            for (Line line : currentNode.getLines()) {
                if (currentCameFromNode == line.getOppositeNode(currentNode)) line.setPartOfRoute(true);
            }
            currentNode = currentCameFromNode;
        } */
    }
}
