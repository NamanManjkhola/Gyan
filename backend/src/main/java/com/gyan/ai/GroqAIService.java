package com.gyan.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class GroqAIService implements AIService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Override
    public List<Double> generateEmbedding(String text) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "text-embedding-3-small");
        request.put("input", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

        Map data = ((List<Map>) response.getBody().get("data")).get(0);

        return (List<Double>) data.get("embedding");
    }
}
