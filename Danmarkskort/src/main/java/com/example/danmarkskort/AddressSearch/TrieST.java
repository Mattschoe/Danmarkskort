package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;


import java.util.LinkedList;


public class TrieST<Item> {
    public int R = 29; // Størrelse på alfabet (Radix)
    private TrieNode root; // root of trie

    /**
     * Takes a string and returns its corresponding value by calling {@link #get(TrieNode, String, int)}
     * @param word
     * @return
     */
    public Object get(String word) {
        TrieNode current = get(root, word, 0);
        if (current == null) return null; //EVT SKAL MATTHIAS' KODE AKTIVERES HER
        return current.getValue(); //Dette skal ændres da det er goofy kode
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

    public void put(String word, Node val) {
        root = put(root, word, val, 0);
    }

    private TrieNode put(TrieNode current, String word, Object val, int depth) { // Change value associated with key if in subtrie rooted at x.
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

    public LinkedList<String> keys() {
        return keysWithPrefix("");
    }


    public LinkedList<String> keysWithPrefix(String prefix) {
        LinkedList<String> queue = new LinkedList<>();
        collect(get(root,prefix,0),prefix,queue);
        return queue;
    }

    private void collect(TrieNode x, String pre, LinkedList<String> queue) {
        if (x == null) return;
        if (x.getValue() != null) queue.addFirst(pre);
        for (char c = 'a' ; c < R + 'a'; c++) {
            collect(x.getChildren()[c - 'a'],pre + c, queue);
        }
    }

    public LinkedList<String> keysThatMatch(String pattern) {
        LinkedList<String> queue = new LinkedList<>();
        collect(root, "", pattern, queue);
        return queue;
    }

    public void collect(TrieNode x, String pre, String pat, LinkedList<String> queue) {
        int d = pre.length();
        if (x==null) return;
        if (d == pat.length() && x.getValue() != null) queue.addFirst(pre);
        if (d == pat.length()) return;

        char next = pat.charAt(d);
        for (int i = 0; i < R; i++) {
            if (next == '.' || next == i + 'a') {
                collect(x.getChildren()[i],pre + next, pat, queue);
            }
        }
    }

    // Main Test Method
    public static void main(String[] args) {
        TrieST<Node> trie = new TrieST<>();

        trie.put("soborg", new Node(10, 10));
        trie.put("sollerod", new Node(20,20));

        System.out.println(trie.keysThatMatch("so").pollLast());//Null

        for (String s :trie.keysWithPrefix("so")) {
            System.out.println(s);
        }

        System.out.println(trie.keysThatMatch("sollerod").pollLast()); //should work

        for (String s : trie.keysWithPrefix("sollerod")) {
            System.out.println(s);
        }

        System.out.println(trie.keysThatMatch("soborg").pollLast()); //Should work

        for (String s : trie.keysWithPrefix("sob")) {
            System.out.println(s);
        }
    }
}
