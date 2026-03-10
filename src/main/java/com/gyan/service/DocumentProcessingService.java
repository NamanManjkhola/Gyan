package com.gyan.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessingService {
    
    @Async("documentExecutor")
    public void processDocument(String filePath) {
        try{
            System.out.println("Processing Started for: " + filePath);

            //simulate heavy processing
            Thread.sleep(5000);

            System.out.println("Processing completed for: "+ filePath);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        
    }
}
