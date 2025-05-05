package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.util.*;
import java.util.PriorityQueue;

public class Search {
    Node startNode;
    Node endNode;
    List<Road> route;
    private boolean foundRoute;
    PriorityQueue<Node> priorityQueue;
    private Map<Node, Node> cameFrom;
    ///For A*, fScore = node.distanceTo + heuristic(node)
    private TObjectDoubleHashMap<Node> fScore;
    List<Road> endRoads;
    /// den sidste vej inden slutnoden i rutesøgningen
    Road destinationRoad;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();
    }

    /// Start a route from the Node {@code from} to the Node {@code to}.
    public void route(Node from, Node to) {
        route = new ArrayList<>();
        startNode = from.getEdges().getFirst().getStartOrEndNodeFromRoad(from); //Makes sure that we start on a start- or endNode otherwise the algorithm gets stuck in "relax"
        endNode = to;
        endRoads = new ArrayList<>(endNode.getEdges());
        assert startNode != null && endNode != null;

        startNode.setPartOfRoute(true);
        endNode.setPartOfRoute(true);
        
        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        fScore = new TObjectDoubleHashMap<>(); //distanceTo + heuristic(node)
        priorityQueue = new java.util.PriorityQueue<>(Comparator.comparingDouble(fScore::get)); //Retrieves the nodes fScore and uses that in the PQ instead of its "distanceTo" (A*)
        cameFrom = new HashMap<>();

        priorityQueue.add(startNode);
        fScore.put(startNode, heuristic(startNode, endNode));
        int count = 0;
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            count++;
            if (currentNode.equals(endNode)) { //Reached endNode
                System.out.println("Reached EndNode! Route found!");
                foundRoute = true;
                break;
            }
            for (Road road : currentNode.getEdges()) {
                if (road.isDriveable()) relax(road, currentNode); //Relaxes the road if its drivable
            }
        }
        System.out.println("Amount of nodes looked at: " + count);
        if (foundRoute) {
            drawPath(); //Only draws path if we actually found a path.
            Model.getInstance().setLatestRoute(route);
        } else {
            System.out.println("No path found!");
        }
        cleanup();
    }

    /// Relaxes the edge. {@code currentNode} HAS to be either the roads start- or endNode, otherwise an error will be thrown.
    private void relax(Road road, Node currentNode) {
       if (endRoads.contains(road)) {
           this.destinationRoad = road;
           cameFrom.put(endNode, currentNode);
       }

       double newDistanceToNextNode = currentNode.getDistanceTo() + road.getWeight();
       Node nextNode = road.getOppositeNode(currentNode);

       if (nextNode.getDistanceTo() > newDistanceToNextNode) {
            nextNode.setDistanceTo(newDistanceToNextNode);
            cameFrom.put(nextNode, currentNode);
            fScore.put(nextNode, newDistanceToNextNode + heuristic(nextNode, currentNode));

            //Removes node (if in the pq already) and adds it again now with its new fScore
            priorityQueue.remove(nextNode);
            priorityQueue.add(nextNode);
       }
    }

    private void drawPath() {
        List<Node> path = new ArrayList<>();

        //Loops back through the map of nodes until we have a reverse list of route
        Node currentNode = endNode;
        while (cameFrom.containsKey(currentNode)) {
            path.add(currentNode);
           // System.out.println("Tilføjede currentNode til path: " + currentNode.toString());
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
                    if (next.equals(endNode)){
                        List<Node> nodesInNewRoad= new ArrayList<>();
                        nodesInNewRoad.add(current);
                        nodesInNewRoad.add(endNode);
                       Road finalRoad= new Road(nodesInNewRoad,road.isWalkable(), road.isBicycle(),road.isDriveable(), road.getMaxSpeed(), road.getType(), road.getRoadName());
                        route.add(finalRoad);
                        finalRoad.setPartOfRoute(true);
                    } else{
                        route.add(road);
                        road.setPartOfRoute(true);
                    }
                }
            }
            current = next;
        }
    }

    ///Cleans up the mapObjects used in the search so they are ready for another search
    public void cleanup() {
        for (Node node : cameFrom.keySet()) {
            node.setPartOfRoute(false);
            node.setDistanceTo(Double.MAX_VALUE);
        }
    }

    ///Returns route, returns null if haven't found route
    public List<Road> getRoute() { return route; }

    ///The heuristic that's added on top of a Node's {@code distanceTo} to implement A*
    private double heuristic(Node a, Node b) {
        //Euclidean distance
        return Math.hypot(Math.pow(a.getX() - b.getX(), 2), Math.pow(a.getY() - b.getY(), 2));
    }
}
