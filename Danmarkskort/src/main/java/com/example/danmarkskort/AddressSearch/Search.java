package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.*;

public class Search {
    Street[] streets;

    public Search(Map<Long, Node> unsortedNodes) {
        Set<String> streetNames = initializeAllStreetNames(unsortedNodes.values());
        streets = new Street[streetNames.size()];

        int i = 0;
        for (String street : streetNames) {
            streets[i] = new Street(street);
            i++;
        }
        sortAddresses();
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

    private void sortAddresses() {
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort(streets);

        Street[] sortedArray = mergeSort.getSortedArray();

        //TESTING
        for (int i = 0; i < sortedArray.length; i++) {
            System.out.println(sortedArray[i]);
        }
    }

    /**
     * Puts the {@link Node} in the relevant address
     */
    private void putNodeInStreets() {

    }
}
