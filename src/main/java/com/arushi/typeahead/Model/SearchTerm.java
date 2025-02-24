package com.arushi.typeahead.Model;

import lombok.Data; ;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data                                        // @Data: Lombok annotation to create all the getters, setters, equals, hash, and toString methods, based on the fields.
@Document(collection = "SearchTerm")        // @Document: Marks this class as a MongoDB collection.
public class SearchTerm {
    @Id                                     // @Id: Marks this field as the primary key.
    private String term;
    private int frequency;
}
