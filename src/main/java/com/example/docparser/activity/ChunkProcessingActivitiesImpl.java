package com.example.docparser.activity;

import com.example.docparser.model.ChunkProgress;
import com.example.docparser.service.FileService;
import io.temporal.activity.Activity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of chunk processing activities.
 * Handles reading CSV chunks, validation, and user processing.
 */
@Component
public class ChunkProcessingActivitiesImpl implements ChunkProcessingActivities {
    
    private static final Logger logger = LoggerFactory.getLogger(ChunkProcessingActivitiesImpl.class);
    
    @Autowired
    private FileService fileService;
    
    @Override
    public ChunkBoundaries calculateChunkBoundaries(String directory, String filename, int chunkIndex, long chunkSizeBytes) {
        logger.debug("Calculating boundaries for chunk {} in {}/{}", chunkIndex, directory, filename);
        
        try {
            long fileSize = fileService.getFileSize(directory, filename);
            
            // Calculate rough start and end offsets
            long startOffset = chunkIndex * chunkSizeBytes;
            long endOffset = Math.min(startOffset + chunkSizeBytes, fileSize);
            
            // Adjust boundaries to avoid splitting CSV rows
            if (startOffset > 0) {
                // Find the start of the next line after startOffset
                long adjustedStart = fileService.findNextNewline(directory, filename, startOffset, 1024);
                if (adjustedStart != -1) {
                    startOffset = adjustedStart;
                }
            }
            
            if (endOffset < fileSize) {
                // Find the end of the current line at endOffset
                long adjustedEnd = fileService.findNextNewline(directory, filename, endOffset, 1024);
                if (adjustedEnd != -1) {
                    endOffset = adjustedEnd;
                }
            }
            
            long actualChunkSize = endOffset - startOffset;
            logger.debug("Chunk {} boundaries: {}-{} ({} bytes)", chunkIndex, startOffset, endOffset, actualChunkSize);
            
            return new ChunkBoundaries(startOffset, endOffset, actualChunkSize);
            
        } catch (Exception e) {
            logger.error("Failed to calculate chunk boundaries for chunk {} in {}/{}", chunkIndex, directory, filename, e);
            throw new RuntimeException("Failed to calculate chunk boundaries: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ChunkData readChunkFromFile(String directory, String filename, long startOffset, long endOffset) {
        logger.debug("Reading chunk data from {}/{} (bytes {}-{})", directory, filename, startOffset, endOffset);
        
        try {
            // Read the chunk data
            byte[] data = fileService.readFileRange(directory, filename, startOffset, endOffset);
            String csvContent = new String(data);
            
            // Count users (lines) in the chunk
            long userCount = countLinesInChunk(csvContent, startOffset == 0);
            
            logger.debug("Read {} bytes with {} users from {}/{}", data.length, userCount, directory, filename);
            
            return new ChunkData(data, userCount, data.length);
            
        } catch (Exception e) {
            logger.error("Failed to read chunk from {}/{} (bytes {}-{})", directory, filename, startOffset, endOffset, e);
            throw new RuntimeException("Failed to read chunk data: " + e.getMessage(), e);
        }
    }
    
    @Override
    public BatchProcessingResult processUserBatch(String jobId, int chunkIndex, long batchStart, long batchEnd, boolean enableDeduplication) {
        logger.debug("Processing user batch {}-{} for job {} chunk {}", batchStart, batchEnd, jobId, chunkIndex);
        
        try {
            // Demo implementation - simulate processing
            long batchSize = batchEnd - batchStart;
            
            // Simulate validation and processing with realistic success rates
            long processedCount = batchSize;
            long validCount = Math.round(batchSize * 0.90);      // 90% validation success
            long invalidCount = Math.round(batchSize * 0.08);    // 8% validation failures
            long duplicateCount = Math.round(batchSize * 0.02);  // 2% duplicates
            
            // Heartbeat to prevent activity timeout
            Activity.getExecutionContext().heartbeat(null);
            
            // Simulate processing time
            try {
                Thread.sleep(100); // 100ms per batch
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            logger.debug("Processed batch {}-{}: {} valid, {} invalid, {} duplicates", 
                    batchStart, batchEnd, validCount, invalidCount, duplicateCount);
            
            return new BatchProcessingResult(processedCount, validCount, invalidCount, duplicateCount);
            
        } catch (Exception e) {
            logger.error("Failed to process user batch {}-{} for job {} chunk {}", 
                    batchStart, batchEnd, jobId, chunkIndex, e);
            throw new RuntimeException("Failed to process user batch: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void updateChunkProgress(ChunkProgress progress) {
        logger.debug("Updating progress for job {} chunk {}: {}/{} users processed", 
                progress.getJobId(), progress.getChunkIndex(), 
                progress.getProcessedUsers(), progress.getTotalUsers());
        
        // Demo implementation - in production this would update the database
        // INSERT INTO chunk_progress (...) VALUES (...) ON CONFLICT UPDATE
        
        // Heartbeat to show activity is alive
        Activity.getExecutionContext().heartbeat(progress);
    }
    
    @Override
    public void recordChunkFailure(ChunkProgress progress) {
        logger.warn("Recording chunk failure for job {} chunk {}: {}", 
                progress.getJobId(), progress.getChunkIndex(), progress.getErrorMessage());
        
        // Demo implementation - in production this would:
        // - Store failure details in database
        // - Create alerts/notifications
        // - Update metrics
    }
    
    @Override
    public void finalizeChunk(String jobId, int chunkIndex) {
        logger.debug("Finalizing chunk {} for job {}", chunkIndex, jobId);
        
        // Demo implementation - in production this would:
        // - Update chunk status to completed
        // - Clean up temporary resources
        // - Update job progress
        // - Trigger any post-processing
    }
    
    /**
     * Count the number of lines (users) in a CSV chunk
     * @param csvContent CSV content as string
     * @param includeHeader Whether this chunk includes the header row
     * @return Number of user rows (excluding header if present)
     */
    private long countLinesInChunk(String csvContent, boolean includeHeader) {
        try {
            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(new StringReader(csvContent));
            
            AtomicLong count = new AtomicLong(0);
            
            for (CSVRecord record : parser) {
                // Skip empty rows
                if (record.size() > 0 && !record.get(0).trim().isEmpty()) {
                    count.incrementAndGet();
                }
            }
            
            // If this is the first chunk and includes header, the count is already correct
            // If this is not the first chunk but we're treating first line as header, subtract 1
            if (!includeHeader && count.get() > 0) {
                return count.get() - 1;
            }
            
            return count.get();
            
        } catch (Exception e) {
            logger.warn("Failed to parse CSV for line counting, using newline count fallback", e);
            // Fallback: count newlines
            long lineCount = csvContent.chars().mapToLong(c -> c == '\n' ? 1 : 0).sum();
            return includeHeader ? Math.max(0, lineCount - 1) : lineCount;
        }
    }
}
