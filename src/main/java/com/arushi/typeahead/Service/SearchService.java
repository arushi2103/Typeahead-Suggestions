package com.arushi.typeahead.Service;

import com.arushi.typeahead.Model.SearchTerm;
import com.arushi.typeahead.Repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    // Search API with caching
    public List<String> getTrieSuggestions(String prefix) {
        //CACHED SUGGESTIONS
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

        //3.if No results found , return empty list Cache Empty List for 5 minutes (Reduce load)
        if(suggestions.isEmpty()){
            System.out.println("Prefix not found in trie,caching empty response.");
            cacheService.cacheEmptyResponses(prefix);
            return List.of();   //return empty list instead of throwing error
        }
        //FREQUENCY  & RECENCY
        //4. Fetch frequency and recency from MongoDB for ranking
        List<SearchTerm> searchTerms=searchRepository.findByTermStartingWith(prefix);
        //5️⃣ . Create a Map for quick lookup of frequency and recency
        Map<String, SearchTerm> termData = searchTerms.stream()
                .collect(Collectors.toMap(SearchTerm::getTerm, term -> term));
        // 6️⃣ Update frequency & timestamp for searched terms
        for(String term :suggestions){
            termData.compute(term,(key,existingTerm)->{
                if(existingTerm==null){
                    return new SearchTerm(term,1, Instant.now());//new Term
                }else{
                    existingTerm.setFrequency(existingTerm.getFrequency()+1);
                    existingTerm.setLastSearched(Instant.now());
                    return existingTerm;
                }
            });
        }
        // 7. save updated terms back to mongoDB
        searchRepository.saveAll(termData.values());
        // 8. Rank suggestions using the scoring function
        List<String> rankedSuggestions = suggestions.stream()
                .sorted(Comparator.comparing(suggestion -> -calculateScore(suggestion, termData)))
                .collect(Collectors.toList());
        // 9. Store in cache for future lookups
        cacheService.cacheSuggestions(prefix, rankedSuggestions);

        return rankedSuggestions;
    }
    // Scoring function to rank suggestions
    private double calculateScore(String term, Map<String,SearchTerm> termData){
        double w_frequency=1.0; //frequency weight
        double w_recency=0.5; //recency weight
        SearchTerm searchTerm=termData.get(term);
        if(searchTerm==null){
            return 0.0;
        }
        long timeElapsed= Instant.now().getEpochSecond()-searchTerm.getLastSearched().getEpochSecond();
        double recencyScore=1.0/(1+timeElapsed);//Normalize recency score
        return (w_frequency*searchTerm.getFrequency())+(w_recency*recencyScore);
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





