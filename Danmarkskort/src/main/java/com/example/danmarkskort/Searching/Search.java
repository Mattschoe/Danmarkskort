package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MVC.Model;
import com.example.danmarkskort.MapObjects.Node;
import com.example.danmarkskort.MapObjects.Road;
import com.example.danmarkskort.MapObjects.Tile;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.sql.SQLOutput;
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
    ///The list of roads that contain endNode. Used for ending the search early to make sure we find the endNode even if it cant be found by .getOppositeNode()
    Road destinationRoad;
    ///True = QuickestRoute <br> False = ShortestRoute <br> True as default
    boolean quickestRoute;

    public Search(Collection<Node> nodes) {
        assert !nodes.isEmpty();
        quickestRoute = true; 
    }

    /// Start a route from the Node {@code from} to the Node {@code to}.
    public void route(Node from, Node to) {
        route = new ArrayList<>();
        startNode = from.getEdges().getFirst().getStartOrEndNodeFromRoad(from); //Makes sure that we start on a start- or endNode otherwise the algorithm gets stuck in "relax"
        endNode = to;
        endRoads = new ArrayList<>(endNode.getEdges());
        assert startNode != null && endNode != null;
        if (startNode.equals(endNode)) {
            System.out.println("Start and End is same!");
            return;
        }

        startNode.setPartOfRoute(true);
        endNode.setPartOfRoute(true);
        
        startNode.setDistanceTo(0);
        findPath();
    }

    private void findPath() {
        fScore = new TObjectDoubleHashMap<>(); //distanceTo + heuristic(node)
        priorityQueue = new java.util.PriorityQueue<>(Comparator.comparingDouble(fScore::get)); //Retrieves the nodes fScore and uses that in the PQ instead of its "distanceTo" (A*)
        cameFrom = new HashMap<>();
        HashSet<Node> closedNodes = new HashSet<>(); //When we're certain we have the lowest distanceTo this node we add it to this set to avoid relaxing the same roads

        priorityQueue.add(startNode);
        fScore.put(startNode, heuristic(startNode, endNode));
        int count = 0;
        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            closedNodes.add(currentNode);
            count++;
            if (currentNode.equals(endNode)) { //Reached endNode
                System.out.println("Reached EndNode! Route found!");
                foundRoute = true;
                break;
            }
            for (Road road : currentNode.getEdges()) {
                if (closedNodes.contains(road.getOppositeNode(currentNode))) continue; //If we have already looked at the node and relaxed its edges we skip it (This avoids relooking at nodes)
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
           //We found a road connected to the endNode so we put it in the PQ with lowest value so its the next we pop and therefore end on
           this.destinationRoad = road;
           cameFrom.put(endNode, currentNode);
           fScore.put(endNode, 0.0);
           priorityQueue.add(endNode);
       }

       double newDistanceToNextNode = currentNode.getDistanceTo() + road.getWeight();
        Node nextNode;
       if (road.isOneway()) nextNode = road.getEndNode();
       else nextNode = road.getOppositeNode(currentNode);

       if (nextNode.getDistanceTo() > newDistanceToNextNode) {
            nextNode.setDistanceTo(newDistanceToNextNode);
            cameFrom.put(nextNode, currentNode);
            fScore.put(nextNode, newDistanceToNextNode + heuristic(nextNode, endNode));

            //Removes node (if in the pq already) and adds it again now with its new fScore
            priorityQueue.remove(nextNode);
            priorityQueue.add(nextNode);
       }
    }

    private void drawPath() {
        List<Node> path = new ArrayList<>();

        //Loops back through the map of nodes until we have a reverse list of route
        for (Node current = endNode; current != null; current = cameFrom.get(current)) {
            path.add(current);
        }
        Collections.reverse(path);

        //Makes a new road between the endNode and the next in the path, this road is always a subset of "destinationRoad"
        int from = destinationRoad.getNodes().indexOf(cameFrom.get(endNode));
        int to = destinationRoad.getNodes().indexOf(endNode);
        if (from > to) { //Reverses if indexposition is off
            int temp = from;
            from = to;
            to = temp;
        }
        List<Node> newRoadNodes = destinationRoad.getNodes().subList(from, to + 1);
        Road newRoad = new Road(newRoadNodes, destinationRoad.isWalkable(), destinationRoad.isBicycle(), destinationRoad.isDriveable(), destinationRoad.isOneway(), destinationRoad.getMaxSpeed(), destinationRoad.getType(), destinationRoad.getRoadName());
        newRoad.setPartOfRoute(true);
        route.add(newRoad);

        //Runs through the path. If the current node and the next node in line is equal to the start and endNode of a Road, we set it as part of the route
        Node current = path.getFirst();
        for (int i = 1; i < path.size(); i++) {
            Node next = path.get(i);
            List<Road> currentRoads = new ArrayList<>();
            for (Road road : current.getEdges()) { //Runs through all edges of the path
                if (road.equals(destinationRoad)) continue; //Skips the last road since we made "newRoad" earlier to represent the last road
                if (road.getOppositeNode(current).equals(next)) { //Else we add those that are linked by the current node and next node
                    currentRoads.add(road);
                }
            }
            if (!currentRoads.isEmpty()) {
                //Goes through all roads that are eligible for being part of the route and chooses the one with the smallest weight
                Road roadWithSmallestWeight = currentRoads.getFirst();
                for (int j = 1; j < currentRoads.size(); j++) {
                    if (currentRoads.get(j).getWeight() < roadWithSmallestWeight.getWeight()) roadWithSmallestWeight = currentRoads.get(j);
                }
                roadWithSmallestWeight.setPartOfRoute(true);
                route.add(roadWithSmallestWeight);
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
        if (quickestRoute) return Math.hypot((a.getX() - b.getX()), (a.getY() - b.getY())) / 130; //Distance formula divided by speedlimit in DK
        else return Math.hypot((a.getX() - b.getX()), (a.getY() - b.getY())); //Just pure distance if doing shortest path
    }

    /**
     * Changes the settings on the route
     * @param quickestRoute <br> </be>- True = Quickest <br> - False = Shortest
     */
    public void changeRouteSettings(boolean quickestRoute) {
        this.quickestRoute = quickestRoute;
    }

    public boolean isQuickestRoute() { return quickestRoute; }// for testing
}
