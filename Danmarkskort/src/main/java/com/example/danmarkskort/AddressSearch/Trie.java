package com.example.danmarkskort.AddressSearch;

import java.util.ArrayList;
import java.util.HashMap;

public class Trie {
    /* private final TrieNode root;


    public Trie() {
        root = new TrieNode(Character.MIN_VALUE);
    }

    // Insert a word into the Trie
    public void insert(String word) {
        TrieNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                if (current.findChild(ch) == null) { // Hvis noden ikke har et barn tilsvarende bogstavet tilføjes det
                    current.addChild(new TrieNode(ch));
                }
                current = current.findChild(ch);
            }
            current.isEndOfWord = true;
        }

        // Search for a word in the Trie
        public String search(String word) {
            word = word.toLowerCase();
            TrieNode current = root;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);

                if (current.findChild(ch) == null) {
                    // SØG I VEJNAVNE I STEDET!!!
                    return "City not found";
                }
                result.append(ch);
                if (!current.isEndOfWord()&& result.toString().equals(word)) { //dette kan skabe problemer hvis bynavn er en substring af en anden by
                    current = current.findChild(ch);
                }

            }
            if (current.isEndOfWord()) {
                return word;
            } else {
                if (startsWith(result.toString())) { //Hvis der er et ord der starter med det søgt på
                    ArrayList<TrieNode> children = current.getChildren();
                    while (!children.isEmpty()) {
                        children = current.getChildren();
                        if (!children.isEmpty()) {
                            TrieNode child = children.getFirst();
                            result.append
                            if (child.isEndOfWord()) {

                            }
                        }

                    }
                }
                    //Print forslag!!!
            }
            return null;
        }

        //Check if a given prefix exists in the Trie
        public boolean startsWith(String prefix) {
            TrieNode current = root;
            for (int i = 0; i < prefix.length(); i++) {
                char ch = prefix.charAt(i);

                if (current.findChild(ch) == null) {
                    // Prefix not found
                    return false;
                }

                current = current.findChild(ch);
            }
            return true;
        }



        // Main Method
        public static void main(String[] args) {
            Trie trie = new Trie();
            trie.insert("søborg");
            trie.insert("søllerød");
            System.out.print(trie.search("søborg"));

        }
        */
    }


