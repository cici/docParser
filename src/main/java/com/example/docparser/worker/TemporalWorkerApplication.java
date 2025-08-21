package com.example.docparser.worker;

import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Standalone worker application for processing Temporal workflows.
 * This can be deployed separately from the REST API for better scalability.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.docparser")
public class TemporalWorkerApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporalWorkerApplication.class);
    
    @Autowired
    private WorkerFactory workerFactory;
    
    @Autowired
    private Worker fileProcessingWorker;
    
    public static void main(String[] args) {
        SpringApplication.run(TemporalWorkerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Temporal worker...");
        
        try {
            // Start the worker
            workerFactory.start();
            
            logger.info("Temporal worker started successfully");
            logger.info("Worker configuration:");
            logger.info("  - Task Queue: {}", fileProcessingWorker.getTaskQueue());
            logger.info("  - Worker started with configured options");
            
            // Keep the worker running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Temporal worker...");
                workerFactory.shutdown();
                logger.info("Temporal worker shut down successfully");
            }));
            
            // Block to keep the worker running
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start Temporal worker", e);
            throw e;
        }
    }
}
