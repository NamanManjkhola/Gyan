package com.gyan.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyan.entity.Document;
import com.gyan.entity.DocumentChunk;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.DocumentIndex;
import com.gyan.search.SearchIndexService;
import com.gyan.util.TextChunker;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractionService ExtractionService;
    private final DocumentIndexService indexService;
    private final SearchIndexService searchIndexService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentProcessingService(DocumentRepository documentRepository, 
        DocumentTextExtractionService ExtractionService, 
        DocumentIndexService indexService,
        SearchIndexService searchIndexService,
        EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {

        this.documentRepository = documentRepository;
        this.ExtractionService = ExtractionService;
        this.indexService = indexService;
        this.searchIndexService = searchIndexService;
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }
    
    public void processDocument(Long documentId, String filePath, String fileType) throws JsonProcessingException {

        System.out.println("Processing document " + documentId + " of type " + fileType);

        String extractedText = ExtractionService.extractText(filePath);

        Document document = documentRepository.findById(documentId)
                    .orElseThrow();

        document.setExtractedText(extractedText);

        // generate embedding
        List<String> chunks = TextChunker.chunkText(extractedText, 500);
        
        for(String chunk: chunks) {
            List<Double> embedding = embeddingService.generateEmbedding(chunk);
            String vectorJson = new ObjectMapper().writeValueAsString(embedding);

            DocumentChunk documentChunk = new DocumentChunk();

            documentChunk.setDocument(document);
            documentChunk.setChunkText(chunk);
            documentChunk.setEmbeddingVector(vectorJson);

            documentChunkRepository.save(documentChunk);    

            
        }

        documentRepository.save(document);
        
        DocumentIndex index = indexService.buildIndex(document);
        searchIndexService.indexDocument(index);
        
        System.out.println("Index prepared for document " + index.getDocumentId());


        

        // future tasksdp
        // indexing
        // embedding generation
        
    }
}
