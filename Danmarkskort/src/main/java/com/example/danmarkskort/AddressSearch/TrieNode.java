package com.example.danmarkskort.AddressSearch;

public class TrieNode {

    TrieNode[] children;
    boolean isEndOfWord;

    public TrieNode() {
    // Assuming lowercase English letters
    children = new TrieNode[26];
    isEndOfWord = false;
    }

}
