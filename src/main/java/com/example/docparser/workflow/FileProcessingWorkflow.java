package com.example.docparser.workflow;

import com.example.docparser.model.FileProcessingRequest;
import com.example.docparser.model.JobStatus;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Main workflow interface for processing large CSV files.
 * Orchestrates chunk processing workflows and tracks overall progress.
 */
@WorkflowInterface
public interface FileProcessingWorkflow {
    
    /**
     * Main workflow method that processes the entire file
     * @param request File processing configuration
     * @return Final job status
     */
    @WorkflowMethod
    JobStatus processFile(FileProcessingRequest request);
    
    /**
     * Query method to get current job status
     * @return Current job status with progress information
     */
    @QueryMethod
    JobStatus getJobStatus();
    
    /**
     * Query method to get detailed progress information
     * @return Detailed progress including chunk-level status
     */
    @QueryMethod
    JobStatus getDetailedProgress();
    
    /**
     * Signal to pause processing
     */
    @SignalMethod
    void pauseProcessing();
    
    /**
     * Signal to resume processing
     */
    @SignalMethod
    void resumeProcessing();
    
    /**
     * Signal to cancel processing
     */
    @SignalMethod
    void cancelProcessing();
}
