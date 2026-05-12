<#if cacheType == "REDIS">
spring:
  data:
    redis:
      host: ${'$'}{REDIS_HOST:localhost}
      port: ${'$'}{REDIS_PORT:6379}
      password: ${'$'}{REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
  cache:
    type: redis
<#else>
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=30m
</#if>
