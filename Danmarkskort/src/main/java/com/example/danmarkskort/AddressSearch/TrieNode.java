package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;

public class TrieNode {
    int val;
    TrieNode[] children = new TrieNode[29];

    public int getValue() {
        return val;
    }

    public void setValue(int val) {
        this.val = val;
    }

    public TrieNode[] getChildren() {
        return children;
    }
}
