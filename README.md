# Document Parser - Large Scale CSV Processing with Temporal

A scalable solution for processing large CSV files (500MB+, 30M+ records) using Temporal workflows, Spring Boot, and AWS S3. The system provides fault-tolerant, parallel processing with progress tracking, deduplication, and failure handling.

## Architecture Overview

The solution uses a hierarchical workflow design:

- **Main Workflow**: Orchestrates the entire file processing job
- **Chunk Workflows**: Process individual chunks in parallel for maximum throughput
- **Activities**: Handle actual work (S3 operations, validation, database operations)
- **Progress Tracking**: Real-time progress updates at job and chunk levels
- **Fault Tolerance**: Automatic retries, restart capability, and failure isolation

### Key Features

- ✅ **Parallel Processing**: Process up to 20 chunks simultaneously
- ✅ **Fault Tolerance**: Automatic retries with configurable policies
- ✅ **Progress Tracking**: Real-time progress monitoring via REST APIs
- ✅ **Deduplication**: User-level deduplication based on ID
- ✅ **Restart Capability**: Resume processing from where it left off
- ✅ **Failure Handling**: Failed records stored for manual review and reprocessing
- ✅ **Scalability**: Horizontal scaling via Temporal Cloud workers

## Quick Start

### Prerequisites

- Java 17+
- Gradle 8.4+
- PostgreSQL 13+ (optional for demo)
- Temporal Cloud account (or local Temporal server)

### Local Development Setup (Demo Mode)

1. **Clone and build the project**:
```bash
git clone <repository-url>
cd docParser
./gradlew build
```

2. **Start with Docker Compose (Easiest)**:
```bash
# This starts everything: PostgreSQL, Temporal server, and the application
docker-compose up -d

# Wait for services to start, then check health
curl http://localhost:8080/actuator/health
```

3. **OR Manual Setup**:

**Set up PostgreSQL database** (optional for basic demo):
```sql
CREATE DATABASE docparser;
CREATE USER docparser WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE docparser TO docparser;
```

**Configure environment variables**:
```bash
# Database (optional for demo)
export DB_USERNAME=docparser
export DB_PASSWORD=password

# Local file processing (demo mode)
export FILE_BASE_DIR=./data

# Temporal (use local server or cloud)
export TEMPORAL_ENDPOINT=localhost:7233
export TEMPORAL_NAMESPACE=default
# For Temporal Cloud, add your credentials:
# export TEMPORAL_CLIENT_CERT_PATH=/path/to/client.pem
# export TEMPORAL_CLIENT_KEY_PATH=/path/to/client.key
```

**Start the application**:
```bash
# Start the REST API
./gradlew runApp

# In another terminal, start the worker
./gradlew runWorker
```

## API Usage

### Start File Processing

**Demo with sample file**:
```bash
curl -X POST http://localhost:8080/api/v1/file-processing/start \
  -H "Content-Type: application/json" \
  -d '{
    "directory": "demo",
    "filename": "sample-users.csv",
    "chunkSizeMB": 1,
    "maxParallelChunks": 2,
    "enableDeduplication": true,
    "reprocessFailures": true
  }'
```

**With your own CSV file**:
```bash
# Place your CSV file in ./data/mydata/users.csv, then:
curl -X POST http://localhost:8080/api/v1/file-processing/start \
  -H "Content-Type: application/json" \
  -d '{
    "directory": "mydata",
    "filename": "users.csv",
    "chunkSizeMB": 100,
    "maxParallelChunks": 10,
    "enableDeduplication": true,
    "reprocessFailures": true
  }'
```

### Monitor Progress

```bash
# Get basic job status
curl http://localhost:8080/api/v1/file-processing/jobs/{jobId}/status

# Get detailed progress with chunk information
curl http://localhost:8080/api/v1/file-processing/jobs/{jobId}/progress
```

### Control Job Execution

```bash
# Pause processing
curl -X POST http://localhost:8080/api/v1/file-processing/jobs/{jobId}/pause

# Resume processing
curl -X POST http://localhost:8080/api/v1/file-processing/jobs/{jobId}/resume

# Cancel processing
curl -X POST http://localhost:8080/api/v1/file-processing/jobs/{jobId}/cancel
```

