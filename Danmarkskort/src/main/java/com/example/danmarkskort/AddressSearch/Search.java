package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.*;

public class Search {
    List<Node>[] adresses;

    public Search(Map<Long, Node> unsortedNodes) {
        Set<String> streetNames = getAllSpecialStreetnames(unsortedNodes.values());
        adresses = new List[streetNames.size()];

        //Initializes array
        for (int i = 0; i < adresses.length; i++) {
            adresses[i] = new ArrayList<>();
        }
    }

    /**
     * Calculates all the special street-names that exists in the set of nodes provided in the constructor.
     * @param nodes given in constructor
     * @return a Set of strings of streetnames
     */
    private Set<String> getAllSpecialStreetnames(Collection<Node> nodes) {
        Set<String> result = new HashSet<>();

        for (Node node : nodes) {
            try {
                result.add(node.getAddress()[3]);
            } catch (NullPointerException e) { //Node doesnt have an address

            }

        }
        return result;
    }

    /**
     * Initializes all street-names. Goes through all Node's, gets alle special street-names and saves them
     */
    private void initializeAllStreetNames() {
    }

    /**
     * Puts the {@Link Node} in the relevant address
     */
    private void putNodeInStreets() {

    }

}
