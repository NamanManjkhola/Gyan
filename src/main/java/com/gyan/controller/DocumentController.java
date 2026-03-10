package com.gyan.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.gyan.dto.DocumentResponseDTO;
import com.gyan.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public DocumentResponseDTO uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        return documentService.uploadFile(file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = documentService.downloadDocument(id);

        StreamingResponseBody stream = outputStream -> {
            InputStream inputStream = resource.getInputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;

            while((bytesRead = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();

        };

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @GetMapping
    public Page<DocumentResponseDTO> getDocuments(Pageable pageable) {
        return documentService.getDocuments(pageable);
    }

    @GetMapping("/{id}")
    public DocumentResponseDTO getDocument(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

}
