package com.example.danmarkskort.AddressSearch;

import com.example.danmarkskort.MapObjects.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class RadixTrie {
    private final TrieNode root;

    ///A radix tree holds all {@link Node}'s with addresses.
    public RadixTrie() {
        root = new TrieNode("");
    }

    ///Puts a Node into the tree
    public void put(Node node) {
        TrieNode current = root;
        String remaining = node.getAddress().toLowerCase();

        while (!remaining.isEmpty()) {
            boolean matched = false;
            Map<String, TrieNode> childrenOfCurrent = current.getChildren();
            for (String child : childrenOfCurrent.keySet()) {
                TrieNode childNode = childrenOfCurrent.get(child);
                int sharedPrefix = commonPrefixLength(child, remaining);

                if (sharedPrefix > 0) { //At least 1 common char match
                    if (sharedPrefix == child.length()) {
                        //Full match prefix so we add to the prefix
                        current = childNode;
                    } else {
                        //Partial match (Some of the string is shared (Fx: "Nyv..." and "Nyb...")
                        //Intermediate splitNode for the shared-prefix
                        TrieNode splitNode = new TrieNode(child.substring(0, sharedPrefix)); //Node's prefix is until we hit the part where the two nodes don't share prefix anymore. From here we add the nodes to the new split
                        childrenOfCurrent.remove(child); //Removes old "dominant" node
                        childrenOfCurrent.put(splitNode.getPrefix(), splitNode); //Adds the splitNode so we can split from here

                        //The prefix of the old node now is at the split of the common prefix until the end of the string
                        childNode.updatePrefix(child.substring(sharedPrefix));
                        splitNode.getChildren().put(childNode.getPrefix(), childNode); //Adds node back in at new split

                        current = splitNode;
                    }
                    remaining = remaining.substring(sharedPrefix); //Rest of string is original string minus shared prefix (Fx: Same street but different house number)
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                //No shared prefix at all so we add one
                TrieNode newChild = new TrieNode(remaining);
                newChild.addValue(node);
                current.getChildren().put(remaining, newChild);
                return;
            }
        }

        //Cleanup, we now reached the currentNode that should hold this node, so we add it to its values
        current.addValue(node);
    }

    ///Returns a list of nodes that all start with the given {@code prefix}
    public List<Node> keysWithPrefix(String prefix) {
        return collectWithPrefix(root, prefix);
    }

    ///Recursively runs down the tree returning only either what fully matches or has children that match (Fx: "København S" only returns all under that prefix and not under "København")
    private List<Node> collectWithPrefix(TrieNode current, String remaining) {
        List<Node> result = new ArrayList<>();

        for (String child : current.getChildren().keySet()) {
            TrieNode childNode = current.getChildren().get(child);
            int sharedPrefix = commonPrefixLength(child, remaining);

            if (sharedPrefix == 0) continue; //No shared prefix so we don't look at this child

            if (sharedPrefix == remaining.length()) {
                //Full match so we return everything (Fx: "København" returns everything under the node "København ..."
                result.addAll(collectAll(childNode));
            } else if (sharedPrefix == child.length() && remaining.length() > child.length()) {
                //Full match but the user input has more (Fx: "København S", gets all under "København S" and not "København")
                result.addAll(collectWithPrefix(childNode, remaining.substring(sharedPrefix)));
            }
        }
        return result;
    }

    ///Given a {@code subtree} it collects all value nodes in the subtree
    private List<Node> collectAll(TrieNode subtree) {
        List<Node> result = new ArrayList<>();

        //If subtree has any value we add it to the list
        if (!subtree.getValues().isEmpty()) result.addAll(subtree.getValues());

        //Recursively goes deeper into every node and also adds their value to the final List of nodes
        for (TrieNode child : subtree.getChildren().values()) {
            result.addAll(collectAll(child));
        }
        return result;
    }

    ///Compares the two strings prefixes and counts how many chars they share. Returns the charAt index position at which they break
    private int commonPrefixLength(String a, String b) {
        int length = Math.min(a.length(), b.length());
        int i = 0;
        while (i < length && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }
}
