package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.*;

public class Search {
    ///A list of streets (no duplicates) that the parser has parsed
    private Street[] streets;

    /**
     * Saves all nodes in their given street via a sorted list of {@link Street}'s. Get this list via {@link #getStreets()} and search for a given street via a search algorithm
     * @param nodesWithStreetAddresses all nodes found with a street address. This is calculated in {@code Model}
     */
    public Search(Set<Node> nodesWithStreetAddresses) {
        //Gets all street names and saves them in a set
        Set<String> streetNames = initializeAllStreetNames(nodesWithStreetAddresses);
        streets = new Street[streetNames.size()];

        //Creates all the streets and puts them in the streets array
        int i = 0;
        for (String street : streetNames) {
            streets[i] = new Street(street);
            i++;
        }

        //Sorts array and puts all nodes in the respectively Street
        sortAddresses();
        putNodeInStreets(nodesWithStreetAddresses);
    }

    /**
     * Calculates all the special street-names that exists in the set of nodes provided in the constructor.
     * @param nodes given in constructor
     * @return a Set of strings of streetnames
     */
    private Set<String> initializeAllStreetNames(Collection<Node> nodes) {
        Set<String> result = new HashSet<>();
        for (Node node : nodes) {
            result.add(node.getAddress()[3]);
        }
        return result;
    }

    /**
     * Sorts the addresses using mergesort and changes the value of the {@code streets} array to a sorted one
     */
    private void sortAddresses() {
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort(streets);
        streets = mergeSort.getSortedArray();
        System.out.println(streets.length);
    }

    /**
     * Puts the {@link Node} in the relevant address
     */
    private void putNodeInStreets(Collection<Node> nodes) {
        //This is horrible and should be replaced later with binary search or another optimization algorithm
        for (int i = 0; i < streets.length; i++) {
            for (Node node : nodes) {
                if (streets[i].getStreetName().equals(node.getAddress()[3])) streets[i].addNode(node);
            }
        }

        for (int i = 0; i < streets.length; i++) {
            System.out.println(streets[i].getStreetName());
            for (Node node : streets[i].getNodes()) {
                System.out.println(node.getAddress()[1] + " " + node.getAddress()[0]);
            }
            System.out.println();
        }
    }

    //region getters and setters
    public Street[] getStreets() {
        return streets;
    }
    //endregion
}
