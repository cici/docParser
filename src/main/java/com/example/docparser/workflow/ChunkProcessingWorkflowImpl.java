package com.example.docparser.workflow;

import com.example.docparser.activity.ChunkProcessingActivities;
import com.example.docparser.model.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Implementation of chunk processing workflow.
 * Handles reading, validation, and processing of individual CSV chunks.
 */
public class ChunkProcessingWorkflowImpl implements ChunkProcessingWorkflow {
    
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingWorkflowImpl.class);
    
    private ChunkProgress currentProgress;
    
    // Activity stub with chunk-specific retry configuration
    private final ChunkProcessingActivities activities = Workflow.newActivityStub(
            ChunkProcessingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofHours(2)) // Longer timeout for chunk processing
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(30))
                            .setMaximumInterval(Duration.ofMinutes(10))
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );
    
    // Activity stub for quick operations (validation, deduplication)
    private final ChunkProcessingActivities quickActivities = Workflow.newActivityStub(
            ChunkProcessingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(5))
                            .setMaximumInterval(Duration.ofMinutes(2))
                            .setBackoffCoefficient(1.5)
                            .setMaximumAttempts(5)
                            .build())
                    .build()
    );
    
    @Override
    public ChunkProgress processChunk(FileProcessingRequest request, int chunkIndex) {
        String workflowId = Workflow.getInfo().getWorkflowId();
        logger.info("Starting chunk processing workflow: {} for chunk {}", workflowId, chunkIndex);
        
        try {
            // Initialize chunk progress
            currentProgress = ChunkProgress.builder()
                    .jobId(request.getJobId())
                    .chunkIndex(chunkIndex)
                    .status(ChunkProgress.ChunkStatus.PENDING)
                    .startTime(Instant.now())
                    .build();
            
            // Step 1: Calculate chunk boundaries
            currentProgress = calculateChunkBoundaries(request, chunkIndex);
            
            // Step 2: Read chunk data from S3
            currentProgress = readChunkData(request);
            
            // Step 3: Process users in the chunk
            currentProgress = processUsers(request);
            
            // Step 4: Finalize chunk processing
            currentProgress = finalizeChunk();
            
            logger.info("Chunk {} processing completed successfully. Processed {} users", 
                    chunkIndex, currentProgress.getProcessedUsers());
            
            return currentProgress;
            
        } catch (Exception e) {
            logger.error("Chunk {} processing failed", chunkIndex, e);
            
            currentProgress = ChunkProgress.builder()
                    .jobId(request.getJobId())
                    .chunkIndex(chunkIndex)
                    .status(ChunkProgress.ChunkStatus.FAILED)
                    .startTime(currentProgress != null ? currentProgress.getStartTime() : Instant.now())
                    .endTime(Instant.now())
                    .errorMessage(e.getMessage())
                    .retryAttempt(currentProgress != null ? currentProgress.getRetryAttempt() + 1 : 1)
                    .build();
            
            // Store failed chunk information for later retry
            activities.recordChunkFailure(currentProgress);
            
            throw e;
        }
    }
    
    private ChunkProgress calculateChunkBoundaries(FileProcessingRequest request, int chunkIndex) {
        logger.debug("Calculating boundaries for chunk {}", chunkIndex);
        
        var chunkBoundaries = activities.calculateChunkBoundaries(
                request.getDirectory(), 
                request.getFilename(), 
                chunkIndex, 
                request.getChunkSizeBytes()
        );
        
        return ChunkProgress.builder()
                .jobId(request.getJobId())
                .chunkIndex(chunkIndex)
                .startOffset(chunkBoundaries.getStartOffset())
                .endOffset(chunkBoundaries.getEndOffset())
                .status(ChunkProgress.ChunkStatus.READING)
                .startTime(currentProgress.getStartTime())
                .build();
    }
    
    private ChunkProgress readChunkData(FileProcessingRequest request) {
        logger.debug("Reading chunk data for chunk {} (bytes {}-{})", 
                currentProgress.getChunkIndex(), currentProgress.getStartOffset(), currentProgress.getEndOffset());
        
        // Read CSV chunk from local file
        var chunkData = activities.readChunkFromFile(
                request.getDirectory(),
                request.getFilename(),
                currentProgress.getStartOffset(),
                currentProgress.getEndOffset()
        );
        
        return ChunkProgress.builder()
                .jobId(currentProgress.getJobId())
                .chunkIndex(currentProgress.getChunkIndex())
                .startOffset(currentProgress.getStartOffset())
                .endOffset(currentProgress.getEndOffset())
                .totalUsers(chunkData.getUserCount())
                .status(ChunkProgress.ChunkStatus.PROCESSING)
                .startTime(currentProgress.getStartTime())
                .build();
    }
    
    private ChunkProgress processUsers(FileProcessingRequest request) {
        logger.debug("Processing {} users in chunk {}", 
                currentProgress.getTotalUsers(), currentProgress.getChunkIndex());
        
        // Process users in batches for better memory management
        long processedUsers = 0;
        long validUsers = 0;
        long invalidUsers = 0;
        long duplicateUsers = 0;
        
        int batchSize = 1000; // Process 1000 users at a time
        long totalUsers = currentProgress.getTotalUsers();
        
        for (long offset = 0; offset < totalUsers; offset += batchSize) {
            long batchEnd = Math.min(offset + batchSize, totalUsers);
            
            // Process batch of users
            var batchResult = activities.processUserBatch(
                    request.getJobId(),
                    currentProgress.getChunkIndex(),
                    offset,
                    batchEnd,
                    request.isEnableDeduplication()
            );
            
            // Accumulate results
            processedUsers += batchResult.getProcessedCount();
            validUsers += batchResult.getValidCount();
            invalidUsers += batchResult.getInvalidCount();
            duplicateUsers += batchResult.getDuplicateCount();
            
            // Update progress periodically
            if (offset % (batchSize * 10) == 0 || batchEnd == totalUsers) {
                currentProgress = ChunkProgress.builder()
                        .jobId(currentProgress.getJobId())
                        .chunkIndex(currentProgress.getChunkIndex())
                        .startOffset(currentProgress.getStartOffset())
                        .endOffset(currentProgress.getEndOffset())
                        .totalUsers(currentProgress.getTotalUsers())
                        .processedUsers(processedUsers)
                        .validUsers(validUsers)
                        .invalidUsers(invalidUsers)
                        .duplicateUsers(duplicateUsers)
                        .status(ChunkProgress.ChunkStatus.PROCESSING)
                        .startTime(currentProgress.getStartTime())
                        .build();
                
                // Report progress to main workflow
                activities.updateChunkProgress(currentProgress);
                
                logger.debug("Chunk {} progress: {}/{} users processed", 
                        currentProgress.getChunkIndex(), processedUsers, totalUsers);
            }
        }
        
        return ChunkProgress.builder()
                .jobId(currentProgress.getJobId())
                .chunkIndex(currentProgress.getChunkIndex())
                .startOffset(currentProgress.getStartOffset())
                .endOffset(currentProgress.getEndOffset())
                .totalUsers(currentProgress.getTotalUsers())
                .processedUsers(processedUsers)
                .validUsers(validUsers)
                .invalidUsers(invalidUsers)
                .duplicateUsers(duplicateUsers)
                .status(ChunkProgress.ChunkStatus.PROCESSING)
                .startTime(currentProgress.getStartTime())
                .build();
    }
    
    private ChunkProgress finalizeChunk() {
        logger.debug("Finalizing chunk {}", currentProgress.getChunkIndex());
        
        // Perform final chunk cleanup and validation
        activities.finalizeChunk(currentProgress.getJobId(), currentProgress.getChunkIndex());
        
        return ChunkProgress.builder()
                .jobId(currentProgress.getJobId())
                .chunkIndex(currentProgress.getChunkIndex())
                .startOffset(currentProgress.getStartOffset())
                .endOffset(currentProgress.getEndOffset())
                .totalUsers(currentProgress.getTotalUsers())
                .processedUsers(currentProgress.getProcessedUsers())
                .validUsers(currentProgress.getValidUsers())
                .invalidUsers(currentProgress.getInvalidUsers())
                .duplicateUsers(currentProgress.getDuplicateUsers())
                .status(ChunkProgress.ChunkStatus.COMPLETED)
                .startTime(currentProgress.getStartTime())
                .endTime(Instant.now())
                .build();
    }
    
    @Override
    public ChunkProgress getChunkProgress() {
        return currentProgress;
    }
}
