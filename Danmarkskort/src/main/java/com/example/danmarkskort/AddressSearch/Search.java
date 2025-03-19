package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.*;

public class Search {
    Street[] streets;

    public Search(Map<Long, Node> unsortedNodes) {
        Set<String> streetNames = initializeAllStreetNames(unsortedNodes.values());
        streets = new Street[streetNames.size()];

        //Creates all the streets and puts them in the streets array
        int i = 0;
        for (String street : streetNames) {
            streets[i] = new Street(street);
            i++;
        }

        //Sorts array and puts all nodes in the respectively array
        sortAddresses();
        putNodeInStreets();
    }

    /**
     * Calculates all the special street-names that exists in the set of nodes provided in the constructor.
     * @param nodes given in constructor
     * @return a Set of strings of streetnames
     */
    private Set<String> initializeAllStreetNames(Collection<Node> nodes) {
        Set<String> result = new HashSet<>();
        for (Node node : nodes) {
            try {
                result.add(node.getAddress()[3]);
            } catch (NullPointerException e) { //Node doesn't have an address

            }
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

        //TESTING
        for (int i = 0; i < streets.length; i++) {
            System.out.println(streets[i]);
        }
    }

    /**
     * Puts the {@link Node} in the relevant address
     */
    private void putNodeInStreets() {

    }
}
