package com.arushi.typeahead.Model;

import lombok.AllArgsConstructor;
import lombok.Data; ;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data// @Data: Lombok annotation to create all the getters, setters, equals, hash, and toString methods, based on the fields.
@AllArgsConstructor// @AllArgsConstructor: Lombok annotation to create a constructor with all the fields in the class.
@Document(collection = "SearchTerm")        // @Document: Marks this class as a MongoDB collection.
public class SearchTerm {
    @Id                                     // @Id: Marks this field as the primary key.
    private String term;
    private int frequency;
    private Instant lastSearched = Instant.now(); // Instant.now(): Returns the current date-time in UTC.
//    // ✅ Add explicit constructor (if Lombok is not used)
//    public SearchTerm(String term, int frequency, Instant lastSearched) {
//        this.term = term;
//        this.frequency = frequency;
//        this.lastSearched = lastSearched;
//    }
}
