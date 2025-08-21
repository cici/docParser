package com.example.docparser.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of processing a batch of users
 */
public class BatchProcessingResult {
    private long processedCount;
    private long validCount;
    private long invalidCount;
    private long duplicateCount;
    
    // Default constructor for Jackson
    public BatchProcessingResult() {
    }
    
    @JsonCreator
    public BatchProcessingResult(
            @JsonProperty("processedCount") long processedCount, 
            @JsonProperty("validCount") long validCount, 
            @JsonProperty("invalidCount") long invalidCount, 
            @JsonProperty("duplicateCount") long duplicateCount) {
        this.processedCount = processedCount;
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.duplicateCount = duplicateCount;
    }
    
    public long getProcessedCount() { return processedCount; }
    public long getValidCount() { return validCount; }
    public long getInvalidCount() { return invalidCount; }
    public long getDuplicateCount() { return duplicateCount; }
    
    // Setters for Jackson
    public void setProcessedCount(long processedCount) { this.processedCount = processedCount; }
    public void setValidCount(long validCount) { this.validCount = validCount; }
    public void setInvalidCount(long invalidCount) { this.invalidCount = invalidCount; }
    public void setDuplicateCount(long duplicateCount) { this.duplicateCount = duplicateCount; }
}
