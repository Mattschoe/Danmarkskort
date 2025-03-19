package com.example.danmarkskort.AddressSearch;


import com.example.danmarkskort.MapObjects.Node;

import java.util.ArrayList;
import java.util.List;

public class Street implements Comparable<Street> {
    private String streetName;
    private List<Node> nodes;

    /**
     * A Street is a list of nodes related to that street-name
     * @param streetName the name of this street
     */
    public Street(String streetName) {
        this.streetName = streetName;
        nodes = new ArrayList<>();
    }

    @Override
    public int compareTo(Street otherStreet) {
        return otherStreet.getStreetName().compareTo(streetName);
    }

    //region getters and setters
    public void addNode(Node node) { nodes.add(node); }
    public String getStreetName() { return streetName; }
    public List<Node> getNodes() { return nodes; }
    //endregion

}
