package com.gyan.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gyan.dto.ChatCreateRequestDTO;
import com.gyan.dto.ChatResponseDTO;
import com.gyan.entity.Chat;
import com.gyan.entity.Document;
import com.gyan.entity.User;
import com.gyan.repository.ChatRepository;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.SearchIndexService;
import com.gyan.storage.StorageService;

@Service
public class ChatService {
    private static final long MAX_CHATS_PER_USER = 5;

    private final ChatRepository chatRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final SearchIndexService searchIndexService;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;

    public ChatService(
        ChatRepository chatRepository,
        DocumentRepository documentRepository,
        DocumentChunkRepository documentChunkRepository,
        SearchIndexService searchIndexService,
        StorageService storageService,
        CurrentUserService currentUserService
    ) {
        this.chatRepository = chatRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.searchIndexService = searchIndexService;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
    }

    public List<ChatResponseDTO> getCurrentUserChats() {
        User user = currentUserService.getCurrentUser();

        return chatRepository.findByUserOrderByUpdatedAtDesc(user)
            .stream()
            .map(this::mapToDTO)
            .toList();
    }

    public ChatResponseDTO createChat(ChatCreateRequestDTO request) {
        User user = currentUserService.getCurrentUser();

        if (chatRepository.countByUser(user) >= MAX_CHATS_PER_USER) {
            throw new IllegalStateException("You can create at most 5 chats.");
        }

        LocalDateTime now = LocalDateTime.now();

        Chat chat = new Chat();
        chat.setName(request.getName().trim());
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        chat.setUser(user);

        return mapToDTO(chatRepository.save(chat));
    }

    public Chat getOwnedChat(Long chatId) {
        User user = currentUserService.getCurrentUser();
        return chatRepository.findByIdAndUser(chatId, user)
            .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    public ChatResponseDTO getChat(Long chatId) {
        return mapToDTO(getOwnedChat(chatId));
    }

    public void touch(Chat chat) {
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        Chat chat = getOwnedChat(chatId);

        for (Document document : documentRepository.findAllByChat(chat)) {
            documentChunkRepository.deleteByDocument(document);
            searchIndexService.deleteDocument(document.getId());

            if (document.getStoredFileName() != null && !document.getStoredFileName().isBlank()) {
                storageService.delete(document.getStoredFileName());
            }

            documentRepository.delete(document);
        }

        chatRepository.delete(chat);
    }

    private ChatResponseDTO mapToDTO(Chat chat) {
        return new ChatResponseDTO(
            chat.getId(),
            chat.getName(),
            chat.getCreatedAt(),
            chat.getUpdatedAt(),
            documentRepository.countByChat(chat)
        );
    }
}
