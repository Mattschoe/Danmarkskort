package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;

import java.util.LinkedList;
import java.util.Queue;

public class TrieST<Integer> {
    public int R = 29; // Størrelse på alfabet (Radix)
    private TrieNode root; // root of trie

    //Få ordets værdi... hvis -1 er det en fejl
    public int get(String word) {
        TrieNode current = get(root, word, 0);
        if (current == null) return -1; //EVT SKAL MATTHIAS' KODE AKTIVERES HER
        return current.getValue();
    }

    private TrieNode get(TrieNode current, String word, int depth) { // Return value associated with key in the subtrie rooted at x.
        if (current == null) return null; //Hvis ordet ikke har en tilhørende value
        if (depth == word.length())
            return current; //Hvis dybden af træet passer med ordlængde har man fundet den korrekte subtrie
        char c = word.charAt(depth); // Use dth key char to identify subtrie.
        if (c == 'æ') {
            return get(current.getChildren()[26], word, depth + 1);
        } else if (c == 'ø') {
            return get(current.getChildren()[27], word, depth + 1);
        } else if (c == 'å') {
            return get(current.getChildren()[28], word, depth + 1);
        } else {
            return get(current.getChildren()[c - 'a'], word, depth + 1);
        }
    }

    public void put(String word, int val) {
        root = put(root, word, val, 0);
    }

    private TrieNode put(TrieNode current, String word, int val, int depth) { // Change value associated with key if in subtrie rooted at x.
        if (current == null) current = new TrieNode(); //Hvis trienoden ikke eksistere allerede, skab den
        if (depth == word.length()) {
            current.setValue(val);
            return current;
        } //Returnere hvilken Trie hele ordet gemmes i

        char c = word.charAt(depth); // Traversere ned af træet med hvert bogstav indtil der enten ikke er flere eller
        if (c == 'æ') {
            current.getChildren()[26] = put(current.getChildren()[26], word, val, depth + 1);
        } else if (c == 'ø') {
            current.getChildren()[27] = put(current.getChildren()[27], word, val, depth + 1);
        } else if (c == 'å') {
            current.getChildren()[28] = put(current.getChildren()[28], word, val, depth + 1);
        } else {
            current.getChildren()[c - 'a'] = put(current.getChildren()[c - 'a'], word, val, depth + 1); //Sætter bogstav i arrayet
        }

        return current;
    }

    public LinkedQueue keys() {
        return keysWithPrefix("");
    }

    public LinkedQueue keysWithPrefix(String prefix) {
        LinkedQueue queue = new LinkedQueue();
        collect(get(root,prefix,0),prefix,queue);
        return queue;
    }

    private void collect(TrieNode x, String pre, LinkedQueue queue) {
        if (x == null) return;
        if (x.getValue() != 0) queue.enqueue(pre);
        for (char c = 0 ; c < R; c++) {
            collect(x.getChildren()[c],pre + c, queue);
        }
    }

    public LinkedQueue keysThatMatch(String pattern) {
        LinkedQueue queue = new LinkedQueue();
        collect(root, "", pattern, queue);
        return queue;
    }

    public void collect(TrieNode x, String pre, String pat, LinkedQueue queue) {
        int d = pre.length();
        if (x==null) return;
        if (d == pat.length() && x.getValue() != 0) queue.enqueue(pre);
        if (d == pat.length()) return;

        char next = pat.charAt(d);
        for (char c = 0; c < R; c++) {
            if (next == '.' || next == c) {
                collect(x.getChildren()[c],pre + c, pat, queue);
            }
        }
    }

    // Main Test Method
    public static void main(String[] args) {
        TrieST trie = new TrieST();

        trie.put("soborg", 10);
        trie.put("sollerod", 3);

        trie.keysThatMatch("so").dequeue();
        trie.keysThatMatch("so").dequeue();
        trie.keysThatMatch("sollerod").dequeue();
        trie.keysThatMatch("sollerod").dequeue();
        trie.keysThatMatch("soborg").dequeue();
        trie.keysThatMatch("so").dequeue();


    }
}
