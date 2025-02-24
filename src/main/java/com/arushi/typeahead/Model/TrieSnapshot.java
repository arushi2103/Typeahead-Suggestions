package com.arushi.typeahead.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Trie_snapshot")
public class TrieSnapshot {                 //Each word is stored as a document in MongoDB.

    @Id
    private String term;                    //The word itself is the primary key.
    private int frequency;                  //The frequency is stored for maintaining the ranking.
    public TrieSnapshot(String term, int frequency){
        this.term=term;
        this.frequency=frequency;
    }
}
