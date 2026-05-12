name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  JAVA_VERSION: '${javaVersion}'
  REGISTRY: ghcr.io
  IMAGE_NAME: ${"${{ github.repository }}"}

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: ${artifactId}
          POSTGRES_USER: ${artifactId}
          POSTGRES_PASSWORD: ${artifactId}
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '${javaVersion}'
          distribution: 'temurin'
<#if buildTool == "MAVEN">
          cache: 'maven'
      - name: Run tests
        run: ./mvnw clean verify -Dspring.profiles.active=test
      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: target/site/jacoco/
<#else>
          cache: 'gradle'
      - name: Run tests
        run: ./gradlew clean test -Dspring.profiles.active=test
      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: build/reports/jacoco/
</#if>

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.event_name == 'push'
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '${javaVersion}'
          distribution: 'temurin'
<#if buildTool == "MAVEN">
      - name: Build JAR
        run: ./mvnw clean package -DskipTests
<#else>
      - name: Build JAR
        run: ./gradlew bootJar -x test
</#if>
      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${"${{ env.REGISTRY }}"}
          username: ${"${{ github.actor }}"}
          password: ${"${{ secrets.GITHUB_TOKEN }}"}
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${"${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}"}
            ${"${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest"}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to production
        run: echo "Deploy step - configure with your deployment target"
