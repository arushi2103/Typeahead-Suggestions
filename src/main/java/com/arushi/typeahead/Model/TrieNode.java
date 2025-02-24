package com.arushi.typeahead.Model;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    public char value;
    public boolean isEndOfWord;
    public int frequency;
    public Map<Character,TrieNode> children;

    public TrieNode(char value){
        this.value=value;
        this.isEndOfWord=false;
        this.frequency=0;
        this.children=new HashMap<>();
    }
}
