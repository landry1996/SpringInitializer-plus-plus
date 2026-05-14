server:
  port: ${r"${servicePort!0}"}

spring:
  application:
    name: ${r"${serviceName}"}
<#if serviceDatabases?? && (serviceDatabases?size > 0)>
<#list serviceDatabases as db>
<#if db.type == "POSTGRESQL" && db.purpose == "PRIMARY_STORE">
  datasource:
    url: jdbc:postgresql://localhost:5432/${r"${serviceName}"}
    username: ${r"${serviceName}"}
    password: ${r"${serviceName}"}
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
<#elseif db.type == "MYSQL" && db.purpose == "PRIMARY_STORE">
  datasource:
    url: jdbc:mysql://localhost:3306/${r"${serviceName}"}
    username: ${r"${serviceName}"}
    password: ${r"${serviceName}"}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
<#elseif db.type == "MONGODB" && db.purpose == "PRIMARY_STORE">
  data:
    mongodb:
      uri: mongodb://localhost:27017/${r"${serviceName}"}
      database: ${r"${serviceName}"}
<#elseif db.type == "REDIS" && db.purpose == "CACHE">
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
<#elseif db.type == "CASSANDRA" && db.purpose == "PRIMARY_STORE">
  cassandra:
    keyspace-name: ${r"${serviceName}"}
    contact-points: localhost
    port: 9042
    local-datacenter: datacenter1
<#elseif db.type == "NEO4J" && db.purpose == "PRIMARY_STORE">
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: ${r"${serviceName}"}
</#if>
</#list>
<#else>
  datasource:
    url: jdbc:postgresql://localhost:5432/${r"${serviceName}"}
    username: ${r"${serviceName}"}
    password: ${r"${serviceName}"}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
</#if>

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${r"${spring.application.name}:${random.uuid}"}

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: when-authorized
<#if msObservability??>
<#if msObservability.distributedTracing == "ZIPKIN">

  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
<#elseif msObservability.distributedTracing == "JAEGER">

  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
</#if>
</#if>
<#if resilience??>

resilience4j:
<#list resilience as r>
<#if r.source == serviceName>
<#if r.circuitBreaker?? && r.circuitBreaker.enabled>
  circuitbreaker:
    instances:
      ${r.target}:
        failure-rate-threshold: ${r.circuitBreaker.failureThreshold}
        wait-duration-in-open-state: ${r.circuitBreaker.waitDurationSeconds}s
        sliding-window-size: ${r.circuitBreaker.slidingWindowSize}
</#if>
<#if r.retry?? && r.retry.enabled>
  retry:
    instances:
      ${r.target}:
        max-attempts: ${r.retry.maxAttempts}
        wait-duration: ${r.retry.backoffDelayMs}ms
</#if>
<#if r.timeout?? && r.timeout.enabled>
  timelimiter:
    instances:
      ${r.target}:
        timeout-duration: ${r.timeout.durationMs}ms
</#if>
</#if>
</#list>
</#if>
