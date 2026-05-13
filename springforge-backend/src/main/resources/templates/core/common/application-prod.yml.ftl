spring:
<#if mongodb?? && mongodb>
  data:
    mongodb:
      uri: ${'$'}{MONGODB_URI}
      auto-index-creation: false
<#else>
  datasource:
    url: ${'$'}{DATABASE_URL}
    username: ${'$'}{DATABASE_USERNAME}
    password: ${'$'}{DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 600000
  jpa:
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        generate_statistics: false
  flyway:
    enabled: true
</#if>
<#if cacheType?? && cacheType == "REDIS">
  data:
    redis:
      host: ${'$'}{REDIS_HOST}
      port: ${'$'}{REDIS_PORT:6379}
      password: ${'$'}{REDIS_PASSWORD}
      ssl:
        enabled: true
</#if>

server:
  error:
    include-stacktrace: never
    include-message: never

logging:
  level:
    root: WARN
    ${packageName}: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
