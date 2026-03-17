package com.gyan.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.service.QuestionAnswerService;

@RestController
@RequestMapping("/ai")
public class AskController {
    private final QuestionAnswerService qaService;

    public AskController(QuestionAnswerService qaService) {
        this.qaService = qaService;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody Map<String, String> request) throws Exception { 
        String question = request.get("question");

        return qaService.askQuestion(question);
    }
}
