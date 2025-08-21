package com.example.docparser.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * File analysis result containing chunk information
 */
public class FileAnalysisResult {
    private long fileSizeBytes;
    private long estimatedUserCount;
    private int totalChunks;
    private long chunkSizeBytes;
    
    // Default constructor for Jackson
    public FileAnalysisResult() {
    }
    
    @JsonCreator
    public FileAnalysisResult(
            @JsonProperty("fileSizeBytes") long fileSizeBytes, 
            @JsonProperty("estimatedUserCount") long estimatedUserCount, 
            @JsonProperty("totalChunks") int totalChunks, 
            @JsonProperty("chunkSizeBytes") long chunkSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
        this.estimatedUserCount = estimatedUserCount;
        this.totalChunks = totalChunks;
        this.chunkSizeBytes = chunkSizeBytes;
    }
    
    public long getFileSizeBytes() { return fileSizeBytes; }
    public long getEstimatedUserCount() { return estimatedUserCount; }
    public int getTotalChunks() { return totalChunks; }
    public long getChunkSizeBytes() { return chunkSizeBytes; }
    
    // Setters for Jackson
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public void setEstimatedUserCount(long estimatedUserCount) { this.estimatedUserCount = estimatedUserCount; }
    public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
    public void setChunkSizeBytes(long chunkSizeBytes) { this.chunkSizeBytes = chunkSizeBytes; }
}
