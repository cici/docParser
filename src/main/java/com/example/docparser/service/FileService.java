package com.example.docparser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for local file operations including file reading and metadata retrieval.
 * Optimized for large file processing with range requests using RandomAccessFile.
 * Demo version using local filesystem instead of S3.
 */
@Service
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    @Value("${app.file-processing.base-directory:./data}")
    private String baseDirectory;
    
    /**
     * Get the size of a local file
     * @param directory Directory name (replaces S3 bucket for demo)
     * @param filename File name (replaces S3 key for demo)
     * @return Size in bytes
     */
    public long getFileSize(String directory, String filename) {
        try {
            Path filePath = getFilePath(directory, filename);
            
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File does not exist: " + filePath);
            }
            
            long size = Files.size(filePath);
            logger.debug("File {}/{} size: {} bytes", directory, filename, size);
            return size;
            
        } catch (Exception e) {
            logger.error("Failed to get file size for {}/{}", directory, filename, e);
            throw new RuntimeException("Failed to get file size: " + e.getMessage(), e);
        }
    }
    
    /**
     * Read a range of bytes from a local file
     * @param directory Directory name (replaces S3 bucket for demo)
     * @param filename File name (replaces S3 key for demo)
     * @param startOffset Start byte offset (inclusive)
     * @param endOffset End byte offset (exclusive)
     * @return Byte array containing the requested range
     */
    public byte[] readFileRange(String directory, String filename, long startOffset, long endOffset) {
        if (startOffset >= endOffset) {
            throw new IllegalArgumentException("Start offset must be less than end offset");
        }
        
        try {
            Path filePath = getFilePath(directory, filename);
            
            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
                // Seek to start position
                file.seek(startOffset);
                
                // Calculate bytes to read
                long bytesToRead = endOffset - startOffset;
                byte[] buffer = new byte[(int) bytesToRead];
                
                int bytesRead = file.read(buffer);
                
                // Handle case where we read fewer bytes than expected (end of file)
                if (bytesRead < bytesToRead) {
                    byte[] result = new byte[bytesRead];
                    System.arraycopy(buffer, 0, result, 0, bytesRead);
                    buffer = result;
                }
                
                logger.debug("Read {} bytes from {}/{} range {}-{}", 
                        buffer.length, directory, filename, startOffset, endOffset - 1);
                
                return buffer;
            }
            
        } catch (IOException e) {
            logger.error("Failed to read range {}-{} from {}/{}", 
                    startOffset, endOffset - 1, directory, filename, e);
            throw new RuntimeException("Failed to read file range: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find the next newline character after a given offset
     * This ensures we don't split CSV rows when chunking
     * @param directory Directory name
     * @param filename File name
     * @param offset Starting offset to search from
     * @param maxSearchBytes Maximum bytes to search
     * @return Offset of the next newline, or -1 if not found
     */
    public long findNextNewline(String directory, String filename, long offset, int maxSearchBytes) {
        try {
            byte[] searchData = readFileRange(directory, filename, offset, offset + maxSearchBytes);
            
            for (int i = 0; i < searchData.length; i++) {
                if (searchData[i] == '\n') {
                    return offset + i + 1; // Return position after newline
                }
            }
            
            return -1; // No newline found in search range
            
        } catch (Exception e) {
            logger.warn("Failed to find newline after offset {} in {}/{}", offset, directory, filename, e);
            return -1;
        }
    }
    
    /**
     * Check if a local file exists
     * @param directory Directory name
     * @param filename File name
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String directory, String filename) {
        try {
            Path filePath = getFilePath(directory, filename);
            return Files.exists(filePath);
            
        } catch (Exception e) {
            logger.error("Failed to check if file exists: {}/{}", directory, filename, e);
            throw new RuntimeException("Failed to check file existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get file metadata (last modified time)
     * @param directory Directory name
     * @param filename File name
     * @return Last modified time in milliseconds
     */
    public long getFileLastModified(String directory, String filename) {
        try {
            Path filePath = getFilePath(directory, filename);
            return Files.getLastModifiedTime(filePath).toMillis();
                    
        } catch (Exception e) {
            logger.error("Failed to get metadata for {}/{}", directory, filename, e);
            throw new RuntimeException("Failed to get file metadata: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to construct full file path
     * @param directory Directory name
     * @param filename File name
     * @return Full path to file
     */
    private Path getFilePath(String directory, String filename) {
        return Paths.get(baseDirectory, directory, filename);
    }
    
    /**
     * Create directory if it doesn't exist
     * @param directory Directory name
     */
    public void createDirectoryIfNotExists(String directory) {
        try {
            Path dirPath = Paths.get(baseDirectory, directory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info("Created directory: {}", dirPath);
            }
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", directory, e);
            throw new RuntimeException("Failed to create directory: " + e.getMessage(), e);
        }
    }
}
