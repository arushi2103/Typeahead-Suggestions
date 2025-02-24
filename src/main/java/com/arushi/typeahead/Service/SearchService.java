package com.arushi.typeahead.Service;

import com.arushi.typeahead.Model.SearchTerm;
import com.arushi.typeahead.Repository.SearchRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private final SearchRepository searchRepository;
    private final TrieService trieService;
    private final CacheService cacheService;

    //Constructor injection
    public  SearchService(SearchRepository searchRepository, TrieService trieService, CacheService cacheService){
        this.searchRepository = searchRepository;
        this.trieService = trieService;
        this.cacheService = cacheService;
    }
    //Methods
    public List<SearchTerm> getSuggestion(String prefix){
        //getSuggestions() fetches top search results.
        return searchRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix);
    }
    // Search API with caching
    public List<String> getTrieSuggestions(String prefix) {
        //1.first search in cache if available
        List<String>cachedSuggestions=cacheService.getCachedSuggestions(prefix);
        if(cachedSuggestions!=null && !cachedSuggestions.isEmpty()){
            System.out.println("Cache hit i.e. Found in cache: " + prefix);
            return cachedSuggestions;
        }else{
            System.out.println("Cache Miss i.e. Not found in cache: " + prefix);
        }
        //2.if not in cache , fetch from trie
        //getTrieSuggestions() fetches top suggestions from the trie for fast lookup.
        List<String> suggestions= trieService.searchSuggestions(prefix);
        //3.if No results found , return empty list Catche Empty List for 5 minutes (Reduce load)
        if(suggestions.isEmpty()){
            System.out.println("Prefix not found in trie,caching empty response.");
            cacheService.cacheEmptyResponses(prefix);
            return List.of();   //return empty list instead of throwing error
        }
        //4.store in cache for future lookups
        cacheService.cacheSuggestions(prefix,suggestions);

        return suggestions;
    }
    public void updateSearchTerm(SearchTerm SearchTerm){
        //updateSearchTerm() saves new terms or updates existing ones.
        searchRepository.save(SearchTerm);
    }
    // Update Trie & clear cache on insert for a new search term
    public void updateTrieSearchTerm(SearchTerm searchTerm){
        searchRepository.save(searchTerm);
        trieService.insert(searchTerm.getTerm(),searchTerm.getFrequency());
        trieService.saveTrieToDB();//persist the trie to the database.
        //clear cache for the prefix
        cacheService.clearCache(searchTerm.getTerm());
    }
}
