package com.gyan.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.gyan.search.DocumentIndex;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentIndex, Long> {
    
}
