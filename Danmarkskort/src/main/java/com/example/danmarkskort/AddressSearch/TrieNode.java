package com.example.danmarkskort.AddressSearch;

import java.util.ArrayList;

public class TrieNode {

    ArrayList<TrieNode> children;
    boolean isEndOfWord; // Skal denne bruges??
    char character;

    public TrieNode(char character) {
        this.children = new ArrayList<TrieNode>();
        this.isEndOfWord = false;
        this.character = character;
    }

    public ArrayList<TrieNode> getChildren() {
        return children;
    }

    public char getCharacter() {
        return character;
    }
    /*
    TrieNode[] children;
    boolean isEndOfWord;

    public TrieNode() {
    // Assuming lowercase English letters
    children = new TrieNode[29];
    isEndOfWord = false;
    }
     */
}
