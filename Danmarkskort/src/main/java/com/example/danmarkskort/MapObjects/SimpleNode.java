package com.example.danmarkskort.MapObjects;

public class SimpleNode extends Node {
    private final String address;

    /// A SimpleNode is a "fake" Node that only contains a single string being the overwritten address. It also holds a reference to the node that is the actual node
    public SimpleNode(String address) {
        super(0,0);
        this.address = address;
    }

    @Override
    public String getAddress() { return address; }

    @Override
    public String getCity() { return getAddress(); }
}
