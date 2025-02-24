package com.arushi.typeahead.Service;

import com.arushi.typeahead.Model.SearchTerm;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CacheService {
    private final RedisTemplate<String, String> redisTemplate;

    public CacheService( RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //Get Cached suggestions
    public List<String> getCachedSuggestions(String prefix){
        String cacheKey="suggestions:"+prefix;
        List<String>cachedResults= redisTemplate.opsForList().range(cacheKey,0,-1);
        //return empty list if no results found
        return (cachedResults==null || cachedResults.isEmpty())?List.of():cachedResults;
    }
    //Cache new suggestions with expiry time
    public void cacheSuggestions(String prefix,List<String>suggestions){
        String cacheKey="suggestions:"+prefix;
        //Ensure we store a proper list, not a sublist
        List<String> validList=new ArrayList<>(suggestions);
        //push to redis
        redisTemplate.opsForList().rightPushAll(cacheKey,validList.toArray(new String[0]));
        // Set expiry time (1 hour)
        redisTemplate.expire(cacheKey,1, TimeUnit.HOURS);
    }
    //Cache empty responses for 5 minutes
    public void cacheEmptyResponses(String prefix){
        String cacheKey="suggestions:"+prefix;
        //push empty list to redis
        redisTemplate.opsForList().rightPushAll(cacheKey, List.of("").toArray(new String[0]));
        // Set expiry time (5 minutes)
        redisTemplate.expire(cacheKey,5, TimeUnit.MINUTES);
    }
    //Clear cache for a specific prefix in the request
    public void clearCache(String prefix){
        redisTemplate.delete("suggestions:"+prefix);
    }

    //Flush entire cache periodically
    public void clearAllCache(){
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}
