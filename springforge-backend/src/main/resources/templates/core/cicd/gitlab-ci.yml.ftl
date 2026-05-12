stages:
  - test
  - quality
  - build
  - deploy

variables:
  JAVA_VERSION: "${javaVersion}"
  DOCKER_IMAGE: ${"$CI_REGISTRY_IMAGE"}
<#if buildTool == "MAVEN">
  MAVEN_OPTS: "-Dmaven.repo.local=${"$CI_PROJECT_DIR"}/.m2/repository"
</#if>

<#if buildTool == "MAVEN">
cache:
  paths:
    - .m2/repository/
<#else>
cache:
  paths:
    - .gradle/
</#if>

test:
  stage: test
  image: eclipse-temurin:${javaVersion}-jdk
  services:
    - name: postgres:16-alpine
      alias: postgres
      variables:
        POSTGRES_DB: ${artifactId}
        POSTGRES_USER: ${artifactId}
        POSTGRES_PASSWORD: ${artifactId}
  variables:
    SPRING_PROFILES_ACTIVE: test
    SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/${artifactId}"
  script:
<#if buildTool == "MAVEN">
    - ./mvnw clean verify
<#else>
    - ./gradlew clean test
</#if>
  artifacts:
    reports:
<#if buildTool == "MAVEN">
      junit: target/surefire-reports/TEST-*.xml
<#else>
      junit: build/test-results/test/TEST-*.xml
</#if>
    expire_in: 7 days

quality:
  stage: quality
  image: eclipse-temurin:${javaVersion}-jdk
  script:
<#if buildTool == "MAVEN">
    - ./mvnw sonar:sonar -Dsonar.host.url=${"$SONAR_HOST_URL"} -Dsonar.token=${"$SONAR_TOKEN"}
<#else>
    - ./gradlew sonar -Dsonar.host.url=${"$SONAR_HOST_URL"} -Dsonar.token=${"$SONAR_TOKEN"}
</#if>
  allow_failure: true
  only:
    - main
    - develop

build:
  stage: build
  image: docker:24-dind
  services:
    - docker:24-dind
  script:
    - docker login -u ${"$CI_REGISTRY_USER"} -p ${"$CI_REGISTRY_PASSWORD"} ${"$CI_REGISTRY"}
    - docker build -t ${"$DOCKER_IMAGE:$CI_COMMIT_SHA"} -t ${"$DOCKER_IMAGE:latest"} .
    - docker push ${"$DOCKER_IMAGE:$CI_COMMIT_SHA"}
    - docker push ${"$DOCKER_IMAGE:latest"}
  only:
    - main
    - develop

deploy_staging:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/${artifactId} app=${"$DOCKER_IMAGE:$CI_COMMIT_SHA"} -n staging
  environment:
    name: staging
  only:
    - develop

deploy_production:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/${artifactId} app=${"$DOCKER_IMAGE:$CI_COMMIT_SHA"} -n production
  environment:
    name: production
  when: manual
  only:
    - main
