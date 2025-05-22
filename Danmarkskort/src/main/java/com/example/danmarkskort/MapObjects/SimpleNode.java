package com.example.danmarkskort.MapObjects;

public class SimpleNode extends Node {
    private final String address;
    private final float x, y;

    /// A SimpleNode is a "fake" Node that only contains a single string being the overwritten address. It also holds a reference to the node that is the actual node
    public SimpleNode(float x, float y, String address) {
        super(-1, -1);
        this.address = address;
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX() { return x; }

    @Override
    public float getY() { return y; }

    @Override
    public String getAddress() { return address; }

    @Override
    public String getCity() { return getAddress(); }
}
