spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${artifactId}
    username: ${'$'}{DB_USERNAME:${artifactId}}
    password: ${'$'}{DB_PASSWORD:${artifactId}}
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  devtools:
    restart:
      enabled: true

logging:
  level:
    ${packageName}: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG

server:
  error:
    include-stacktrace: always
