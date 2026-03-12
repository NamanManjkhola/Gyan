package com.gyan.service;
import org.springframework.stereotype.Service;

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

    public DocumentProcessingService(DocumentRepository documentRepository, 
        DocumentTextExtractionService ExtractionService, 
        DocumentIndexService indexService,
        SearchIndexService searchIndexService) {

        this.documentRepository = documentRepository;
        this.ExtractionService = ExtractionService;
        this.indexService = indexService;
        this.searchIndexService = searchIndexService;
    }
    
    public void processDocument(Long documentId, String filePath, String fileType) {

        System.out.println("Processing document " + documentId + " of type " + fileType);

        String extractedText = ExtractionService.extractText(filePath);

        Document document = documentRepository.findById(documentId)
                    .orElseThrow();

        document.setExtractedText(extractedText);

        documentRepository.save(document);
        
        DocumentIndex index = indexService.buildIndex(document);
        searchIndexService.indexDocument(index);
        
        System.out.println("Index prepared for document " + index.getDocumentId());


        

        // future tasksdp
        // indexing
        // embedding generation
        
    }
}
