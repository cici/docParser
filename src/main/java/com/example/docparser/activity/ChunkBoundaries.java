package com.example.docparser.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chunk boundaries ensuring CSV row integrity
 */
public class ChunkBoundaries {
    private long startOffset;
    private long endOffset;
    private long actualChunkSize;
    
    // Default constructor for Jackson
    public ChunkBoundaries() {
    }
    
    @JsonCreator
    public ChunkBoundaries(
            @JsonProperty("startOffset") long startOffset, 
            @JsonProperty("endOffset") long endOffset, 
            @JsonProperty("actualChunkSize") long actualChunkSize) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.actualChunkSize = actualChunkSize;
    }
    
    public long getStartOffset() { return startOffset; }
    public long getEndOffset() { return endOffset; }
    public long getActualChunkSize() { return actualChunkSize; }
    
    // Setters for Jackson
    public void setStartOffset(long startOffset) { this.startOffset = startOffset; }
    public void setEndOffset(long endOffset) { this.endOffset = endOffset; }
    public void setActualChunkSize(long actualChunkSize) { this.actualChunkSize = actualChunkSize; }
}
