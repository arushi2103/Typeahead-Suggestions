package com.arushi.typeahead.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TrendingClientService {

    @Autowired
    private RestTemplate restTemplate;
    private final String trendingServiceUrl = "http://localhost:8081/trending";
    // This class is responsible for interacting with the Trending Service.
    public List<String> fetchTrendingTerms() {
        // Fetches the top 10 trending terms from the Trending Service.
        // It uses RestTemplate to make a GET request to the Trending Service.
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(trendingServiceUrl, List.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error fetching trending terms: " + e.getMessage());
            return List.of(); // fallback to empty list
        }
    }
}
