package com.example.docparser.workflow;

import com.example.docparser.activity.FileProcessingActivities;
import com.example.docparser.model.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the main file processing workflow.
 * Coordinates chunk processing workflows and manages overall job state.
 */
public class FileProcessingWorkflowImpl implements FileProcessingWorkflow {
    
    private static final Logger logger = LoggerFactory.getLogger(FileProcessingWorkflowImpl.class);
    
    private JobStatus currentJobStatus;
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final Map<Integer, ChunkProgress> chunkProgressMap = new HashMap<>();
    
    // Activity stub with retry configuration
    private final FileProcessingActivities activities = Workflow.newActivityStub(
            FileProcessingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(30))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(10))
                            .setMaximumInterval(Duration.ofMinutes(5))
                            .setBackoffCoefficient(2.0)
                            .setMaximumAttempts(5)
                            .build())
                    .build()
    );
    
    @Override
    public JobStatus processFile(FileProcessingRequest request) {
        logger.info("Starting file processing workflow for job: {}", request.getJobId());
        
        try {
            // Initialize job status
            currentJobStatus = JobStatus.builder()
                    .jobId(request.getJobId())
                    .directory(request.getDirectory())
                    .filename(request.getFilename())
                    .status(JobStatus.Status.STARTED)
                    .startTime(Instant.now())
                    .build();
            
            // Step 1: Analyze file and determine chunk strategy
            currentJobStatus = analyzeFile(request);
            
            // Step 2: Process chunks in parallel
            currentJobStatus = processChunksInParallel(request);
            
            // Step 3: Handle any failed records if reprocessing is enabled
            if (request.isReprocessFailures()) {
                currentJobStatus = reprocessFailures(request);
            }
            
            // Step 4: Finalize job
            currentJobStatus = finalizeJob();
            
            logger.info("File processing completed for job: {}", request.getJobId());
            return currentJobStatus;
            
        } catch (Exception e) {
            logger.error("File processing failed for job: {}", request.getJobId(), e);
            currentJobStatus = currentJobStatus.withUpdates(
                    JobStatus.builder()
                            .status(JobStatus.Status.FAILED)
                            .errorMessage(e.getMessage())
            );
            return currentJobStatus;
        }
    }
    
    private JobStatus analyzeFile(FileProcessingRequest request) {
        logger.info("Analyzing file: {}/{}", request.getDirectory(), request.getFilename());
        
        currentJobStatus = currentJobStatus.withUpdates(
                JobStatus.builder().status(JobStatus.Status.ANALYZING_FILE)
        );
        
        // Get file metadata and determine chunking strategy
        var fileAnalysis = activities.analyzeFile(request.getDirectory(), request.getFilename(), 
                request.getChunkSizeBytes());
        
        // Update job status with file analysis results
        return currentJobStatus.withUpdates(
                JobStatus.builder()
                        .totalUsers(fileAnalysis.getEstimatedUserCount())
                        .totalChunks(fileAnalysis.getTotalChunks())
                        .status(JobStatus.Status.PROCESSING_CHUNKS)
        );
    }
    
    private JobStatus processChunksInParallel(FileProcessingRequest request) {
        logger.info("Processing {} chunks in parallel (max: {})", 
                currentJobStatus.getTotalChunks(), request.getMaxParallelChunks());
        
        List<Promise<ChunkProgress>> chunkPromises = new ArrayList<>();
        AtomicInteger activeChunks = new AtomicInteger(0);
        AtomicInteger completedChunks = new AtomicInteger(0);
        
        // Process chunks with controlled parallelism
        for (int chunkIndex = 0; chunkIndex < currentJobStatus.getTotalChunks(); chunkIndex++) {
            final int currentChunkIndex = chunkIndex;
            
            // Wait if we've reached max parallel chunks
            while (activeChunks.get() >= request.getMaxParallelChunks()) {
                Workflow.await(() -> activeChunks.get() < request.getMaxParallelChunks());
            }
            
            // Check for pause/cancel signals
            checkForSignals();
            
            // Start chunk processing workflow
            activeChunks.incrementAndGet();
            
            ChunkProcessingWorkflow chunkWorkflow = Workflow.newChildWorkflowStub(
                    ChunkProcessingWorkflow.class,
                    ChildWorkflowOptions.newBuilder()
                            .setWorkflowId(request.getJobId() + "-chunk-" + currentChunkIndex)
                            .build()
            );
            
            Promise<ChunkProgress> chunkPromise = Async.function(chunkWorkflow::processChunk,
                    request, currentChunkIndex);
            
            chunkPromises.add(chunkPromise);
            
            // Set up completion handler for this chunk
            chunkPromise.thenApply(chunkProgress -> {
                activeChunks.decrementAndGet();
                completedChunks.incrementAndGet();
                
                // Update chunk progress
                chunkProgressMap.put(currentChunkIndex, chunkProgress);
                
                // Update overall job progress
                updateJobProgressFromChunks();
                
                logger.info("Chunk {} completed. Progress: {}/{} chunks", 
                        currentChunkIndex, completedChunks.get(), currentJobStatus.getTotalChunks());
                
                return chunkProgress;
            });
        }
        
        // Wait for all chunks to complete
        logger.info("Waiting for all chunks to complete...");
        Promise.allOf(chunkPromises).get();
        
        // Final progress update
        updateJobProgressFromChunks();
        
        return currentJobStatus.withUpdates(
                JobStatus.builder().completedChunks(currentJobStatus.getTotalChunks())
        );
    }
    
    private JobStatus reprocessFailures(FileProcessingRequest request) {
        logger.info("Reprocessing failed records for job: {}", request.getJobId());
        
        // Get failed records that haven't been reprocessed
        var failedRecords = activities.getFailedRecords(request.getJobId(), false);
        
        if (!failedRecords.isEmpty()) {
            logger.info("Found {} failed records to reprocess", failedRecords.size());
            
            // Reprocess failed records
            var reprocessResult = activities.reprocessFailedRecords(request.getJobId(), failedRecords);
            
            // Update job status with reprocessing results
            return currentJobStatus.withUpdates(
                    JobStatus.builder()
                            .validUsers(currentJobStatus.getValidUsers() + reprocessResult.getSuccessfullyProcessed())
                            .invalidUsers(currentJobStatus.getInvalidUsers() - reprocessResult.getSuccessfullyProcessed())
            );
        }
        
        return currentJobStatus;
    }
    
    private JobStatus finalizeJob() {
        logger.info("Finalizing job: {}", currentJobStatus.getJobId());
        
        // Perform final cleanup and status update
        activities.finalizeJob(currentJobStatus.getJobId());
        
        return currentJobStatus.withUpdates(
                JobStatus.builder().status(JobStatus.Status.COMPLETED)
        );
    }
    
    private void updateJobProgressFromChunks() {
        long totalProcessed = 0;
        long totalValid = 0;
        long totalInvalid = 0;
        long totalDuplicates = 0;
        
        for (ChunkProgress progress : chunkProgressMap.values()) {
            totalProcessed += progress.getProcessedUsers();
            totalValid += progress.getValidUsers();
            totalInvalid += progress.getInvalidUsers();
            totalDuplicates += progress.getDuplicateUsers();
        }
        
        currentJobStatus = currentJobStatus.withUpdates(
                JobStatus.builder()
                        .processedUsers(totalProcessed)
                        .validUsers(totalValid)
                        .invalidUsers(totalInvalid)
                        .duplicateUsers(totalDuplicates)
                        .completedChunks(chunkProgressMap.size())
        );
    }
    
    private void checkForSignals() {
        if (isCancelled.get()) {
            logger.info("Processing cancelled for job: {}", currentJobStatus.getJobId());
            throw new RuntimeException("Processing was cancelled");
        }
        
        if (isPaused.get()) {
            logger.info("Processing paused for job: {}", currentJobStatus.getJobId());
            Workflow.await(() -> !isPaused.get() || isCancelled.get());
            
            if (isCancelled.get()) {
                throw new RuntimeException("Processing was cancelled while paused");
            }
            logger.info("Processing resumed for job: {}", currentJobStatus.getJobId());
        }
    }
    
    @Override
    public JobStatus getJobStatus() {
        return currentJobStatus;
    }
    
    @Override
    public JobStatus getDetailedProgress() {
        // Include chunk-level progress information
        updateJobProgressFromChunks();
        return currentJobStatus;
    }
    
    @Override
    public void pauseProcessing() {
        logger.info("Pause signal received for job: {}", currentJobStatus.getJobId());
        isPaused.set(true);
    }
    
    @Override
    public void resumeProcessing() {
        logger.info("Resume signal received for job: {}", currentJobStatus.getJobId());
        isPaused.set(false);
    }
    
    @Override
    public void cancelProcessing() {
        logger.info("Cancel signal received for job: {}", currentJobStatus.getJobId());
        isCancelled.set(true);
        isPaused.set(false); // Wake up if paused
    }
}
