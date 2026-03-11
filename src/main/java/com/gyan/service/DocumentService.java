package com.gyan.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.entity.Document;
import com.gyan.entity.User;
import com.gyan.event.DocumentUploadedEvent;
import com.gyan.producer.DocumentEventProducer;
import com.gyan.repository.DocumentRepository;
import com.gyan.repository.UserRepository;
import com.gyan.storage.StorageService;
import com.gyan.util.FileValidator;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final DocumentProcessingService documentProcessingService;  
    private final FileValidator fileValidator;
    private final DocumentEventProducer documentEventProducer;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(
            DocumentRepository documentRepository 
            ,StorageService storageService
            ,UserRepository userRepository
            ,DocumentProcessingService documentProcessingService
            ,FileValidator fileValidator, DocumentEventProducer documentEventProducer) {

        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.documentProcessingService = documentProcessingService;
        this.fileValidator = fileValidator;
        this.documentEventProducer = documentEventProducer; 
    }

    public DocumentResponseDTO uploadFile(MultipartFile file) throws IOException {

        fileValidator.validate(file);

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();
                
        String storedFileName = storageService.store(file);

        Document document = new Document();

        document.setFilename(file.getOriginalFilename());
        document.setStoredFileName(storedFileName);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(storedFileName);
        document.setFilePath(uploadDir + "/" + storedFileName);
        document.setUploadedAt(LocalDateTime.now());
        document.setUser(user);

        Document saved = documentRepository.save(document);

        // documentProcessingService.processDocument(saved.getFilePath());

        DocumentUploadedEvent event = new DocumentUploadedEvent(
            document.getId(),
            document.getFilePath(),
            document.getFileType(),
            user.getId()
        );

        documentEventProducer.publishDocumentUploaded(event);

        return mapToDTO(saved);
    }

    public Resource downloadDocument(Long id) {

        Document document = documentRepository  
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException("Document Not Found"));

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        
        if(!document.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorizad Access");
        }
        
        
        return storageService.load(document.getStoredFileName());
    }

    private DocumentResponseDTO mapToDTO(Document document) {

        DocumentResponseDTO dto = new DocumentResponseDTO();

        dto.setId(document.getId());
        dto.setFileName(document.getFilename());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setFilePath(document.getFilePath());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setOwnerEmail(document.getUser().getEmail());

        return dto;
    }

    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()    
                .getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        
        Page<Document> documents = documentRepository.findByUser(user, pageable);

        return documents.map(this::mapToDTO);
    }


    public DocumentResponseDTO getDocumentById(Long id) {

        Document document = documentRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));
        
        String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()    
                    .getName();

        if(!document.getUser().getEmail().equals(email)){
            throw new RuntimeException("Unauthorized Access");
        }

        return mapToDTO(document);
    }
}
