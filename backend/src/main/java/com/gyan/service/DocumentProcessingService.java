package com.gyan.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Document;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.DocumentIndex;
import com.gyan.search.SearchIndexService;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractionService ExtractionService;
    private final DocumentIndexService indexService;
    private final SearchIndexService searchIndexService;
    private final EmbeddingService embeddingService;

    public DocumentProcessingService(DocumentRepository documentRepository, 
        DocumentTextExtractionService ExtractionService, 
        DocumentIndexService indexService,
        SearchIndexService searchIndexService,
        EmbeddingService embeddingService) {

        this.documentRepository = documentRepository;
        this.ExtractionService = ExtractionService;
        this.indexService = indexService;
        this.searchIndexService = searchIndexService;
        this.embeddingService = embeddingService;
    }
    
    public void processDocument(Long documentId, String filePath, String fileType) throws JsonProcessingException {

        System.out.println("Processing document " + documentId + " of type " + fileType);

        String extractedText = ExtractionService.extractText(filePath);

        Document document = documentRepository.findById(documentId)
                    .orElseThrow();

        document.setExtractedText(extractedText);

        // generate embedding
        List<Double> embedding =
        embeddingService.generateEmbedding(extractedText);

        String vectorJson =
        new ObjectMapper().writeValueAsString(embedding);
        document.setEmbeddingVector(vectorJson);

        documentRepository.save(document);
        
        DocumentIndex index = indexService.buildIndex(document);
        searchIndexService.indexDocument(index);
        
        System.out.println("Index prepared for document " + index.getDocumentId());


        

        // future tasksdp
        // indexing
        // embedding generation
        
    }
}
