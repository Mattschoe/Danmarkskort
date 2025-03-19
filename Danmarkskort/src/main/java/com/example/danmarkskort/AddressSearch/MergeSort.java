package com.example.danmarkskort.AddressSearch;

public class MergeSort {
    Comparable[] inputArray;

    /**
     * Sorts the given inputArray. To get the sorted array, see {@link #getSortedArray()}
     * @param inputArray the unsorted array that needs sorting
     */
    public void sort(Comparable[] inputArray) {
        this.inputArray = inputArray;
        int inputLength = inputArray.length;

        //Base case
        if (inputLength < 2) {
            return;
        }

        //Sorts recursively
        int mid = inputLength/2;
        Comparable[] leftArray = new Comparable[mid];
        Comparable[] rightArray = new Comparable[inputLength - mid];

        for (int i = 0; i < mid; i++) {
            leftArray[i] = inputArray[i];
        }

        for (int i = mid; i < inputLength; i++) {
            rightArray[i - mid] = inputArray[i];
        }

        sort(leftArray);
        sort(rightArray);
        merge(inputArray, leftArray, rightArray);
    }

    private void merge(Comparable[] inputArray, Comparable[] leftArray, Comparable[] rightArray) {
        int leftLength = leftArray.length;
        int rightLength = rightArray.length;
        int i = 0, j = 0, k = 0;

        //Loops through until we either run out of elements on the left or right array
        while (i < leftLength && j < rightLength) {
            if (leftArray[i].compareTo(rightArray[j]) == 0) { //Equal to eachother
                inputArray[k] = leftArray[i];
                i++;
            } else if (leftArray[i].compareTo(rightArray[j]) < 0) { //Right is bigger
                inputArray[k] = rightArray[j];
                j++;
            } else if (leftArray[i].compareTo(rightArray[j]) > 0) { //Left is bigger
                inputArray[k] = leftArray[i];
                i++;
            }
            k++;
        }

        //Cleanup after the while loop
        while (i < leftLength) {
            inputArray[k] = leftArray[i];
            i++;
            k++;
        }
        while (j < rightLength) {
            inputArray[k] = rightArray[j];
            j++;
            k++;
        }
    }

    public Comparable[] getSortedArray() {
        return inputArray;
    }
}
