package com.gyan.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.User;
import com.gyan.event.DocumentUploadedEvent;
import com.gyan.producer.DocumentEventProducer;
import com.gyan.repository.DocumentRepository;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.search.SearchIndexService;
import com.gyan.storage.StorageService;
import com.gyan.util.FileValidator;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CurrentUserService currentUserService;
    private final ChatService chatService;
    private final DocumentChunkRepository documentChunkRepository;
    private final SearchIndexService searchIndexService;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final DocumentEventProducer documentEventProducer;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(
            DocumentRepository documentRepository 
            ,StorageService storageService
            ,CurrentUserService currentUserService
            ,ChatService chatService
            ,DocumentChunkRepository documentChunkRepository
            ,SearchIndexService searchIndexService
            ,FileValidator fileValidator, DocumentEventProducer documentEventProducer) {

        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
        this.chatService = chatService;
        this.documentChunkRepository = documentChunkRepository;
        this.searchIndexService = searchIndexService;
        this.fileValidator = fileValidator;
        this.documentEventProducer = documentEventProducer; 
    }

    public DocumentResponseDTO uploadFile(Long chatId, MultipartFile file) throws IOException {

        fileValidator.validate(file);

        User user = currentUserService.getCurrentUser();
        Chat chat = chatService.getOwnedChat(chatId);
                
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
        document.setChat(chat);

        Document saved = documentRepository.save(document);
        chatService.touch(chat);

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
        return downloadDocument(null, id);
    }

    public Resource downloadDocument(Long chatId, Long id) {

        Document document = documentRepository  
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException("Document Not Found"));

        User user = currentUserService.getCurrentUser();
        
        if(!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized Access");
        }

        if (chatId != null) {
            if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
                throw new RuntimeException("Document does not belong to this chat");
            }
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
        dto.setChatId(document.getChat() != null ? document.getChat().getId() : null);

        return dto;
    }

    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        
        Page<Document> documents = documentRepository.findByUser(user, pageable);

        return documents.map(this::mapToDTO);
    }

    public Page<DocumentResponseDTO> getDocumentsByChat(Long chatId, Pageable pageable) {
        Chat chat = chatService.getOwnedChat(chatId);

        return documentRepository.findByChat(chat, pageable)
            .map(this::mapToDTO);
    }


    public DocumentResponseDTO getDocumentById(Long id) {

        Document document = documentRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Document Not Found"));
        
        User user = currentUserService.getCurrentUser();

        if(!document.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized Access");
        }

        return mapToDTO(document);
    }

    public DocumentResponseDTO getDocumentByChatAndId(Long chatId, Long id) {
        Document document = documentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Document Not Found"));

        User user = currentUserService.getCurrentUser();

        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized Access");
        }

        if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
            throw new RuntimeException("Document does not belong to this chat");
        }

        return mapToDTO(document);
    }

    @Transactional
    public void deleteDocumentByChatAndId(Long chatId, Long id) {
        Document document = documentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Document Not Found"));

        User user = currentUserService.getCurrentUser();

        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized Access");
        }

        if (document.getChat() == null || !document.getChat().getId().equals(chatId)) {
            throw new RuntimeException("Document does not belong to this chat");
        }

        deleteDocumentResources(document);
        chatService.touch(document.getChat());
    }

    @Transactional
    public void deleteDocumentResources(Document document) {
        documentChunkRepository.deleteByDocument(document);
        searchIndexService.deleteDocument(document.getId());

        if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
            storageService.delete(document.getStoredFileName());
        }

        documentRepository.delete(document);
    }
}
