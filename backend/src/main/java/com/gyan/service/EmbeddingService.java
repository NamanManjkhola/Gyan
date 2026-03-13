package com.gyan.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbeddingService {
    
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> generateEmbedding(String text) {
        
        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:8000/embedding",
                request, Map.class);
        
        List<Double> embedding = (List<Double>) response.getBody().get("embedding");

        return embedding;
    }

}
