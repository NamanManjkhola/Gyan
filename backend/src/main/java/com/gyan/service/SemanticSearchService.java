package com.gyan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Document;
import com.gyan.repository.DocumentRepository;
import com.gyan.util.VectorSimilarityUtil;

@Service
public class SemanticSearchService {
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    public SemanticSearchService(DocumentRepository documentRepository, EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    public List<Document> semanticSearch(String query) throws Exception {
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        List<Document> documents = documentRepository.findAll();

        List<Map.Entry<Document, Double>> scores = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for(Document doc : documents) {
            if(doc.getEmbeddingVector() == null)   continue;

            List<Double> docEmbedding = mapper.readValue(doc.getEmbeddingVector(), List.class);;

            double similarity = VectorSimilarityUtil.cosineSimilarity(queryEmbedding, docEmbedding);
            scores.add(Map.entry(doc, similarity));
        }

        return scores.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }
}
