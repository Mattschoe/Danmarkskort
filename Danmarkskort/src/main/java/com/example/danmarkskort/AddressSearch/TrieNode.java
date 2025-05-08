package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class TrieNode {
    ///The prefix of this node
    private String prefix;
    ///All the following children of this node
    private final Map<String, TrieNode> children;
    ///All the nodes associated with the full path of the trieNodes
    private final List<Node> nodes;

    TrieNode(String prefix) {
        this.prefix = prefix;
        children = new HashMap<>();
        nodes = new LinkedList<>();
    }

    public String getPrefix() { return prefix;}
    ///Updates the nodes prefix to be the value of the given {@code newPrefix}
    public void updatePrefix(String newPrefix) { this.prefix = newPrefix; }

    public Map<String, TrieNode> getChildren() { return children; }

    ///Returns all the nodes associated with this prefix
    public List<Node> getValues() { return nodes; }

    ///Adds a node to the list of values associated with this nodes prefix
    public void addValue(Node value) { nodes.add(value); }
}
