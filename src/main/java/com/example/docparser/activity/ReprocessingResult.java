package com.example.docparser.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of reprocessing failed records
 */
public class ReprocessingResult {
    private int totalRecords;
    private int successfullyProcessed;
    private int stillFailed;
    
    // Default constructor for Jackson
    public ReprocessingResult() {
    }
    
    @JsonCreator
    public ReprocessingResult(
            @JsonProperty("totalRecords") int totalRecords, 
            @JsonProperty("successfullyProcessed") int successfullyProcessed, 
            @JsonProperty("stillFailed") int stillFailed) {
        this.totalRecords = totalRecords;
        this.successfullyProcessed = successfullyProcessed;
        this.stillFailed = stillFailed;
    }
    
    public int getTotalRecords() { return totalRecords; }
    public int getSuccessfullyProcessed() { return successfullyProcessed; }
    public int getStillFailed() { return stillFailed; }
    
    // Setters for Jackson
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    public void setSuccessfullyProcessed(int successfullyProcessed) { this.successfullyProcessed = successfullyProcessed; }
    public void setStillFailed(int stillFailed) { this.stillFailed = stillFailed; }
}
