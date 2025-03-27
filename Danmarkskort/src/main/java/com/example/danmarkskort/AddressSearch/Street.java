package com.example.danmarkskort.AddressSearch;


import com.example.danmarkskort.MapObjects.Node;

import java.util.ArrayList;
import java.util.List;

public class Street implements Comparable<Street> {
    //region fields
    private final String streetName;
    private List<Node> nodes;
    //endregion

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
    /**
     * Adds a node to the Street
     * @param node the node that will be associated with this Street
     */
    public void addNode(Node node) { nodes.add(node); }
    /**
     * @return The name of the street. For example "Østerbrogade"
     */
    public String getStreetName() { return streetName; }
    /**
     * @return all the nodes associated with this Street
     */
    public List<Node> getNodes() { return nodes; }
    //endregion

}
