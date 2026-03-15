package com.gyan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gyan.entity.Document;
import com.gyan.service.SemanticSearchService;

@RestController
@RequestMapping("/documents")
public class SemanticSearchController {
    private final SemanticSearchService semanticSearchService;    

    public SemanticSearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }   

    @GetMapping("/semantic-search")
    public List<Document> sematicSearch(@RequestParam String q) throws Exception {
        return semanticSearchService.semanticSearch(q);
    }
 
}
