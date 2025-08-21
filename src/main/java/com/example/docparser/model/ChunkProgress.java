package com.example.docparser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Tracks progress for individual chunk processing.
 * Used to monitor parallel chunk execution and enable restart capability.
 */
public class ChunkProgress {
    
    public enum ChunkStatus {
        PENDING,
        READING,
        PROCESSING,
        COMPLETED,
        FAILED,
        RETRYING
    }
    
    private final String jobId;
    private final int chunkIndex;
    private final long startOffset;
    private final long endOffset;
    private final ChunkStatus status;
    private final long totalUsers;
    private final long processedUsers;
    private final long validUsers;
    private final long invalidUsers;
    private final long duplicateUsers;
    private final Instant startTime;
    private final Instant endTime;
    private final String errorMessage;
    private final int retryAttempt;
    
    @JsonCreator
    public ChunkProgress(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("chunkIndex") int chunkIndex,
            @JsonProperty("startOffset") long startOffset,
            @JsonProperty("endOffset") long endOffset,
            @JsonProperty("status") ChunkStatus status,
            @JsonProperty("totalUsers") long totalUsers,
            @JsonProperty("processedUsers") long processedUsers,
            @JsonProperty("validUsers") long validUsers,
            @JsonProperty("invalidUsers") long invalidUsers,
            @JsonProperty("duplicateUsers") long duplicateUsers,
            @JsonProperty("startTime") Instant startTime,
            @JsonProperty("endTime") Instant endTime,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("retryAttempt") int retryAttempt) {
        this.jobId = jobId;
        this.chunkIndex = chunkIndex;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.status = status;
        this.totalUsers = totalUsers;
        this.processedUsers = processedUsers;
        this.validUsers = validUsers;
        this.invalidUsers = invalidUsers;
        this.duplicateUsers = duplicateUsers;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorMessage = errorMessage;
        this.retryAttempt = retryAttempt;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public int getChunkIndex() {
        return chunkIndex;
    }
    
    public long getStartOffset() {
        return startOffset;
    }
    
    public long getEndOffset() {
        return endOffset;
    }
    
    public ChunkStatus getStatus() {
        return status;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public long getProcessedUsers() {
        return processedUsers;
    }
    
    public long getValidUsers() {
        return validUsers;
    }
    
    public long getInvalidUsers() {
        return invalidUsers;
    }
    
    public long getDuplicateUsers() {
        return duplicateUsers;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getRetryAttempt() {
        return retryAttempt;
    }
    
    @JsonIgnore
    public long getChunkSize() {
        return endOffset - startOffset;
    }
    
    @JsonIgnore
    public double getProgressPercentage() {
        if (totalUsers == 0) return 0.0;
        return (double) processedUsers / totalUsers * 100.0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkProgress that = (ChunkProgress) o;
        return chunkIndex == that.chunkIndex && Objects.equals(jobId, that.jobId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jobId, chunkIndex);
    }
    
    @Override
    public String toString() {
        return "ChunkProgress{" +
                "jobId='" + jobId + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", status=" + status +
                ", progress=" + String.format("%.2f", getProgressPercentage()) + "%" +
                ", processedUsers=" + processedUsers +
                ", totalUsers=" + totalUsers +
                ", retryAttempt=" + retryAttempt +
                '}';
    }
    
    /**
     * Builder for creating ChunkProgress instances
     */
    public static class Builder {
        private String jobId;
        private int chunkIndex;
        private long startOffset;
        private long endOffset;
        private ChunkStatus status = ChunkStatus.PENDING;
        private long totalUsers = 0;
        private long processedUsers = 0;
        private long validUsers = 0;
        private long invalidUsers = 0;
        private long duplicateUsers = 0;
        private Instant startTime;
        private Instant endTime;
        private String errorMessage;
        private int retryAttempt = 0;
        
        public Builder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }
        
        public Builder chunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
            return this;
        }
        
        public Builder startOffset(long startOffset) {
            this.startOffset = startOffset;
            return this;
        }
        
        public Builder endOffset(long endOffset) {
            this.endOffset = endOffset;
            return this;
        }
        
        public Builder status(ChunkStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder totalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
            return this;
        }
        
        public Builder processedUsers(long processedUsers) {
            this.processedUsers = processedUsers;
            return this;
        }
        
        public Builder validUsers(long validUsers) {
            this.validUsers = validUsers;
            return this;
        }
        
        public Builder invalidUsers(long invalidUsers) {
            this.invalidUsers = invalidUsers;
            return this;
        }
        
        public Builder duplicateUsers(long duplicateUsers) {
            this.duplicateUsers = duplicateUsers;
            return this;
        }
        
        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder retryAttempt(int retryAttempt) {
            this.retryAttempt = retryAttempt;
            return this;
        }
        
        public ChunkProgress build() {
            return new ChunkProgress(jobId, chunkIndex, startOffset, endOffset, status,
                    totalUsers, processedUsers, validUsers, invalidUsers, duplicateUsers,
                    startTime, endTime, errorMessage, retryAttempt);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
