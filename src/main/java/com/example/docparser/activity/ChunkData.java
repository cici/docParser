package com.example.docparser.activity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chunk data read from file
 */
public class ChunkData {
    private byte[] data;
    private long userCount;
    private long bytesRead;
    
    // Default constructor for Jackson
    public ChunkData() {
    }
    
    @JsonCreator
    public ChunkData(
            @JsonProperty("data") byte[] data, 
            @JsonProperty("userCount") long userCount, 
            @JsonProperty("bytesRead") long bytesRead) {
        this.data = data;
        this.userCount = userCount;
        this.bytesRead = bytesRead;
    }
    
    public byte[] getData() { return data; }
    public long getUserCount() { return userCount; }
    public long getBytesRead() { return bytesRead; }
    
    // Setters for Jackson
    public void setData(byte[] data) { this.data = data; }
    public void setUserCount(long userCount) { this.userCount = userCount; }
    public void setBytesRead(long bytesRead) { this.bytesRead = bytesRead; }
}
