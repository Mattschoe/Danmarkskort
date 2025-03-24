package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;
import com.sun.jdi.Value;


public class TrieNode {
    Object val;
    TrieNode[] children = new TrieNode[29];

    public Object getValue() {
        return val;
    }

    public void setValue(Object val) {
        this.val = val;
    }

    public TrieNode[] getChildren() {
        return children;
    }
}
