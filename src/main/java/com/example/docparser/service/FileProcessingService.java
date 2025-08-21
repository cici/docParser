package com.example.docparser.service;

import com.example.docparser.model.FileProcessingRequest;
import com.example.docparser.model.JobStatus;
import com.example.docparser.workflow.FileProcessingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for managing file processing workflows.
 * Provides methods to start, monitor, and control workflows.
 */
@Service
public class FileProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);
    
    @Autowired
    private WorkflowClient workflowClient;
    
    @Value("${temporal.task-queue:file-processing}")
    private String taskQueue;
    
    /**
     * Start a new file processing workflow
     * @param request File processing request
     * @return Workflow ID
     */
    public String startFileProcessing(FileProcessingRequest request) {
        logger.info("Starting file processing workflow for job: {}", request.getJobId());
        
        try {
            String workflowId = "file-processing-" + request.getJobId();
            
            // Create workflow options
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(taskQueue)
                    .setWorkflowExecutionTimeout(Duration.ofHours(24)) // 24 hour timeout
                    .setWorkflowRunTimeout(Duration.ofHours(12))       // 12 hour run timeout
                    .build();
            
            // Create workflow stub and start execution
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, options);
            
            // Start workflow asynchronously
            WorkflowClient.start(workflow::processFile, request);
            
            logger.info("File processing workflow started: {} for job: {}", workflowId, request.getJobId());
            return workflowId;
            
        } catch (Exception e) {
            logger.error("Failed to start file processing workflow for job: {}", request.getJobId(), e);
            throw new RuntimeException("Failed to start workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get job status from workflow
     * @param jobId Job identifier
     * @return Current job status
     */
    public JobStatus getJobStatus(String jobId) {
        try {
            String workflowId = "file-processing-" + jobId;
            
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, workflowId);
            
            return workflow.getJobStatus();
            
        } catch (Exception e) {
            logger.error("Failed to get job status for job: {}", jobId, e);
            return null;
        }
    }
    
    /**
     * Get detailed progress from workflow
     * @param jobId Job identifier
     * @return Detailed progress information
     */
    public JobStatus getDetailedProgress(String jobId) {
        try {
            String workflowId = "file-processing-" + jobId;
            
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, workflowId);
            
            return workflow.getDetailedProgress();
            
        } catch (Exception e) {
            logger.error("Failed to get detailed progress for job: {}", jobId, e);
            return null;
        }
    }
    
    /**
     * Pause job processing
     * @param jobId Job identifier
     */
    public void pauseJob(String jobId) {
        try {
            String workflowId = "file-processing-" + jobId;
            
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, workflowId);
            
            workflow.pauseProcessing();
            logger.info("Paused job: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Failed to pause job: {}", jobId, e);
            throw new RuntimeException("Failed to pause job: " + e.getMessage(), e);
        }
    }
    
    /**
     * Resume job processing
     * @param jobId Job identifier
     */
    public void resumeJob(String jobId) {
        try {
            String workflowId = "file-processing-" + jobId;
            
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, workflowId);
            
            workflow.resumeProcessing();
            logger.info("Resumed job: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Failed to resume job: {}", jobId, e);
            throw new RuntimeException("Failed to resume job: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cancel job processing
     * @param jobId Job identifier
     */
    public void cancelJob(String jobId) {
        try {
            String workflowId = "file-processing-" + jobId;
            
            FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                    FileProcessingWorkflow.class, workflowId);
            
            workflow.cancelProcessing();
            logger.info("Cancelled job: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Failed to cancel job: {}", jobId, e);
            throw new RuntimeException("Failed to cancel job: " + e.getMessage(), e);
        }
    }
}
