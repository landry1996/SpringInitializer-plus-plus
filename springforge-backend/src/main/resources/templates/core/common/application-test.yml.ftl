spring:
  datasource:
    url: jdbc:tc:postgresql:16:///test_db
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
<#if cacheType?? && cacheType == "REDIS">
  data:
    redis:
      host: localhost
      port: 6379
</#if>
<#if messaging?? && messaging == "KAFKA">
  kafka:
    bootstrap-servers: localhost:9092
</#if>

logging:
  level:
    ${packageName}: DEBUG
    org.springframework: WARN
