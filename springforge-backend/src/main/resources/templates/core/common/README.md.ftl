# ${projectName}

${projectDescription}

## Tech Stack

- **Java** ${javaVersion}
- **Spring Boot** ${springBootVersion}
- **Build Tool**: ${buildTool!"Maven"}
<#if architecture??>
- **Architecture**: ${architecture}
</#if>
<#if database??>
- **Database**: ${database.type!"PostgreSQL"}
</#if>
<#if messaging??>
- **Messaging**: ${messaging}
</#if>
<#if cacheType??>
- **Cache**: ${cacheType}
</#if>

## Getting Started

### Prerequisites

- JDK ${javaVersion}+
<#if buildTool?? && buildTool?contains("GRADLE")>
- Gradle 8.x (or use included wrapper)
<#else>
- Maven 3.9+ (or use included wrapper)
</#if>
- Docker & Docker Compose (for local development)

### Run Locally

```bash
# Start infrastructure
docker-compose up -d

# Run the application
<#if buildTool?? && buildTool?contains("GRADLE")>
./gradlew bootRun
<#else>
./mvnw spring-boot:run
</#if>
```

The application starts at `http://localhost:8080`.

### API Documentation

Once running, access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## Project Structure

```
${artifactId}/
├── src/
│   ├── main/
│   │   ├── java/${packageName?replace(".", "/")}
│   │   └── resources/
│   └── test/
<#if buildTool?? && buildTool?contains("GRADLE")>
├── build.gradle<#if buildTool == "GRADLE_KOTLIN">.kts</#if>
<#else>
├── pom.xml
</#if>
├── Dockerfile
├── docker-compose.yml
<#if kubernetes?? && kubernetes>
├── k8s/
</#if>
└── README.md
```

## Profiles

| Profile | Purpose |
|---------|---------|
| `dev` | Local development (H2/Docker DB, debug logging) |
| `test` | Testing (Testcontainers, mock services) |
| `prod` | Production (external DB, JSON logging, optimized) |

## Build & Deploy

```bash
# Build JAR
<#if buildTool?? && buildTool?contains("GRADLE")>
./gradlew build
<#else>
./mvnw clean package -DskipTests
</#if>

# Build Docker image
docker build -t ${artifactId}:latest .

# Run with Docker
docker run -p 8080:8080 ${artifactId}:latest
```
<#if kubernetes?? && kubernetes>

## Kubernetes Deployment

```bash
kubectl apply -f k8s/
```
</#if>

---

*Generated with [SpringForge](https://github.com/landry1996/SpringInitializer-plus-plus)*
