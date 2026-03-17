package com.gyan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gyan.ai.LLMService;
import com.gyan.entity.DocumentChunk;

@Service
public class QuestionAnswerService {
    private final SemanticSearchService semanticSearchService;
    private final LLMService llmService;

    public QuestionAnswerService(SemanticSearchService semanticSearchService, LLMService llmService) {
        this.semanticSearchService = semanticSearchService;
        this.llmService = llmService;
    }

    public String askQuestion(String question) throws Exception {
        List<DocumentChunk> chunks = semanticSearchService.findRelevantChunks((question));

        StringBuilder context = new StringBuilder();

        for(DocumentChunk chunk : chunks) {
            context.append(chunk.getChunkText()).append("\n\n");
        }

        return llmService.generateAnswer(question, context.toString());

    }
}
