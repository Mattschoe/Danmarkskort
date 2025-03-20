package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.List;

public class BinarySearch {
    public Node search(Street[] array, String address) {
        int low = 0;
        int high = array.length - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;

            int comparison = array[mid].getStreetName().compareTo(address);
            if (comparison < 0) {
                low = mid + 1;
            } else if (comparison > 0) {
                high = mid - 1;
            } else {
                System.out.println("Im supposed to give a specific node here");
                return array[mid].getNodes().getFirst(); //TEST. TO DO: Her skal den finde den specifikke node
            }
        }
        System.out.println("Im supposed to give a general node here");
        return null; //TEST: TO DO: Her skal den så bare give den første node den kan finde
    }
}
