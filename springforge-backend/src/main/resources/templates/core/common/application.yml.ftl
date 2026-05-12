spring:
  application:
    name: ${artifactId}
<#if database??>
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
