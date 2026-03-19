package com.gyan.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gyan.ai.LLMService;
import com.gyan.entity.DocumentChunk;

@ExtendWith(MockitoExtension.class)
class QuestionAnswerServiceTest {

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private LLMService llmService;

    @InjectMocks
    private QuestionAnswerService qaService;

    @Test
    void testAskQuestion() throws Exception {

        DocumentChunk chunk = new DocumentChunk();
        chunk.setChunkText("Kafka is used for event-driven systems");

        when(semanticSearchService.findRelevantChunks(any()))
                .thenReturn(List.of(chunk));

        when(llmService.generateAnswer(any(), any()))
                .thenReturn("Kafka is used for event-driven systems");

        String answer = qaService.askQuestion("What is Kafka?");

        assertNotNull(answer);
        assertTrue(answer.contains("Kafka"));
    }
}