spring:
<#if mongodb?? && mongodb>
  data:
    mongodb:
      uri: mongodb://localhost:27017/test_db
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
<#else>
  datasource:
    url: jdbc:tc:postgresql:16:///test_db
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
</#if>
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

mongock:
  enabled: false

logging:
  level:
    ${packageName}: DEBUG
    org.springframework: WARN
