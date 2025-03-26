package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;
import com.sun.jdi.Value;


public class TrieNode {
    Object val;
    TrieNode[] children = new TrieNode[29];

    /**
     * Returns value of TrieNode
     * @return value of type Object
     */
    public Object getValue() {
        return val;
    }

    /**
     * Sets value of TrieNode
     * @param val value of type Object
     */
    public void setValue(Object val) {
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
