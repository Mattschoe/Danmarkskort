package com.example.danmarkskort.AddressSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TrieNode {

    ArrayList<TrieNode> children;
    HashSet<Character> bondosBasement;
    boolean isEndOfWord; // Skal denne bruges??
    char character;

    public TrieNode(char character) {
        this.children = new ArrayList<>();
        this.isEndOfWord = false;
        this.character = character;
    }

    public void addChild(TrieNode child) {
        getChildren().add(child);
    }

    public TrieNode findChild(char character) {
        for (TrieNode child : getChildren()) {
            if (child.getCharacter() == character) {
                return child;
            }
        }
        return null;
    }

    public ArrayList<TrieNode> getChildren() {
        return children;
    }

    public char getCharacter() {
        return character;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
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
