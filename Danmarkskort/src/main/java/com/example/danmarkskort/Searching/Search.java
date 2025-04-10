package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Line;
import com.example.danmarkskort.MapObjects.Node;
import javafx.scene.canvas.GraphicsContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class Search {
    Node startNode;
    Node endNode;
    GraphicsContext graphicsContext;
    PriorityQueue priorityQueue;

    public Search(Node startNode, Node endNode, Collection<Node> nodes, GraphicsContext graphicsContext) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.graphicsContext = graphicsContext;
        assert startNode != null && endNode != null && !nodes.isEmpty() && graphicsContext != null;
        priorityQueue = new PriorityQueue(nodes.size());
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
                System.out.println("Looking at: " + line);
                line.drawAsRoute(graphicsContext);
                relax(line, currentNode);
            }
        }
    }

    private void relax(Line line, Node currentNode) {
        Node oppositeNode = line.getOppositeNode(currentNode);
        if (oppositeNode.getDistanceTo() > currentNode.getDistanceTo() + line.getWeight()) {
            oppositeNode.setDistanceTo((int) (currentNode.getDistanceTo() + line.getWeight()));
            priorityQueue.insert(oppositeNode);
        }
    }
}
