# Subscription Service

![Architecture Diagram](https://github.com/user-attachments/assets/226b9505-d8ae-46b3-9dc6-3d285539cabd)

This project is a microservice developed for subscription management system. It is built using Spring Boot and configured to run on Kubernetes.

## Technologies

- Java 17
- Spring Boot 3.x
- PostgreSQL
- Kafka
- Docker
- Kubernetes
- Maven

## Project Structure

```
subscription-service/
├── src/                    # Source code
├── k8s/                    # Kubernetes manifest files
│   ├── deployment.yaml     # Deployment configuration
│   ├── service.yaml        # Service configuration
│   ├── configmap.yaml      # ConfigMap configuration
│   └── secret.yaml         # Secret configuration
├── Dockerfile              # Docker image build file
├── docker-compose.yml      # Docker Compose for local development
└── pom.xml                 # Maven project configuration
```

## Development Environment Setup

1. Required software installation:
   - JDK 17
   - Maven
   - Docker
   - Docker Compose

2. Clone the project:
   ```bash
   git clone [repository-url]
   cd subscription-service
   ```

3. Start the application:
   ```bash
   docker-compose up -d
   ```

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster
- kubectl CLI tool
- Docker registry access

### Deployment Steps

1. Apply ConfigMap and Secrets:
   ```bash
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/secret.yaml
   ```

2. Apply Deployment and Service:
   ```bash
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   ```

3. Check deployment status:
   ```bash
   kubectl get deployments
   kubectl get pods
   kubectl get services
   ```

## Configuration

### Environment Variables

The application uses the following environment variables:

- `SPRING_PROFILES_ACTIVE`: Active Spring profile
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### Resource Limits

- CPU Request: 250m
- CPU Limit: 500m
- Memory Request: 512Mi
- Memory Limit: 1Gi

## Monitoring and Health Checks

The application provides health check endpoints using Spring Boot Actuator:

- Liveness Probe: `/actuator/health`
- Readiness Probe: `/actuator/health`

## Security

- Database credentials are stored in Kubernetes Secrets
- Remember to update Secrets in production environment
- Update sensitive information in ConfigMap for production environment

## License

[License information to be added]
