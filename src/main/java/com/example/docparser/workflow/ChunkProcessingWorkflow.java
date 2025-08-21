package com.example.docparser.workflow;

import com.example.docparser.model.ChunkProgress;
import com.example.docparser.model.FileProcessingRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for processing individual chunks of the CSV file.
 * Each chunk is processed independently for maximum parallelism and fault tolerance.
 */
@WorkflowInterface
public interface ChunkProcessingWorkflow {
    
    /**
     * Process a specific chunk of the CSV file
     * @param request The original file processing request
     * @param chunkIndex The index of the chunk to process (0-based)
     * @return Final chunk progress status
     */
    @WorkflowMethod
    ChunkProgress processChunk(FileProcessingRequest request, int chunkIndex);
    
    /**
     * Query method to get current chunk progress
     * @return Current chunk progress
     */
    @QueryMethod
    ChunkProgress getChunkProgress();
}
