package com.gyan.service;
import org.springframework.stereotype.Service;

import com.gyan.entity.Document;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentRepository;

@Service
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentTextExtractionService ExtractionService;

    public DocumentProcessingService(DocumentRepository documentRepository, DocumentTextExtractionService ExtractionService) {
        this.documentRepository = documentRepository;
        this.ExtractionService = ExtractionService;
    }
    
    public void processDocument(Long documentId, String filePath, String fileType) {

        System.out.println("Processing document " + documentId + " of type " + fileType);

        String extractedText = ExtractionService.extractText(filePath);

        Document document = documentRepository.findById(documentId)
                    .orElseThrow();

        document.setExtractedText(extractedText);

        documentRepository.save(document);

        
        System.out.println("Text extraction completed for document " + documentId);


        

        // future tasksdp
        // indexing
        // embedding generation
        
    }
}
