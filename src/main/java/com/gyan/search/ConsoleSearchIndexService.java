package com.gyan.search;

import org.springframework.stereotype.Service;

@Service
public class ConsoleSearchIndexService implements SearchIndexService {
    @Override
    public void indexDocument(DocumentIndex documentIndex) {
         System.out.println("Indexing document " + documentIndex.getDocumentId());

        System.out.println("Indexed content preview: " +
                documentIndex.getContent().substring(0, Math.min(100, documentIndex.getContent().length())));
    }
}
