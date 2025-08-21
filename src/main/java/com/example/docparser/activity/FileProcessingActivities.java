package com.example.docparser.activity;

import com.example.docparser.model.FailedRecord;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

/**
 * Activity interface for file-level processing operations.
 * These activities handle file analysis, job management, and reprocessing.
 */
@ActivityInterface
public interface FileProcessingActivities {
    
    /**
     * Analyze the local file to determine chunking strategy
     * @param directory Directory name (replaces S3 bucket for demo)
     * @param filename File name (replaces S3 key for demo)
     * @param chunkSizeBytes Target chunk size in bytes
     * @return File analysis result with chunk information
     */
    @ActivityMethod
    FileAnalysisResult analyzeFile(String directory, String filename, long chunkSizeBytes);
    
    /**
     * Get failed records for reprocessing
     * @param jobId Job identifier
     * @param includeReprocessed Whether to include already reprocessed records
     * @return List of failed records
     */
    @ActivityMethod
    List<FailedRecord> getFailedRecords(String jobId, boolean includeReprocessed);
    
    /**
     * Reprocess failed records
     * @param jobId Job identifier
     * @param failedRecords List of failed records to reprocess
     * @return Reprocessing result
     */
    @ActivityMethod
    ReprocessingResult reprocessFailedRecords(String jobId, List<FailedRecord> failedRecords);
    
    /**
     * Finalize job processing
     * @param jobId Job identifier
     */
    @ActivityMethod
    void finalizeJob(String jobId);
}
