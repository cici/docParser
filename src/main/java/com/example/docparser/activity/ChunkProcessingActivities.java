package com.example.docparser.activity;

import com.example.docparser.model.ChunkProgress;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity interface for chunk-level processing operations.
 * These activities handle reading, validation, and processing of CSV chunks.
 */
@ActivityInterface
public interface ChunkProcessingActivities {
    
    /**
     * Calculate exact chunk boundaries ensuring we don't split CSV rows
     * @param directory Directory name (replaces S3 bucket for demo)
     * @param filename File name (replaces S3 key for demo)
     * @param chunkIndex Zero-based chunk index
     * @param chunkSizeBytes Target chunk size
     * @return Chunk boundaries
     */
    @ActivityMethod
    ChunkBoundaries calculateChunkBoundaries(String directory, String filename, int chunkIndex, long chunkSizeBytes);
    
    /**
     * Read CSV chunk data from local file
     * @param directory Directory name (replaces S3 bucket for demo)
     * @param filename File name (replaces S3 key for demo)
     * @param startOffset Start byte offset
     * @param endOffset End byte offset
     * @return Chunk data with user count
     */
    @ActivityMethod
    ChunkData readChunkFromFile(String directory, String filename, long startOffset, long endOffset);
    
    /**
     * Process a batch of users from the chunk
     * @param jobId Job identifier
     * @param chunkIndex Chunk index
     * @param batchStart Start index within chunk
     * @param batchEnd End index within chunk
     * @param enableDeduplication Whether to check for duplicates
     * @return Batch processing result
     */
    @ActivityMethod
    BatchProcessingResult processUserBatch(String jobId, int chunkIndex, long batchStart, long batchEnd, boolean enableDeduplication);
    
    /**
     * Update chunk progress in the database
     * @param progress Current chunk progress
     */
    @ActivityMethod
    void updateChunkProgress(ChunkProgress progress);
    
    /**
     * Record chunk failure for later analysis
     * @param progress Failed chunk progress
     */
    @ActivityMethod
    void recordChunkFailure(ChunkProgress progress);
    
    /**
     * Finalize chunk processing
     * @param jobId Job identifier
     * @param chunkIndex Chunk index
     */
    @ActivityMethod
    void finalizeChunk(String jobId, int chunkIndex);
}
