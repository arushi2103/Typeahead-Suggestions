package com.arushi.typeahead.Controller;

import com.arushi.typeahead.Model.SearchTerm;
import com.arushi.typeahead.Service.SearchService;
import com.arushi.typeahead.Service.TrendingClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")

public class SearchController {
    //This class handles the search API requests.
    private final SearchService searchService;
    @Autowired
    private TrendingClientService trendingClientService;


    //constructor injection
    public SearchController(SearchService searchService){
        this.searchService = searchService;

    }
    // // Search API to get suggestions based on the query.
    @GetMapping
    public Map<String,List<String> > getTrieSuggestions(@RequestParam(value = "query", required = false) String query) {
        //Spring Boot automatically converts the Map into JSON format.
        Map<String, List<String>> response = new HashMap<>();
        response.put("suggestions", searchService.getTrieSuggestions(query));
        return response;
    }

    @GetMapping("/trending")
    public List<String> getTrendingFromMicroservice() {
        return trendingClientService.fetchTrendingTerms();
    }
    @PostMapping("/update")
    public  String updateTrieSearchTerm(@RequestBody SearchTerm searchTerm){
        searchService.updateTrieSearchTerm(searchTerm);
        return "Search term updated & cache cleared successfully!";
    }
}
