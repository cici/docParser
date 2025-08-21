package com.example.docparser.controller;

import com.example.docparser.model.FileProcessingRequest;
import com.example.docparser.model.JobStatus;
import com.example.docparser.service.FileProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST controller for file processing operations.
 * Provides endpoints for starting jobs, monitoring progress, and managing workflows.
 */
@RestController
@RequestMapping("/api/v1/file-processing")
@Tag(name = "File Processing", description = "APIs for large file processing with Temporal workflows")
public class FileProcessingController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileProcessingController.class);
    
    @Autowired
    private FileProcessingService fileProcessingService;
    
    @PostMapping("/start")
    @Operation(
        summary = "Start file processing job",
        description = "Initiates processing of a CSV file stored in S3. Returns a job ID for tracking progress."
    )
    @ApiResponse(responseCode = "200", description = "Job started successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<JobStartResponse> startFileProcessing(
            @Valid @RequestBody FileProcessingStartRequest request) {
        
        logger.info("Starting file processing for {}/{}", request.getDirectory(), request.getFilename());
        
        try {
            // Generate unique job ID
            String jobId = UUID.randomUUID().toString();
            
            // Create file processing request
            FileProcessingRequest processingRequest = FileProcessingRequest.builder()
                    .jobId(jobId)
                    .directory(request.getDirectory())
                    .filename(request.getFilename())
                    .chunkSizeBytes(request.getChunkSizeMB() * 1024 * 1024L)
                    .maxParallelChunks(request.getMaxParallelChunks())
                    .enableDeduplication(request.isEnableDeduplication())
                    .reprocessFailures(request.isReprocessFailures())
                    .build();
            
            // Start the workflow
            String workflowId = fileProcessingService.startFileProcessing(processingRequest);
            
            JobStartResponse response = new JobStartResponse(jobId, workflowId, "Job started successfully");
            logger.info("File processing job started: {} (workflow: {})", jobId, workflowId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to start file processing for {}/{}", 
                    request.getDirectory(), request.getFilename(), e);
            return ResponseEntity.internalServerError()
                    .body(new JobStartResponse(null, null, "Failed to start job: " + e.getMessage()));
        }
    }
    
    @GetMapping("/jobs/{jobId}/status")
    @Operation(
        summary = "Get job status",
        description = "Retrieves current status and progress information for a file processing job."
    )
    @ApiResponse(responseCode = "200", description = "Job status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    public ResponseEntity<JobStatus> getJobStatus(
            @Parameter(description = "Unique job identifier") @PathVariable String jobId) {
        
        logger.debug("Getting status for job: {}", jobId);
        
        try {
            JobStatus status = fileProcessingService.getJobStatus(jobId);
            if (status == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get status for job: {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/jobs/{jobId}/progress")
    @Operation(
        summary = "Get detailed job progress",
        description = "Retrieves detailed progress information including chunk-level status."
    )
    @ApiResponse(responseCode = "200", description = "Progress information retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    public ResponseEntity<JobStatus> getDetailedProgress(
            @Parameter(description = "Unique job identifier") @PathVariable String jobId) {
        
        logger.debug("Getting detailed progress for job: {}", jobId);
        
        try {
            JobStatus progress = fileProcessingService.getDetailedProgress(jobId);
            if (progress == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            logger.error("Failed to get detailed progress for job: {}", jobId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/jobs/{jobId}/pause")
    @Operation(
        summary = "Pause job processing",
        description = "Pauses a running file processing job. Processing can be resumed later."
    )
    @ApiResponse(responseCode = "200", description = "Job paused successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    public ResponseEntity<String> pauseJob(
            @Parameter(description = "Unique job identifier") @PathVariable String jobId) {
        
        logger.info("Pausing job: {}", jobId);
        
        try {
            fileProcessingService.pauseJob(jobId);
            return ResponseEntity.ok("Job paused successfully");
            
        } catch (Exception e) {
            logger.error("Failed to pause job: {}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to pause job: " + e.getMessage());
        }
    }
    
    @PostMapping("/jobs/{jobId}/resume")
    @Operation(
        summary = "Resume job processing",
        description = "Resumes a paused file processing job."
    )
    @ApiResponse(responseCode = "200", description = "Job resumed successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    public ResponseEntity<String> resumeJob(
            @Parameter(description = "Unique job identifier") @PathVariable String jobId) {
        
        logger.info("Resuming job: {}", jobId);
        
        try {
            fileProcessingService.resumeJob(jobId);
            return ResponseEntity.ok("Job resumed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to resume job: {}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to resume job: " + e.getMessage());
        }
    }
    
    @PostMapping("/jobs/{jobId}/cancel")
    @Operation(
        summary = "Cancel job processing",
        description = "Cancels a running or paused file processing job."
    )
    @ApiResponse(responseCode = "200", description = "Job cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Job not found")
    public ResponseEntity<String> cancelJob(
            @Parameter(description = "Unique job identifier") @PathVariable String jobId) {
        
        logger.info("Cancelling job: {}", jobId);
        
        try {
            fileProcessingService.cancelJob(jobId);
            return ResponseEntity.ok("Job cancelled successfully");
            
        } catch (Exception e) {
            logger.error("Failed to cancel job: {}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to cancel job: " + e.getMessage());
        }
    }
    
    /**
     * Request model for starting file processing
     */
    public static class FileProcessingStartRequest {
        @jakarta.validation.constraints.NotBlank
        private String directory;
        
        @jakarta.validation.constraints.NotBlank
        private String filename;
        
        private int chunkSizeMB = 100;
        private int maxParallelChunks = 10;
        private boolean enableDeduplication = true;
        private boolean reprocessFailures = true;
        
        // Getters and setters
        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public int getChunkSizeMB() { return chunkSizeMB; }
        public void setChunkSizeMB(int chunkSizeMB) { this.chunkSizeMB = chunkSizeMB; }
        
        public int getMaxParallelChunks() { return maxParallelChunks; }
        public void setMaxParallelChunks(int maxParallelChunks) { this.maxParallelChunks = maxParallelChunks; }
        
        public boolean isEnableDeduplication() { return enableDeduplication; }
        public void setEnableDeduplication(boolean enableDeduplication) { this.enableDeduplication = enableDeduplication; }
        
        public boolean isReprocessFailures() { return reprocessFailures; }
        public void setReprocessFailures(boolean reprocessFailures) { this.reprocessFailures = reprocessFailures; }
    }
    
    /**
     * Response model for job start
     */
    public static class JobStartResponse {
        private final String jobId;
        private final String workflowId;
        private final String message;
        
        public JobStartResponse(String jobId, String workflowId, String message) {
            this.jobId = jobId;
            this.workflowId = workflowId;
            this.message = message;
        }
        
        public String getJobId() { return jobId; }
        public String getWorkflowId() { return workflowId; }
        public String getMessage() { return message; }
    }
}
