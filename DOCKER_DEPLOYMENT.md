# Docker Deployment Guide

This document provides comprehensive instructions for deploying the Autotroph service using Docker, both locally and on Render.com.

## 🐳 Docker Configuration

### Files Added/Modified

- **`Dockerfile`** - Multi-stage build configuration for production deployment
- **`.dockerignore`** - Excludes unnecessary files from Docker build context

- **`render.yaml`** - Render.com deployment configuration
- **`docker-compose.yml`** - Updated to include application service
- **`.env.example`** - Environment variables template
- **`docker-build.sh`** - Local Docker build script
- **`settings.gradle.kts`** - Gradle settings file

## 🚀 Local Development

### Option 1: Docker Compose (Recommended)

```bash
# Start both Neo4j and application
docker-compose up -d

# View logs
docker-compose logs -f autotroph-service

# Stop everything
docker-compose down
```

### Option 2: Manual Docker Build

```bash
# Build the image
./docker-build.sh
# or
docker build -t autotroph-service:latest .

# Run with external Neo4j
docker run -p 8080:8080 \
  -e NEO4J_URI=bolt://host.docker.internal:7687 \
  -e NEO4J_USERNAME=neo4j \
  -e NEO4J_PASSWORD=password \
  -e NEO4J_DATABASE=neo4j \
  -e OPENAI_API_KEY=your-api-key \
  autotroph-service:latest
```

## ☁️ Render.com Deployment

### Prerequisites

1. Fork this repository to your GitHub account
2. Create a [Render.com](https://render.com) account
3. Set up a Neo4j database (see Neo4j Setup section below)
4. Obtain an OpenAI API key

### Neo4j Database Setup

You need an external Neo4j database. Here are the recommended options:

### Deployment Steps

1. **Connect Repository:**
   - Go to Render.com dashboard
   - Click "New" → "Blueprint"
   - Connect your GitHub account
   - Select your forked repository
   - Render will detect the `render.yaml` configuration

2. **Configure Environment Variables:**
   - In Render dashboard, go to your service → Environment
   - Set the following variables:
     - `NEO4J_URI`: Your Neo4j connection string (e.g., `neo4j+s://your-db.databases.neo4j.io`)
     - `NEO4J_USERNAME`: Your Neo4j username
     - `NEO4J_PASSWORD`: Your Neo4j password
     - `OPENAI_API_KEY`: Your OpenAI API key

3. **Deploy:**
   - Render automatically builds and deploys the application
   - Application connects to your external Neo4j database
   - Application auto-scales based on traffic

### Service Architecture

- **Web Service**: Kotlin/Ktor application (autotroph-service)
- **Database**: External Neo4j database (Neo4j Aura recommended)
- **Health Checks**: Automatic monitoring on `/api/v1/health`

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `NEO4J_URI` | `bolt://localhost:7687` | Neo4j connection URI |
| `NEO4J_USERNAME` | `neo4j` | Neo4j username |
| `NEO4J_PASSWORD` | `password` | Neo4j password |
| `NEO4J_DATABASE` | `neo4j` | Neo4j database name |
| `SERVER_PORT` | `8080` | Application server port |
| `OPENAI_API_KEY` | - | OpenAI API key (required) |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | JVM options |

### Docker Image Details

- **Base Images**: 
  - Build: `gradle:8.5-jdk21`
  - Runtime: `eclipse-temurin:21-jre-jammy`
- **Size**: ~1.74GB (includes Playwright browsers)
- **User**: Non-root user (`appuser`)
- **Health Check**: Built-in health monitoring
- **Playwright**: Pre-installed Chromium browser

## 🧪 Testing

### Local Testing

```bash
# Build and test locally
docker-compose up -d

# Check application health
curl http://localhost:8080/api/v1/health

# Check Neo4j
curl http://localhost:7474
```

### Production Testing

```bash
# Test your deployed service
curl https://your-app.onrender.com/api/v1/health
```

## 🔍 Troubleshooting

### Common Issues

1. **Playwright Browser Issues**
   - Browsers are pre-installed in the Docker image
   - If issues persist, check container logs

2. **Neo4j Connection Issues**
   - Verify environment variables are set correctly
   - Check Neo4j service is running and accessible

3. **Memory Issues**
   - Adjust `JAVA_OPTS` for your deployment size
   - Render Starter plan has 512MB RAM limit

### Logs

```bash
# Local logs
docker-compose logs -f autotroph-service
docker-compose logs -f neo4j

# Render logs
# Available in Render dashboard under your service
```

## 📊 Performance

### Resource Usage

- **CPU**: Optimized for multi-core usage
- **Memory**: 512MB-1GB recommended
- **Storage**: 10GB for Neo4j data
- **Network**: HTTP/HTTPS on port 8080

### Scaling

- Render.com provides automatic scaling
- Neo4j runs as a single instance with persistent storage
- Application can scale horizontally

## 🔒 Security

- Non-root container user
- Environment variable-based configuration
- HTTPS termination handled by Render.com
- Neo4j authentication required

## 📝 Next Steps

1. Deploy to Render.com using the provided configuration
2. Set up monitoring and alerting
3. Configure custom domain (optional)
4. Set up CI/CD pipeline for automated deployments
5. Consider backup strategy for Neo4j data

For additional support, refer to the main README.md or create an issue in the repository.
