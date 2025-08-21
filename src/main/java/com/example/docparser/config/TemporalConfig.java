package com.example.docparser.config;

import com.example.docparser.activity.ChunkProcessingActivitiesImpl;
import com.example.docparser.activity.FileProcessingActivitiesImpl;
import com.example.docparser.workflow.ChunkProcessingWorkflowImpl;
import com.example.docparser.workflow.FileProcessingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporal configuration for connecting to Temporal Cloud
 * and configuring workers for file processing workflows.
 */
@Configuration
public class TemporalConfig {
    
    @Value("${temporal.cloud.endpoint:}")
    private String temporalEndpoint;
    
    @Value("${temporal.cloud.namespace}")
    private String temporalNamespace;
    
    @Value("${temporal.cloud.client-cert}")
    private String clientCertPath;
    
    @Value("${temporal.cloud.client-key}")
    private String clientKeyPath;
    
    @Value("${temporal.task-queue:file-processing}")
    private String taskQueue;
    
    @Value("${temporal.worker.max-concurrent-activities:50}")
    private int maxConcurrentActivities;
    
    @Value("${temporal.worker.max-concurrent-workflows:20}")
    private int maxConcurrentWorkflows;
    
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions.Builder optionsBuilder = WorkflowServiceStubsOptions.newBuilder();
        
        if (!temporalEndpoint.isEmpty()) {
            // Configure for Temporal Cloud
            optionsBuilder.setTarget(temporalEndpoint);
            
            // SSL configuration for Temporal Cloud
            // Note: SSL configuration is disabled for demo purposes
            // In production, implement proper certificate loading and validation
            // if (!clientCertPath.isEmpty() && !clientKeyPath.isEmpty()) {
            //     optionsBuilder.setSslContext(createSslContext());
            // }
        }
        
        return WorkflowServiceStubs.newServiceStubs(optionsBuilder.build());
    }
    
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalNamespace)
                        .build()
        );
    }
    
    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }
    
    @Bean
    public Worker fileProcessingWorker(
            WorkerFactory workerFactory,
            FileProcessingActivitiesImpl fileActivities,
            ChunkProcessingActivitiesImpl chunkActivities) {
        
        // Create worker with basic configuration
        // Note: Worker options configuration simplified for demo
        Worker worker = workerFactory.newWorker(taskQueue);
        
        // Register workflow implementations
        worker.registerWorkflowImplementationTypes(
                FileProcessingWorkflowImpl.class,
                ChunkProcessingWorkflowImpl.class
        );
        
        // Register activity implementations
        worker.registerActivitiesImplementations(fileActivities, chunkActivities);
        
        return worker;
    }
    

}
