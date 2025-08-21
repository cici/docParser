package com.example.docparser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Request model for starting file processing workflow.
 * Contains local file location and processing configuration.
 */
public class FileProcessingRequest {
    
    private final String jobId;
    private final String directory;
    private final String filename;
    private final long chunkSizeBytes;
    private final int maxParallelChunks;
    private final boolean enableDeduplication;
    private final boolean reprocessFailures;
    
    @JsonCreator
    public FileProcessingRequest(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("directory") String directory,
            @JsonProperty("filename") String filename,
            @JsonProperty("chunkSizeBytes") long chunkSizeBytes,
            @JsonProperty("maxParallelChunks") int maxParallelChunks,
            @JsonProperty("enableDeduplication") boolean enableDeduplication,
            @JsonProperty("reprocessFailures") boolean reprocessFailures) {
        this.jobId = jobId;
        this.directory = directory;
        this.filename = filename;
        this.chunkSizeBytes = chunkSizeBytes;
        this.maxParallelChunks = maxParallelChunks;
        this.enableDeduplication = enableDeduplication;
        this.reprocessFailures = reprocessFailures;
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
    
    public long getChunkSizeBytes() {
        return chunkSizeBytes;
    }
    
    public int getMaxParallelChunks() {
        return maxParallelChunks;
    }
    
    public boolean isEnableDeduplication() {
        return enableDeduplication;
    }
    
    public boolean isReprocessFailures() {
        return reprocessFailures;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileProcessingRequest that = (FileProcessingRequest) o;
        return Objects.equals(jobId, that.jobId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
    
    @Override
    public String toString() {
        return "FileProcessingRequest{" +
                "jobId='" + jobId + '\'' +
                ", directory='" + directory + '\'' +
                ", filename='" + filename + '\'' +
                ", chunkSizeBytes=" + chunkSizeBytes +
                ", maxParallelChunks=" + maxParallelChunks +
                ", enableDeduplication=" + enableDeduplication +
                ", reprocessFailures=" + reprocessFailures +
                '}';
    }
    
    /**
     * Builder for creating FileProcessingRequest instances
     */
    public static class Builder {
        private String jobId;
        private String directory;
        private String filename;
        private long chunkSizeBytes = 100 * 1024 * 1024; // 100MB default
        private int maxParallelChunks = 10;
        private boolean enableDeduplication = true;
        private boolean reprocessFailures = true;
        
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
        
        public Builder chunkSizeBytes(long chunkSizeBytes) {
            this.chunkSizeBytes = chunkSizeBytes;
            return this;
        }
        
        public Builder maxParallelChunks(int maxParallelChunks) {
            this.maxParallelChunks = maxParallelChunks;
            return this;
        }
        
        public Builder enableDeduplication(boolean enableDeduplication) {
            this.enableDeduplication = enableDeduplication;
            return this;
        }
        
        public Builder reprocessFailures(boolean reprocessFailures) {
            this.reprocessFailures = reprocessFailures;
            return this;
        }
        
        public FileProcessingRequest build() {
            return new FileProcessingRequest(jobId, directory, filename, chunkSizeBytes,
                    maxParallelChunks, enableDeduplication, reprocessFailures);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
