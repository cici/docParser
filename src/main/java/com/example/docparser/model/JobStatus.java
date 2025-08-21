package com.example.docparser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the overall status of a file processing job.
 * This is used for progress tracking and restart capability.
 */
public class JobStatus {
    
    public enum Status {
        STARTED,
        ANALYZING_FILE,
        PROCESSING_CHUNKS,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    private final String jobId;
    private final String directory;
    private final String filename;
    private final Status status;
    private final long totalUsers;
    private final long processedUsers;
    private final long validUsers;
    private final long invalidUsers;
    private final long duplicateUsers;
    private final int totalChunks;
    private final int completedChunks;
    private final Instant startTime;
    private final Instant lastUpdateTime;
    private final String errorMessage;
    
    @JsonCreator
    public JobStatus(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("directory") String directory,
            @JsonProperty("filename") String filename,
            @JsonProperty("status") Status status,
            @JsonProperty("totalUsers") long totalUsers,
            @JsonProperty("processedUsers") long processedUsers,
            @JsonProperty("validUsers") long validUsers,
            @JsonProperty("invalidUsers") long invalidUsers,
            @JsonProperty("duplicateUsers") long duplicateUsers,
            @JsonProperty("totalChunks") int totalChunks,
            @JsonProperty("completedChunks") int completedChunks,
            @JsonProperty("startTime") Instant startTime,
            @JsonProperty("lastUpdateTime") Instant lastUpdateTime,
            @JsonProperty("errorMessage") String errorMessage) {
        this.jobId = jobId;
        this.directory = directory;
        this.filename = filename;
        this.status = status;
        this.totalUsers = totalUsers;
        this.processedUsers = processedUsers;
        this.validUsers = validUsers;
        this.invalidUsers = invalidUsers;
        this.duplicateUsers = duplicateUsers;
        this.totalChunks = totalChunks;
        this.completedChunks = completedChunks;
        this.startTime = startTime;
        this.lastUpdateTime = lastUpdateTime;
        this.errorMessage = errorMessage;
    }
    
    public String getJobId() {
        return jobId;
    }
    
    public String getDirectory() {
        return directory;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public Status getStatus() {
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
    
    public int getTotalChunks() {
        return totalChunks;
    }
    
    public int getCompletedChunks() {
        return completedChunks;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @JsonIgnore
    public double getProgressPercentage() {
        if (totalUsers == 0) return 0.0;
        return (double) processedUsers / totalUsers * 100.0;
    }
    
    @JsonIgnore
    public double getChunkProgressPercentage() {
        if (totalChunks == 0) return 0.0;
        return (double) completedChunks / totalChunks * 100.0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobStatus jobStatus = (JobStatus) o;
        return Objects.equals(jobId, jobStatus.jobId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
    
    @Override
    public String toString() {
        return "JobStatus{" +
                "jobId='" + jobId + '\'' +
                ", status=" + status +
                ", progress=" + String.format("%.2f", getProgressPercentage()) + "%" +
                ", processedUsers=" + processedUsers +
                ", totalUsers=" + totalUsers +
                ", completedChunks=" + completedChunks +
                ", totalChunks=" + totalChunks +
                '}';
    }
    
    /**
     * Builder for creating JobStatus instances
     */
    public static class Builder {
        private String jobId;
        private String directory;
        private String filename;
        private Status status = Status.STARTED;
        private long totalUsers = 0;
        private long processedUsers = 0;
        private long validUsers = 0;
        private long invalidUsers = 0;
        private long duplicateUsers = 0;
        private int totalChunks = 0;
        private int completedChunks = 0;
        private Instant startTime = Instant.now();
        private Instant lastUpdateTime = Instant.now();
        private String errorMessage;
        
        public Builder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }
        
        public Builder directory(String directory) {
            this.directory = directory;
            return this;
        }
        
        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }
        
        public Builder status(Status status) {
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
        
        public Builder totalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
            return this;
        }
        
        public Builder completedChunks(int completedChunks) {
            this.completedChunks = completedChunks;
            return this;
        }
        
        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder lastUpdateTime(Instant lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public JobStatus build() {
            return new JobStatus(jobId, directory, filename, status, totalUsers, processedUsers,
                    validUsers, invalidUsers, duplicateUsers, totalChunks, completedChunks,
                    startTime, lastUpdateTime, errorMessage);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a new JobStatus with updated values
     */
    public JobStatus withUpdates(Builder updates) {
        return new Builder()
                .jobId(this.jobId)
                .directory(this.directory)
                .filename(this.filename)
                .status(updates.status != null ? updates.status : this.status)
                .totalUsers(updates.totalUsers != 0 ? updates.totalUsers : this.totalUsers)
                .processedUsers(updates.processedUsers != 0 ? updates.processedUsers : this.processedUsers)
                .validUsers(updates.validUsers != 0 ? updates.validUsers : this.validUsers)
                .invalidUsers(updates.invalidUsers != 0 ? updates.invalidUsers : this.invalidUsers)
                .duplicateUsers(updates.duplicateUsers != 0 ? updates.duplicateUsers : this.duplicateUsers)
                .totalChunks(updates.totalChunks != 0 ? updates.totalChunks : this.totalChunks)
                .completedChunks(updates.completedChunks != 0 ? updates.completedChunks : this.completedChunks)
                .startTime(this.startTime)
                .lastUpdateTime(Instant.now())
                .errorMessage(updates.errorMessage != null ? updates.errorMessage : this.errorMessage)
                .build();
    }
}
