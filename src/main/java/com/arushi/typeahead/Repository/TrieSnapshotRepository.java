package com.arushi.typeahead.Repository;

import com.arushi.typeahead.Model.TrieSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//This lets us save and retrieve the Trie snapshot from MongoDB.
public interface TrieSnapshotRepository extends MongoRepository<TrieSnapshot,String> {
    List<TrieSnapshot> findAll();
}
