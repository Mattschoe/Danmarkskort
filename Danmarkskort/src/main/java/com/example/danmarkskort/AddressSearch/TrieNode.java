package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;

import java.util.LinkedList;


public class TrieNode {
    Node val;
    LinkedList<Node> values;
    TrieNode[] children = new TrieNode[48];

    TrieNode() {
        values = new LinkedList<>();
    }
    /**
     * Returns value of TrieNode
     * @return value of type Node
     */
    public LinkedList<Node> getValues() {
        return values;
    }

    public Node getValue() {
        return val;
    }

    /**
     * Sets value of TrieNode
     * @param val of type Node
     */
    public void setValue(Node val) {
        if (this.val == null) {
            this.val = val;
        }
        values.add(val);

    }

    /**
     * returns the children of TrieNode
     * @return returns an array of the children of the TrieNode
     */
    public TrieNode[] getChildren() {
        return children;
    }

}
