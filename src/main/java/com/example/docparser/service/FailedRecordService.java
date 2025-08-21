package com.example.docparser.service;

import com.example.docparser.model.FailedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing failed record storage and reprocessing.
 * Demo implementation - in production this would use a database.
 */
@Service
public class FailedRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(FailedRecordService.class);
    
    /**
     * Get failed records for a job
     * @param jobId Job identifier
     * @param includeReprocessed Whether to include already reprocessed records
     * @return List of failed records
     */
    public List<FailedRecord> getFailedRecords(String jobId, boolean includeReprocessed) {
        logger.info("Getting failed records for job: {} (includeReprocessed: {})", jobId, includeReprocessed);
        
        // Demo implementation - return empty list
        // In production, this would query the database:
        // SELECT * FROM failed_records WHERE job_id = ? AND (reprocessed = false OR ?)
        
        return new ArrayList<>();
    }
    
    /**
     * Attempt to reprocess a failed record
     * @param failedRecord The failed record to reprocess
     * @return true if successful, false if still failed
     */
    public boolean reprocessFailedRecord(FailedRecord failedRecord) {
        logger.info("Reprocessing failed record: job={}, line={}", 
                failedRecord.getJobId(), failedRecord.getLineNumber());
        
        // Demo implementation - simulate 70% success rate
        // In production, this would:
        // - Re-parse the CSV row
        // - Re-validate the user data
        // - Attempt to save to database
        // - Update the failed_record status
        
        boolean success = Math.random() > 0.3; // 70% success rate
        
        if (success) {
            logger.info("Successfully reprocessed record: job={}, line={}", 
                    failedRecord.getJobId(), failedRecord.getLineNumber());
        } else {
            logger.warn("Failed to reprocess record: job={}, line={}", 
                    failedRecord.getJobId(), failedRecord.getLineNumber());
        }
        
        return success;
    }
}
