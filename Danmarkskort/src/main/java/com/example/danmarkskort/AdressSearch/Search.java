package com.example.danmarkskort.AdressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Search {
    List<List<Node>> adresses;

    public Search(Map<Long, Node> unsortedNodes) {
        adresses = new ArrayList<>();

        
    }

    /**
     * Initializes all street-names. Goes through all Node's, gets alle special street-names and saves them
     */
    private void initializeAllStreetNames() {
        adresses.add(new ArrayList<>());
    }

}
