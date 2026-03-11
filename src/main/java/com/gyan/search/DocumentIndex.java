package com.gyan.search;

public class DocumentIndex {
    private Long documentId;
    private String title;
    private String content;
    private String ownerEmail;

    public DocumentIndex(Long documentId, String title, String content, String ownerEmail) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.ownerEmail = ownerEmail;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    
}
