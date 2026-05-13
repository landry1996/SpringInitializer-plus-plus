spring:
  application:
    name: ${artifactId}
<#if mongodb?? && mongodb>
  data:
    mongodb:
      uri: mongodb://${artifactId}:${artifactId}@localhost:27017/${artifactId}?authSource=admin
      auto-index-creation: true
<#elseif database??>
  datasource:
    url: jdbc:${database.type}://localhost:${database.port}/${artifactId}
    username: ${artifactId}
    password: ${artifactId}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
</#if>

server:
  port: 8080
  error:
    include-stacktrace: never

logging:
  level:
    ${packageName}: INFO
