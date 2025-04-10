package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;

import java.util.List;
import java.util.PriorityQueue;

public class PrioityQueue {
    private Node[] priorityQueue;
    private int n = 0;

    public PrioityQueue(int maxAmountOfNodes) {
        priorityQueue = new Node[maxAmountOfNodes+1];
    }

    public void insert(Node node) {
        priorityQueue[n++] = node;
        swim(n);
    }

    public Node getMin() {
        Node min = priorityQueue[1]; //Get max key
        exchange(1, n--); //Exchange with last item
        priorityQueue[n++] = null;
        sink(1); //Restoring heap property
        return min;
    }

    ///Exchanges node at index i with node at index j
    private void exchange(int i, int j) {
        Node temp = priorityQueue[i];
        priorityQueue[i] = priorityQueue[j];
        priorityQueue[j] = temp;
    }

    private void swim(int k) {
        while (k > 1 && less(k/2, k)) {
            exchange(k/2, k);
            k = k/2;
        }
    }

    ///Sinks the node at index k down
    private void sink(int k) {
        while (2*k <= n) {
            int j = 2*k;
            if (j < n && less(j, j+1)) j++;
            if (!less(k,j)) break;
            exchange(k, j);
            k = j;
        }
    }

    ///Compares whether node i is lesser than node j
    private boolean less(int i, int j) {
        return priorityQueue[i].compareTo(priorityQueue[j]) < 0;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }
}
