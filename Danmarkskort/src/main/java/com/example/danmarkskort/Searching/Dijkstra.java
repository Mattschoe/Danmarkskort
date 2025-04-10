package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;

import java.util.HashSet;
import java.util.Set;

public class Dijkstra{
    private void calculateDistance (Node startNode){
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        startNode.setDistanceTo(0);

        priorityQueue.add(startNode);



        while (!priorityQueue.isEmpty()){
            Node curretnNode = priorityQueue.poll();
            
        }
    }
}

/**
 * getters og setters
 */
