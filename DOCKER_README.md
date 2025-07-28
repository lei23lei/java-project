# Clothes Warehouse - Docker Setup

This document provides instructions for building, running, and deploying the Clothes Warehouse application using Docker.

## Prerequisites

- Docker installed on your system
- Docker Hub account (for pushing images)
- Maven (for local development)

## Quick Start

### 1. Build the Docker Image

```bash
# Build the image locally
docker build -t clothes-warehouse:latest .

# Or with a specific tag
docker build -t clothes-warehouse:v1.0.0 .
```

### 2. Run the Application

```bash
# Run with Docker Compose (recommended)
docker-compose up -d

# Or run directly with Docker
docker run -p 8080:8080 clothes-warehouse:latest
```

### 3. Access the Application

- **Main Application**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## Docker Hub Deployment

### 1. Tag Your Image

```bash
# Replace 'your-dockerhub-username' with your actual Docker Hub username
docker tag clothes-warehouse:latest your-dockerhub-username/clothes-warehouse:latest
docker tag clothes-warehouse:latest your-dockerhub-username/clothes-warehouse:v1.0.0
```

### 2. Login to Docker Hub

```bash
docker login
```

### 3. Push to Docker Hub

```bash
# Push the latest version
docker push your-dockerhub-username/clothes-warehouse:latest

# Push the tagged version
docker push your-dockerhub-username/clothes-warehouse:v1.0.0
```

### 4. Pull and Run from Docker Hub

```bash
# Pull the image
docker pull your-dockerhub-username/clothes-warehouse:latest

# Run the image
docker run -p 8080:8080 your-dockerhub-username/clothes-warehouse:latest
```

## Environment Variables

You can customize the application behavior using environment variables:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e JAVA_OPTS="-Xmx1g -Xms512m" \
  -e DISTRIBUTION_CENTER_API_URL=http://your-dc-api:8081/api/distribution-centers \
  clothes-warehouse:latest
```

### Available Environment Variables

- `SPRING_PROFILES_ACTIVE`: Spring profile to use (default: docker)
- `JAVA_OPTS`: JVM options (default: -Xmx512m -Xms256m)
- `DISTRIBUTION_CENTER_API_URL`: URL for the Distribution Center API
- `DISTRIBUTION_CENTER_API_USERNAME`: Username for DC API authentication
- `DISTRIBUTION_CENTER_API_PASSWORD`: Password for DC API authentication

## Docker Compose Options

### Basic Setup (H2 Database)

```bash
docker-compose up -d
```

### With PostgreSQL Database

```bash
docker-compose --profile postgres up -d
```

### Custom Configuration

Create a `docker-compose.override.yml` file for custom configurations:

```yaml
version: "3.8"
services:
  clothes-warehouse:
    environment:
      - JAVA_OPTS=-Xmx1g -Xms512m
      - DISTRIBUTION_CENTER_API_URL=http://your-custom-dc-api:8081/api/distribution-centers
```

## Production Deployment

### 1. Multi-Stage Build (Already configured)

The Dockerfile uses a multi-stage build to create a smaller production image:

- Build stage: Uses JDK to compile the application
- Runtime stage: Uses JRE for running the application

### 2. Security Best Practices

- The application runs as a non-root user (`javauser`)
- Health checks are configured
- Resource limits can be set via `JAVA_OPTS`

### 3. Resource Limits

```bash
docker run -p 8080:8080 \
  --memory=1g \
  --cpus=1.0 \
  clothes-warehouse:latest
```

## Troubleshooting

### Check Container Logs

```bash
# Using docker-compose
docker-compose logs clothes-warehouse

# Using docker
docker logs <container-id>
```

### Health Check

```bash
# Check if the application is healthy
curl http://localhost:8080/actuator/health
```

### Access H2 Console

1. Navigate to http://localhost:8080/h2-console
2. Use these connection details:
   - JDBC URL: `jdbc:h2:mem:clothes_warehouse_docker`
   - Username: `sa`
   - Password: (leave empty)

### Common Issues

1. **Port already in use**: Change the port mapping

   ```bash
   docker run -p 8081:8080 clothes-warehouse:latest
   ```

2. **Memory issues**: Increase JVM heap size

   ```bash
   docker run -e JAVA_OPTS="-Xmx1g -Xms512m" clothes-warehouse:latest
   ```

3. **Distribution Center API connection**: Ensure the DC API is running and accessible

## Development with Docker

### Hot Reload (Development)

For development with hot reload, mount the source code:

```bash
docker run -p 8080:8080 \
  -v $(pwd)/src:/app/src \
  -v $(pwd)/target:/app/target \
  clothes-warehouse:latest
```

### Debug Mode

```bash
docker run -p 8080:8080 -p 5005:5005 \
  -e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  clothes-warehouse:latest
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    tags:
      - "v*"

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build Docker image
        run: docker build -t clothes-warehouse:${{ github.ref_name }} .

      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_HUB_USERNAME }}
          password: ${{ secrets.DOCKER_HUB_TOKEN }}

      - name: Push to Docker Hub
        run: |
          docker tag clothes-warehouse:${{ github.ref_name }} your-username/clothes-warehouse:${{ github.ref_name }}
          docker push your-username/clothes-warehouse:${{ github.ref_name }}
```

## Image Optimization

The Dockerfile is optimized for:

- **Layer caching**: Dependencies are downloaded in a separate layer
- **Multi-stage build**: Smaller runtime image
- **Security**: Non-root user execution
- **Health monitoring**: Built-in health checks

## Support

For issues related to Docker deployment, check:

1. Container logs
2. Health check endpoint
3. Environment variables configuration
4. Network connectivity (for Distribution Center API)
