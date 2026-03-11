package com.gyan.service;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessingService {
    
    public void processDocument(Long documentId, String filePath, String fileType) {
        System.out.println("Processing document: " + documentId);

        // future tasks
        // text extraction
        // indexing
        // embedding generation
        
    }
}
