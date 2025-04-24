package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;
import java.util.LinkedList;


public class TrieST<Item> {
    public int R = 49; // Størrelse på alfabet (Radix)
    private TrieNode root; // root of trie

    /**
     * Takes a string key and returns its corresponding value by calling private method {@link #get(TrieNode, String, int)}
     * and if the key called upon does not exist, it returns null.
     * @param word key associated with value
     * @return the corresponding value
     */
    public LinkedList<Node> getList(String word) {
        word = word.toLowerCase();
        TrieNode current = get(root, word, 0);
        if (current == null) return null;
        return current.getValues(); //Dette skal ændres da det er goofy kode
    }

    ///Returns the Node if in tree, gives null if not
    public Node get(String word) {
        word = word.toLowerCase();
        TrieNode current = get(root, word, 0);
        if (current == null) return null;
        return current.getValue();
    }

    /**
     * Runs through the characters of the string word parameter. For each character it recursively
     * looks into the corresponding index in its array of children until the depth of the trie matches the length of
     * the input word or returns null if the TrieMode does not exist
     * @param current TrieNode
     * @param word string key
     * @param depth current level of depth in the trie
     * @return returns the value 'TrieNode' associated with key.
     */
    private TrieNode get(TrieNode current, String word, int depth) { // Return value associated with key in the subtrie rooted at x.
        if (current == null) return null; //Hvis ordet ikke har en tilhørende value
        if (depth == word.length()) {
            return current; //Hvis dybden af træet passer med ordlængde har man fundet den korrekte subtrie
        }
        char c = word.charAt(depth); // Use dth key char to identify subtrie.
        return get(current.getChildren()[charToIndex(c)], word, depth + 1);
    }

    /**
     * Inserts key and value into trie by calling {@link #put(TrieNode, String, Node, int)}
     * @param word key of datatype String
     * @param val value of datatype Object
     */
    public void put(String word, Node val) {
        word = word.toLowerCase();
        root = put(root, word, val, 0);
    }

    /**
     * Inserts key and value into trie by traversing through the Trie. if the TrieNode associated with the character
     * currently looked at doesn't exist it will be instantiated and put into the array of children before making a
     * recursive call making the child TrieNode the current TrieNode and increasing the depth of the tree. The recursive
     * method stops when the depth of the trie matches the length of the key.
     * @param current TrieNode
     * @param word String key
     * @param val Object value
     * @param depth depth of the trie
     * @return returns the TrieNode which the key is now associated to
     */
    private TrieNode put(TrieNode current, String word, Node val, int depth) { // Change value associated with key if in subtrie rooted at x.
        if (current == null) current = new TrieNode(); //Hvis trienoden ikke eksisterer allerede, skaber vi den
        if (depth == word.length()) {
            current.setValue(val);
            return current;
        }
        char c = word.charAt(depth); //Traverser ned af træet med hvert bogstav indtil der enten ikke er flere eller vi har fundet

        current.getChildren()[charToIndex(c)] = put(current.getChildren()[charToIndex(c)], word, val, depth + 1);
        return current;
    }

    /**
     * Returns a LinkedList of all the keys currently in the Trie
     * @return LinkedList<String>
     */
    public LinkedList<String> keys() {
        return keysWithPrefix("");
    }

    /**
     * Takes a prefix and calls {@link #collect(TrieNode, String, LinkedList)} starting from the last TrieNode in the
     * prefix
     * @param prefix Beginning substring of a key
     * @return returns a queue of all keys beginning with the prefix inserted
     */
    public LinkedList<String> keysWithPrefix(String prefix) {
        LinkedList<String> queue = new LinkedList<>();
        collect(get(root,prefix,0),prefix,queue);
        return queue;
    }

    /**
     * Goes through all the descendants of the prefix by making a recursive call, adding all keys with the prefix
     * to a queue.
     * @param current TrieNode corresponding to a character that is currenty being looked at
     * @param prefix substring of a key
     * @param queue queue in which relevant keys is being added to
     */
    private void collect(TrieNode current, String prefix, LinkedList<String> queue) {
        if (current == null) return;
        if (current.getValue() != null) queue.addFirst(prefix);
        for (char c = 'a' ; c < R + 'a'; c++) {
            collect(current.getChildren()[charToIndex(c)],prefix + c, queue); //what the hell er dette -T, Idk man but slay ig -MN
        }
    }

    /**
     * Collects all keys where the length of the keys searched for (pattern) matches and has a value into a queue by traversing
     * through the characters of the pattern searching through its children recursively. if no keys as such exist, returns null
     * @param current TrieNode corresponding to a character that is currenty being looked at
     * @param prefix substring of a key
     * @param pattern String that should match a key
     * @param queue LinkedList with relevant keys
     */
    public void collect(TrieNode current, String prefix, String pattern, LinkedList<String> queue) {
        int d = prefix.length();
        if (current == null) return;
        if (d == pattern.length() && current.getValue() != null) queue.addFirst(prefix);
        if (d == pattern.length()) return;

        char next = pattern.charAt(d);
        for (int i = 0; i < R; i++) {
            if (next == i + 'a') {
                collect(current.getChildren()[i],prefix + next, pattern, queue);
            }
        }
    }

    /**
     * Returns a LinkedList consisting of keys that match the pattern parameter by calling {@link #collect(TrieNode, String, String, LinkedList)}
     * @param pattern String that should match a key
     * @return returns a LinkedList with keys
     */
    public LinkedList<String> keysThatMatch(String pattern) {
        LinkedList<String> queue = new LinkedList<>();
        collect(root, "", pattern, queue);
        return queue;
    }

    ///Converts special character chars to int to prevent giant radix
    private int charToIndex(char c) {
        return switch (c) {
            case 'æ' -> 26;
            case 'ø' -> 27;
            case 'å' -> 28;
            case ' ' -> 29;
            case '.' -> 30;
            case 'ü' -> 31;
            case '-' -> 32;
            case '\'' -> 33;
            case 'é' -> 34;
            case '0' -> 41;
            case '1' -> 35;
            case '2' -> 36;
            case '3' -> 37;
            case '4' -> 38;
            case '5' -> 39;
            case '6' -> 40;
            case '7' -> 41;
            case '8' -> 42;
            case '9' -> 43;
            case 'ä' -> 44;
            case 'ö' -> 45;
            case 'ë' -> 46;
            case '/' -> 47;
            case '(' -> 48;
            case ')' -> 49;
            default -> c - 'a';
        };
    }
}
