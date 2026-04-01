package com.gyan.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gyan.ai.LLMService;
import com.gyan.entity.DocumentChunk;

@Service
public class QuestionAnswerService {
    private final SemanticSearchService semanticSearchService;
    private final LLMService llmService;
    private static final Logger log = LoggerFactory.getLogger(QuestionAnswerService.class);

    public QuestionAnswerService(SemanticSearchService semanticSearchService, LLMService llmService) {
        this.semanticSearchService = semanticSearchService;
        this.llmService = llmService;
    }

    public String askQuestion(String question) throws Exception {
        log.info("Generating answers for : " + question);
        List<DocumentChunk> chunks = semanticSearchService.findRelevantChunks((question));
        return generateAnswer(question, chunks);
    }

    public String askQuestion(Long chatId, String question) throws Exception {
        log.info("Generating answers for chat {} and question {}", chatId, question);
        List<DocumentChunk> chunks = semanticSearchService.findRelevantChunks(chatId, question);
        return generateAnswer(question, chunks);
    }

    private String generateAnswer(String question, List<DocumentChunk> chunks) throws Exception {
        StringBuilder context = new StringBuilder();

        for (DocumentChunk chunk : chunks) {
            context.append(chunk.getChunkText()).append("\n\n");
        }

        return llmService.generateAnswer(question, context.toString());
    }
}
