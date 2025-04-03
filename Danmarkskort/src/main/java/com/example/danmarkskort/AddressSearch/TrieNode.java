package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;
import com.sun.jdi.Value;


public class TrieNode {
    Node val;
    TrieNode[] children = new TrieNode[35];

    /**
     * Returns value of TrieNode
     * @return value of type Node
     */
    public Node getValue() {
        return val;
    }

    /**
     * Sets value of TrieNode
     * @param val value of type Object
     */
    public void setValue(Node val) {
        this.val = val;
    }

    /**
     * returns the children of TrieNode
     * @return returns an array of the children of the TrieNode
     */
    public TrieNode[] getChildren() {
        return children;
    }

}
