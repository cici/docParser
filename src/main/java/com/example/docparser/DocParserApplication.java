package com.example.docparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for the Document Parser.
 * This application provides REST APIs for file processing and monitoring.
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class DocParserApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DocParserApplication.class, args);
    }
}
