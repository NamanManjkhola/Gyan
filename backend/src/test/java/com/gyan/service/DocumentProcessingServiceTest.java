package com.gyan.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.entity.Document;
import com.gyan.processing.DocumentTextExtractionService;
import com.gyan.repository.DocumentChunkRepository;
import com.gyan.repository.DocumentRepository;
import com.gyan.search.DocumentIndex;
import com.gyan.search.SearchIndexService;

@ExtendWith(MockitoExtension.class)
public class DocumentProcessingServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTextExtractionService extractionService;

    @Mock
    private DocumentIndexService indexService;

    @Mock
    private SearchIndexService searchIndexService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @InjectMocks 
    private DocumentProcessingService documentProcessingService;

    @Test
    void testProcessDocument() throws Exception {
        Long docId = 10L;

        Document doc = new Document();
        doc.setId(docId);

        when(documentRepository.findById(docId))
            .thenReturn(Optional.of(doc));
        
        when(extractionService.extractText(any()))
            .thenReturn("Kafka is used for event driven systems");

        when(embeddingService.generateEmbedding(any()))
            .thenReturn(List.of(0.1, 0.2, 0.3));
            
        when(indexService.buildIndex(any()))
            .thenReturn(new DocumentIndex(docId, "new.txt", "Kafka is used for event driven systems", "anam@gmail.com"));
        
        documentProcessingService.processDocument(docId, "filePath", "text/plain");
        
        verify(documentRepository).save(any(Document.class));
        verify(documentChunkRepository, atLeastOnce()).save(any());
        verify(searchIndexService).indexDocument(any());

        
    }
}
