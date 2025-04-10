package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;

public class Test {
    public static void main(String[] args) {
        PriorityQueue pq = new PriorityQueue(10);

        Node node1 = new Node(10, 10);
        Node node2 = new Node(20, 20);
        Node node3 = new Node(30, 30);
        Node node4 = new Node(40, 40);
        Node node5 = new Node(50, 50);

        //Sourcenode
        node1.setDistanceTo(0);
        node2.setDistanceTo(10);
        node3.setDistanceTo(7);
        node4.setDistanceTo(20);
        node5.setDistanceTo(9);

        pq.insert(node1);
        pq.insert(node2);
        pq.insert(node3);
        pq.insert(node4);
        pq.insert(node5);
        System.out.println(pq.getPriority().getDistanceTo());
        System.out.println(pq.getPriority().getDistanceTo());
        System.out.println(pq.getPriority().getDistanceTo());
        System.out.println(pq.getPriority().getDistanceTo());
        System.out.println(pq.getPriority().getDistanceTo());
    }

}
