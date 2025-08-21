package com.example.docparser.activity;

import com.example.docparser.model.FailedRecord;
import com.example.docparser.service.FailedRecordService;
import com.example.docparser.service.JobStatusService;
import com.example.docparser.service.FileService;
import io.temporal.activity.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of file processing activities.
 * Handles file analysis, job management, and coordination.
 */
@Component
public class FileProcessingActivitiesImpl implements FileProcessingActivities {
    
    private static final Logger logger = LoggerFactory.getLogger(FileProcessingActivitiesImpl.class);
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private JobStatusService jobStatusService;
    
    @Autowired
    private FailedRecordService failedRecordService;
    
    @Override
    public FileAnalysisResult analyzeFile(String directory, String filename, long chunkSizeBytes) {
        logger.info("Analyzing file: {}/{}", directory, filename);
        
        try {
            // Get file metadata from local filesystem
            long fileSizeBytes = fileService.getFileSize(directory, filename);
            logger.info("File size: {} bytes", fileSizeBytes);
            
            // Calculate number of chunks needed
            int totalChunks = (int) Math.ceil((double) fileSizeBytes / chunkSizeBytes);
            logger.info("Will process {} chunks of ~{} MB each", totalChunks, chunkSizeBytes / (1024 * 1024));
            
            // Estimate user count by sampling the file
            long estimatedUserCount = estimateUserCount(directory, filename, fileSizeBytes);
            logger.info("Estimated user count: {}", estimatedUserCount);
            
            return new FileAnalysisResult(fileSizeBytes, estimatedUserCount, totalChunks, chunkSizeBytes);
            
        } catch (Exception e) {
            logger.error("Failed to analyze file: {}/{}", directory, filename, e);
            throw new RuntimeException("File analysis failed: " + e.getMessage(), e);
        }
    }
    
    private long estimateUserCount(String directory, String filename, long fileSizeBytes) {
        try {
            // Read a small sample from the beginning of the file to estimate row count
            long sampleSize = Math.min(1024 * 1024, fileSizeBytes); // 1MB sample
            byte[] sample = fileService.readFileRange(directory, filename, 0, sampleSize);
            
            // Count lines in sample (assuming one user per line)
            String sampleText = new String(sample);
            long linesInSample = sampleText.chars().mapToLong(c -> c == '\n' ? 1 : 0).sum();
            
            if (linesInSample == 0) {
                logger.warn("No lines found in sample, defaulting to file size estimation");
                return fileSizeBytes / 100; // Rough estimate: 100 bytes per user
            }
            
            // Extrapolate to full file
            double averageBytesPerLine = (double) sampleSize / linesInSample;
            long estimatedLines = (long) (fileSizeBytes / averageBytesPerLine);
            
            // Subtract header line if present
            return Math.max(0, estimatedLines - 1);
            
        } catch (Exception e) {
            logger.warn("Failed to estimate user count from sample, using default estimation", e);
            return fileSizeBytes / 100; // Fallback: 100 bytes per user
        }
    }
    
    @Override
    public List<FailedRecord> getFailedRecords(String jobId, boolean includeReprocessed) {
        logger.info("Retrieving failed records for job: {} (includeReprocessed: {})", jobId, includeReprocessed);
        
        try {
            List<FailedRecord> failedRecords = failedRecordService.getFailedRecords(jobId, includeReprocessed);
            logger.info("Found {} failed records for job: {}", failedRecords.size(), jobId);
            return failedRecords;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve failed records for job: {}", jobId, e);
            throw new RuntimeException("Failed to retrieve failed records: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ReprocessingResult reprocessFailedRecords(String jobId, List<FailedRecord> failedRecords) {
        logger.info("Reprocessing {} failed records for job: {}", failedRecords.size(), jobId);
        
        try {
            int successfullyProcessed = 0;
            int stillFailed = 0;
            
            for (FailedRecord failedRecord : failedRecords) {
                try {
                    // Attempt to reprocess the failed record
                    boolean success = failedRecordService.reprocessFailedRecord(failedRecord);
                    if (success) {
                        successfullyProcessed++;
                    } else {
                        stillFailed++;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to reprocess record from line {}: {}", 
                            failedRecord.getLineNumber(), e.getMessage());
                    stillFailed++;
                }
                
                // Heartbeat to prevent activity timeout
                if ((successfullyProcessed + stillFailed) % 100 == 0) {
                    Activity.getExecutionContext().heartbeat(null);
                }
            }
            
            logger.info("Reprocessing completed for job: {}. Success: {}, Still failed: {}", 
                    jobId, successfullyProcessed, stillFailed);
            
            return new ReprocessingResult(failedRecords.size(), successfullyProcessed, stillFailed);
            
        } catch (Exception e) {
            logger.error("Failed to reprocess records for job: {}", jobId, e);
            throw new RuntimeException("Reprocessing failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void finalizeJob(String jobId) {
        logger.info("Finalizing job: {}", jobId);
        
        try {
            // Perform final job cleanup and status update
            jobStatusService.finalizeJob(jobId);
            
            // Clean up temporary resources
            cleanup(jobId);
            
            logger.info("Job finalization completed for: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Failed to finalize job: {}", jobId, e);
            throw new RuntimeException("Job finalization failed: " + e.getMessage(), e);
        }
    }
    
    private void cleanup(String jobId) {
        try {
            // Clean up any temporary caches or resources
            logger.debug("Performing cleanup for job: {}", jobId);
            
            // Clear deduplication cache for this job
            // Remove temporary files if any
            // Clean up metrics/monitoring data older than retention period
            
        } catch (Exception e) {
            logger.warn("Cleanup failed for job: {}", jobId, e);
            // Don't fail the entire job for cleanup issues
        }
    }
}
