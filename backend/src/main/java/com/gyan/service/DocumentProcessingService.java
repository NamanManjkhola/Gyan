package com.gyan.service;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final DocumentTextExtractionService extractionService;
    private final DocumentIndexService indexService;
    private final SearchIndexService searchIndexService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    public DocumentProcessingService(DocumentRepository documentRepository, 
        DocumentTextExtractionService extractionService, 
        DocumentIndexService indexService,
        SearchIndexService searchIndexService,
        EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {

        this.documentRepository = documentRepository;
        this.extractionService = extractionService;
        this.indexService = indexService;
        this.searchIndexService = searchIndexService;
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }
    
    public void processDocument(Long documentId, String filePath, String fileType) throws JsonProcessingException {

        System.out.println("STEP 1");
        log.info("Processing document " + documentId + " of type " + fileType);

        String extractedText = extractionService.extractText(filePath);

        System.out.println("STEP 2");

        log.debug("extracted text length : " + extractedText.length());

        Document document = documentRepository.findById(documentId)
                    .orElseThrow();

        
        System.out.println("STEP 3");
        document.setExtractedText(extractedText);
        System.out.println("STEP 4 BEFORE SAVE");
        documentRepository.save(document);
        System.out.println("STEP 5 AFTER SAVE");

        // generate embedding
        List<String> chunks = TextChunker.chunkText(extractedText, 500);

        log.info("total chunks created : " + chunks.size());
        

        
        for(String chunk: chunks) {
            List<Double> embedding = embeddingService.generateEmbedding(chunk);
            String vectorJson = new ObjectMapper().writeValueAsString(embedding);

            DocumentChunk documentChunk = new DocumentChunk();

            documentChunk.setDocument(document);
            documentChunk.setChunkText(chunk);
            documentChunk.setEmbeddingVector(vectorJson);

            documentChunkRepository.save(documentChunk);    

            
        }
        
        DocumentIndex index = indexService.buildIndex(document);
        searchIndexService.indexDocument(index);
        
        log.info("Index prepared for document " + index.getDocumentId());


        

        // future tasksdp
        // indexing
        // embedding generation
        
    }
}