## CSV File Format

The system expects CSV files with the following required columns:
- `id`: Unique user identifier (used for deduplication)
- `name`: User's full name
- `email`: User's email address
- `company_name`: Company name
- `address`: User's address

Example CSV:
```csv
id,name,email,company_name,address
1,John Doe,john.doe@example.com,ACME Corp,123 Main St
2,Jane Smith,jane.smith@example.com,Tech Inc,456 Oak Ave
```

## Configuration

### Application Configuration

Key configuration options in `application.yml`:

```yaml
app:
  file-processing:
    default-chunk-size-mb: 100        # Default chunk size
    max-parallel-chunks: 10           # Max parallel chunk processing
    enable-deduplication: true        # Enable user deduplication
    batch-size: 1000                 # Users processed per batch

temporal:
  worker:
    max-concurrent-activities: 50     # Max concurrent activities per worker
    max-concurrent-workflows: 20      # Max concurrent workflows per worker
```

### Worker Scaling

For production deployments, scale workers horizontally:

```yaml
# Production configuration
temporal:
  worker:
    max-concurrent-activities: 100
    max-concurrent-workflows: 50

app:
  file-processing:
    max-parallel-chunks: 20
```

### Temporal Cloud Setup

1. **Create a Temporal Cloud account** at [cloud.temporal.io](https://cloud.temporal.io)

2. **Generate client certificates**:
   - Download the client certificate and key files
   - Place them in a secure location
   - Update the configuration with the file paths

3. **Configure connection**:
```yaml
temporal:
  cloud:
    endpoint: your-namespace.tmprl.cloud:7233
    namespace: your-namespace
    client-cert: /path/to/client.pem
    client-key: /path/to/client.key
```

## Monitoring and Observability

### Health Checks

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

The application exposes Prometheus metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Temporal Web UI

Monitor workflows in the Temporal Web UI:
- Local: http://localhost:8088
- Cloud: https://cloud.temporal.io

## Performance Tuning

### For 30M Users (500MB file):

**Recommended Configuration**:
- Chunk Size: 100MB (approximately 6M users per chunk)
- Parallel Chunks: 10-20 (depending on worker capacity)
- Worker Instances: 3-5 instances
- Activities per Worker: 50-100
- Batch Size: 1000 users

**Expected Performance**:
- Processing Time: 30-60 minutes (depending on validation complexity)
- Memory Usage: ~2GB per worker instance
- Database Connections: 20 per instance

### Scaling Guidelines

| File Size | Users | Recommended Workers | Parallel Chunks | Expected Time |
|-----------|-------|-------------------|-----------------|---------------|
| 100MB     | 6M    | 2                 | 5               | 10-15 min     |
| 500MB     | 30M   | 3-5               | 10-15           | 30-60 min     |
| 1GB       | 60M   | 5-8               | 15-20           | 60-120 min    |

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Reduce chunk size or batch size
2. **Database connection timeout**: Increase connection pool size
3. **S3 throttling**: Implement exponential backoff (already included)
4. **Temporal worker timeout**: Increase activity timeout

### Debugging

Enable debug logging:
```yaml
logging:
  level:
    com.example.docparser: DEBUG
    io.temporal: DEBUG
```

### Failed Records

Failed records are stored in the database for manual review:
```sql
SELECT * FROM failed_records WHERE job_id = 'your-job-id';
```

## Deployment

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim

COPY build/libs/doc-parser-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment

Deploy API and workers separately for better scaling:

```yaml
# API Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: docparser-api
spec:
  replicas: 2
  selector:
    matchLabels:
      app: docparser-api
  template:
    spec:
      containers:
      - name: api
        image: docparser:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"

---
# Worker Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: docparser-worker
spec:
  replicas: 5
  selector:
    matchLabels:
      app: docparser-worker
  template:
    spec:
      containers:
      - name: worker
        image: docparser:latest
        command: ["java", "-jar", "/app.jar", "--spring.main.class=com.example.docparser.worker.TemporalWorkerApplication"]
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
