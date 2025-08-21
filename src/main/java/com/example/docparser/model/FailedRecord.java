package com.example.docparser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents a failed user record that couldn't be processed.
 * Contains validation errors and original data for manual review.
 */
public class FailedRecord {
    
    public enum FailureType {
        VALIDATION_ERROR,
        PROCESSING_ERROR,
        DUPLICATE_USER,
        PARSE_ERROR
    }
    
    private final String jobId;
    private final int chunkIndex;
    private final long lineNumber;
    private final String rawCsvRow;
    private final FailureType failureType;
    private final List<String> validationErrors;
    private final String errorMessage;
    private final Instant failureTime;
    private final boolean reprocessed;
    private final String userId; // May be null if parsing failed
    
    @JsonCreator
    public FailedRecord(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("chunkIndex") int chunkIndex,
            @JsonProperty("lineNumber") long lineNumber,
            @JsonProperty("rawCsvRow") String rawCsvRow,
            @JsonProperty("failureType") FailureType failureType,
            @JsonProperty("validationErrors") List<String> validationErrors,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("failureTime") Instant failureTime,
            @JsonProperty("reprocessed") boolean reprocessed,
            @JsonProperty("userId") String userId) {
        this.jobId = jobId;
        this.chunkIndex = chunkIndex;
        this.lineNumber = lineNumber;
        this.rawCsvRow = rawCsvRow;
        this.failureType = failureType;
        this.validationErrors = validationErrors;
        this.errorMessage = errorMessage;
        this.failureTime = failureTime;
        this.reprocessed = reprocessed;
        this.userId = userId;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public int getChunkIndex() {
        return chunkIndex;
    }
    
    public long getLineNumber() {
        return lineNumber;
    }
    
    public String getRawCsvRow() {
        return rawCsvRow;
    }
    
    public FailureType getFailureType() {
        return failureType;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Instant getFailureTime() {
        return failureTime;
    }
    
    public boolean isReprocessed() {
        return reprocessed;
    }
    
    public String getUserId() {
        return userId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailedRecord that = (FailedRecord) o;
        return lineNumber == that.lineNumber && 
               chunkIndex == that.chunkIndex && 
               Objects.equals(jobId, that.jobId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jobId, chunkIndex, lineNumber);
    }
    
    @Override
    public String toString() {
        return "FailedRecord{" +
                "jobId='" + jobId + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", lineNumber=" + lineNumber +
                ", failureType=" + failureType +
                ", userId='" + userId + '\'' +
                ", reprocessed=" + reprocessed +
                '}';
    }
    
    /**
     * Builder for creating FailedRecord instances
     */
    public static class Builder {
        private String jobId;
        private int chunkIndex;
        private long lineNumber;
        private String rawCsvRow;
        private FailureType failureType;
        private List<String> validationErrors;
        private String errorMessage;
        private Instant failureTime = Instant.now();
        private boolean reprocessed = false;
        private String userId;
        
        public Builder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }
        
        public Builder chunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
            return this;
        }
        
        public Builder lineNumber(long lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
        
        public Builder rawCsvRow(String rawCsvRow) {
            this.rawCsvRow = rawCsvRow;
            return this;
        }
        
        public Builder failureType(FailureType failureType) {
            this.failureType = failureType;
            return this;
        }
        
        public Builder validationErrors(List<String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder failureTime(Instant failureTime) {
            this.failureTime = failureTime;
            return this;
        }
        
        public Builder reprocessed(boolean reprocessed) {
            this.reprocessed = reprocessed;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public FailedRecord build() {
            return new FailedRecord(jobId, chunkIndex, lineNumber, rawCsvRow, failureType,
                    validationErrors, errorMessage, failureTime, reprocessed, userId);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
