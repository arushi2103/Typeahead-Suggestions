package com.arushi.typeahead.Service;

import com.arushi.typeahead.Model.TrieNode;
import com.arushi.typeahead.Model.TrieSnapshot;
import com.arushi.typeahead.Repository.TrieSnapshotRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class TrieService {
    private final TrieNode root;
    private final TrieSnapshotRepository _trieSnapshotRepository;
    public TrieService(TrieSnapshotRepository trieSnapshotRepository){
        this.root=new TrieNode('\0'); // root node with null character.
        this._trieSnapshotRepository=trieSnapshotRepository;
    }
    //Load Trie from database at startup
    @PostConstruct
    public void loadTrieFromDB(){
        List<TrieSnapshot> snapshots=_trieSnapshotRepository.findAll();
        for(TrieSnapshot snapshot:snapshots){
            insert(snapshot.getTerm(),snapshot.getFrequency());
        }
        System.out.println("Loaded trie from database with" + snapshots.size() + " nodes into Trie from DB.");
    }
    //insert a term into the trie
    public void insert (String term, int frequency){
        TrieNode current=root;
        for(char c:term.toLowerCase().toCharArray()){
            current.children.putIfAbsent(c,new TrieNode(c));
            current=current.children.get(c);
        }
        current.isEndOfWord=true;
        current.frequency=frequency;
        System.out.println("Inserted into Trie: " + term + " with frequency: " + frequency);

    }
    //search for top suggestions based on prefix
    public List<String> searchSuggestions(String prefix){
        TrieNode current=root;
        for(char c: prefix.toLowerCase().toCharArray()){
            if (!current.children.containsKey(c)) {
                System.out.println("Prefix not found in Trie: " + prefix);
                return new ArrayList<>(); // No matches found
            }
            current = current.children.get(c);
        }
        System.out.println("Found prefix in Trie: " + prefix);
        return getTopSuggestions(current,prefix);
    }
    //Helper method to find top 10 suggestions
    private List<String>getTopSuggestions(TrieNode node,String prefix){
        List<String>suggestions=new ArrayList<>();
        PriorityQueue<TrieNode> maxHeap=new PriorityQueue<>((a,b)->b.frequency-a.frequency);
        List<String>words=new ArrayList<>();
        traverseTrie(node,prefix,words);
        //sort words by frequency (desc) if available
        words.sort((a,b)->getFrequency(b)-getFrequency(a));
        return words.subList(0,Math.min(words.size(),10));
    }
    //persist trie to DB
    public void saveTrieToDB(){
        List<TrieSnapshot> snapshots=_trieSnapshotRepository.findAll();
        _trieSnapshotRepository.deleteAll();
        traverseTrieAndSave(root,"",snapshots);
        _trieSnapshotRepository.saveAll(snapshots);
        System.out.println(("Trie snapshot saved to database with " + snapshots.size() + " nodes."));
    }

    // DFS to find all words under a given node
    private void traverseTrie(TrieNode node,String word, List<String>words){
        if(node.isEndOfWord){
            words.add(word);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            traverseTrie(entry.getValue(), word + entry.getKey(), words);
        }
    }
    // Recursively traverse Trie and save words
    private void traverseTrieAndSave(TrieNode node, String word, List<TrieSnapshot> snapshots) {
        if (node.isEndOfWord) {
            snapshots.add(new TrieSnapshot(word, node.frequency));
        }
        for (var entry : node.children.entrySet()) {
            traverseTrieAndSave(entry.getValue(), word + entry.getKey(), snapshots);
        }
    }
    private int getFrequency(String word){
        TrieNode current=root;
        for(char c:word.toLowerCase().toCharArray()){
            if (!current.children.containsKey(c)) {
                return 0;
            }
            current = current.children.get(c);
        }
        return current.frequency;
    }

    //Instead of saving after every update, we can periodically save the Trie snapshot to MongoDB every hour.
    @Scheduled(fixedRate=3600000)   //save every hour
    public void scheduledTrieSave(){
        saveTrieToDB();
    }
}
