spring:
<#if mongodb?? && mongodb>
  data:
    mongodb:
      uri: mongodb://${artifactId}:${artifactId}@localhost:27017/${artifactId}?authSource=admin
<#else>
  datasource:
    url: jdbc:postgresql://localhost:5432/${artifactId}
    username: ${'$'}{DB_USERNAME:${artifactId}}
    password: ${'$'}{DB_PASSWORD:${artifactId}}
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
</#if>
  devtools:
    restart:
      enabled: true

logging:
  level:
    ${packageName}: DEBUG
    org.springframework.web: DEBUG
<#if mongodb?? && mongodb>
    org.springframework.data.mongodb: DEBUG
<#else>
    org.hibernate.SQL: DEBUG
</#if>

server:
  error:
    include-stacktrace: always
