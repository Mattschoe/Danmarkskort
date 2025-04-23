package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Search {
    Node startNode;
    Node endNode;
    List<Road> route;
    private boolean foundRoute;
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();
    }

    /// Start a route from the Node {@code from} to the Node {@code to}.
    public void route(Node from, Node to) {
        route = new ArrayList<>();
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
            if (currentNode == endNode) { //Reached endNode
                System.out.println("Reached EndNode!");
                foundRoute = true;
                break;
            }
            for (Road road : currentNode.getEdges()) {
                relax(road, road.getStartOrEndNodeFromRoad(currentNode));
            }
        }
        if (foundRoute) {
            drawPath(); //Only draws path if we actually found a path.
            Model.getInstance().setLatestRoute(route);
        } else {
            System.out.println("No path found!");
        }
    }

    /// Relaxes the edge. {@code currentNode} HAS to be either the roads start- or endNode, otherwise an error will be thrown.
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

        //Runs through the path. If the current node and the next node in line is equal to the start and endNode of a Road, we set it as part of the route
        Node current = path.getFirst();
        for (int i = 1; i < path.size(); i++) {
            Node next = path.get(i);
            for (Road road : current.getEdges()) {
                if (road.getNodes().contains(next)) {
                    route.add(road);
                    road.setPartOfRoute(true);
                }
            }
            current = next;
        }
    }

    ///Returns route, returns null if haven't found route
    public List<Road> getRoute() { return route; }
}
