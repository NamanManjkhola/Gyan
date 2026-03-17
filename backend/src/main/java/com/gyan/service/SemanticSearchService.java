package com.gyan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.util.VectorSimilarityUtil;

@Service
public class SemanticSearchService {
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    public SemanticSearchService(DocumentChunkRepository documentChunkRepository, EmbeddingService embeddingService) {
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    public List<Document> semanticSearch(String query) throws Exception {
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);
        List<DocumentChunk> chunks = documentChunkRepository.findAll();

        List<Map.Entry<DocumentChunk, Double>> scores = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for(DocumentChunk chunk : chunks) {
            if(chunk.getEmbeddingVector() == null)   continue;

            List<Double> chunkEmbedding = mapper.readValue(chunk.getEmbeddingVector(), List.class);;

            double similarity = VectorSimilarityUtil.cosineSimilarity(queryEmbedding, chunkEmbedding);
            scores.add(Map.entry(chunk, similarity));
        }

        // Sort by similarity descending
        List<Document> result =
                scores.stream()
                        .sorted((a, b) ->
                                Double.compare(
                                        b.getValue(),
                                        a.getValue()))
                        .limit(5)
                        .map(entry ->
                                entry.getKey().getDocument())
                        .distinct()
                        .toList();

        return result;
    }

    public List<DocumentChunk> findRelevantChunks(String query) throws Exception {

        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(query);

        List<DocumentChunk> chunks =
                documentChunkRepository.findAll();

        List<Map.Entry<DocumentChunk, Double>> scores =
                calculateScores(queryEmbedding, chunks);

        return scores.stream()
                .sorted((a, b) ->
                        Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Map.Entry<DocumentChunk, Double>> calculateScores(
            List<Double> queryEmbedding,
            List<DocumentChunk> chunks) throws Exception {

        List<Map.Entry<DocumentChunk, Double>> scores = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for (DocumentChunk chunk : chunks) {

            if (chunk.getEmbeddingVector() == null) continue;

            List<Double> chunkEmbedding =
                    mapper.readValue(
                            chunk.getEmbeddingVector(),
                            List.class
                    );

            double similarity =
                    VectorSimilarityUtil.cosineSimilarity(
                            queryEmbedding,
                            chunkEmbedding
                    );

            scores.add(Map.entry(chunk, similarity));
        }

        return scores;
    }
}
