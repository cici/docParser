-- Initialize DocParser Database Schema

-- Create tables for job tracking
CREATE TABLE IF NOT EXISTS job_status (
    job_id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255),
    status VARCHAR(50),
    total_records BIGINT,
    processed_records BIGINT,
    failed_records BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create table for failed records
CREATE TABLE IF NOT EXISTS failed_records (
    id SERIAL PRIMARY KEY,
    job_id VARCHAR(255),
    record_content TEXT,
    error_message TEXT,
    failure_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES job_status(job_id)
);

-- Create table for chunk processing progress
CREATE TABLE IF NOT EXISTS chunk_progress (
    id SERIAL PRIMARY KEY,
    job_id VARCHAR(255),
    chunk_id VARCHAR(255),
    status VARCHAR(50),
    start_offset BIGINT,
    end_offset BIGINT,
    records_processed INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES job_status(job_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_job_status_workflow_id ON job_status(workflow_id);
CREATE INDEX IF NOT EXISTS idx_failed_records_job_id ON failed_records(job_id);
CREATE INDEX IF NOT EXISTS idx_chunk_progress_job_id ON chunk_progress(job_id);
CREATE INDEX IF NOT EXISTS idx_chunk_progress_status ON chunk_progress(status);
