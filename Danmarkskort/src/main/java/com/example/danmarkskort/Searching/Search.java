package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Line;
import com.example.danmarkskort.MapObjects.Node;
import javafx.scene.canvas.GraphicsContext;

import java.util.*;

public class Search {
    Node startNode;
    Node endNode;
    GraphicsContext graphicsContext;
    PriorityQueue priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Node startNode, Node endNode, Collection<Node> nodes, GraphicsContext graphicsContext) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.graphicsContext = graphicsContext;
        assert startNode != null && endNode != null && !nodes.isEmpty() && graphicsContext != null;
        priorityQueue = new PriorityQueue(nodes.size());
        cameFrom = new HashMap<>();
        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        priorityQueue.insert(startNode);
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.getPriority();
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
            priorityQueue.insert(oppositeNode);
        }
    }

    private void drawPath() {
        Node currentNode = endNode;

        while (currentNode != null) {
            Node currentCameFromNode = cameFrom.get(currentNode); //The node that currentNode came from

            //Finds the line that the currentCameFromNode is part of and draws it
            for (Line line : currentNode.getLines()) {
                if (currentCameFromNode == line.getOppositeNode(currentNode)) line.setPartOfRoute(true);
            }
            currentNode = currentCameFromNode;
        }
    }
}
