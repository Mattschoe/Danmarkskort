package com.example.danmarkskort.AddressSearch;
import com.example.danmarkskort.MapObjects.Node;
import java.util.LinkedList;


public class TrieST<Item> {
    public int R = 29; // Størrelse på alfabet (Radix)
    private TrieNode root; // root of trie
    private final boolean isCity;

    public TrieST(boolean isCity) {
        this.isCity = isCity;

        //Test - skal på sigt indsættes under parsing
        if (isCity) {
            put("Helsinge", new Node(10, 10));
            put("Helsinger", new Node(20, 30));
            put("Herning", new Node(20, 50));
            put("Hobro", new Node(20, 70));
            put("Hjorring", new Node(20, 20));
            put("Horsens", new Node(20, 20));

        } else {
            put("Helsingevej", new Node(20, 21));
            put("Hobrovej", new Node(20, 22));
            put("Hjorringvej", new Node(20, 23));
            put("Horsensvej", new Node(20, 24));
        }
    }


    /**
     * Takes a string key and returns its corresponding value by calling private method {@link #get(TrieNode, String, int)}
     * and if the key called upon does not exist, it returns null.
     * @param word key associated with value
     * @return the corresponding value
     */
    public Object get(String word) {
        TrieNode current = get(root, word, 0);
        if (current == null) return null;
        return current.getValue(); //Dette skal ændres da det er goofy kode
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
        if (depth == word.length())
            return current; //Hvis dybden af træet passer med ordlængde har man fundet den korrekte subtrie
        char c = word.charAt(depth); // Use dth key char to identify subtrie.
        if (c == 'æ') {
            return get(current.getChildren()[26], word, depth + 1);
        } else if (c == 'ø') {
            return get(current.getChildren()[27], word, depth + 1);
        } else if (c == 'å') {
            return get(current.getChildren()[28], word, depth + 1);
        } else if (c == ' ') {
            return get(current.getChildren()[29], word, depth + 1);
        } else {
            return get(current.getChildren()[c - 'a'], word, depth + 1);
        }
    }

    /**
     * Inserts key and value into trie by calling {@link #put(TrieNode, String, Object, int)}
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
        } else if (c == ' ') {
            current.getChildren()[29] = put(current.getChildren()[29], word, val, depth + 1);
        }else {
            current.getChildren()[c - 'a'] = put(current.getChildren()[c - 'a'], word, val, depth + 1); //Sætter bogstav i arrayet
        }

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
            collect(current.getChildren()[c - 'a'],prefix + c, queue);
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
        if (current ==null) return;
        if (d == pattern.length() && current.getValue() != null) queue.addFirst(prefix);
        if (d == pattern.length()) return;

        char next = pattern.charAt(d);
        for (int i = 0; i < R; i++) {
            if (next == '.' || next == i + 'a') {
                collect(current.getChildren()[i],prefix + next, pattern, queue);
            }
        }
    }

    public boolean isCity() {
        return isCity;
    }
}
