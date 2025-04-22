package com.example.danmarkskort.Searching;

import com.example.danmarkskort.MapObjects.Node;

public class PriorityQueue {
    private Node[] array;
    private int n;

    public PriorityQueue(int capacity) {
        array = new Node[capacity];
    }

    ///If priority in node is larger than parents priority we swim it up
    private void swim(int k) {
        while (k > 1 && less(k/2, k)) {
            exchange(k, k/2);
            k = k/2;
        }
    }

    ///Priority of node becomes smaller than one of its children's priority
    private void sink(int k) {
        while (2 * k <= n) {
            int j = 2*k;
            if (j < n && less(j, j+1)) j++; //Children of node k will always be stored at 2*k and 2*k+1
            if (!less(k, j)) break;
            exchange(k, j);
            k = j;
        }
    }

    ///Adds node at the bottom level of the heap and then swims it up
    public void insert(Node node) {
        n++;
        array[n] = node;
        swim(n);
    }

    /**
     * Exchanges the location of the highest priority node with the lowest, removes it from heap and then sinks down the exchanged node
     * @return the highest priority node
     */
    public Node getPriority() {
        Node priority = array[1];
        exchange(1, n);
        n--;
        sink(1);
        array[n+1] = null;
        return priority;
    }

    ///Compares which node is less than the other
    private boolean less(int i, int j) {
        return array[i].compareTo(array[j]) > 0;
    }

    ///Exchanges node at location i with node at location j.
    private void exchange(int i, int j) {
        Node temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }


    public boolean isEmpty() { return n == 0; }
}
