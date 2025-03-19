package com.example.danmarkskort.AddressSearch;

public class Trie {

    private final TrieNode root;

    // Constructor
    public Trie() {
            root = new TrieNode();
    }

    // Insert a word into the Trie
    public void insert(String word) {
        TrieNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                if (current.children[ch - 'a'] == null) {
                    current.children[ch - 'a'] = new TrieNode();
                }
                current = current.children[ch - 'a'];
            }
            current.isEndOfWord = true;
        }

        // Search for a word in the Trie
        public boolean search(String word) {
            TrieNode current = root;

            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);

                if (current.children[ch - 'a'] == null) {
                    // SØG I VEJNAVNE I STEDET!!!
                    return false;
                }
                current = current.children[ch - 'a'];
            }
            return current != null && current.isEndOfWord; //Dette skal ændres
        }

        // Check if a given prefix exists in the Trie
        public boolean startsWith(String prefix) {
            TrieNode current = root;
            for (int i = 0; i < prefix.length(); i++) {
                char ch = prefix.charAt(i);

                if (current.children[ch - 'a'] == null) {
                    // Prefix not found
                    return false;
                }

                current = current.children[ch - 'a'];
            }
            return true;
        }

        // Main Method
        public static void main(String[] args) {
            Trie trie = new Trie();
            trie.insert("hello");
            trie.insert("world");

            // Demonstrate search and startsWith
            System.out.println(trie.search("hello"));
            System.out.println(trie.search("world"));
            System.out.println(trie.search("hi"));
            System.out.println(trie.startsWith("hell"));
        }
    }


