server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
<#list services as service>
        - id: ${r"${service.name}"}
          uri: lb://${r"${service.name}"}
          predicates:
            - Path=/api/${r"${service.name}"}/**
          filters:
            - StripPrefix=2
</#list>

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway

resilience4j:
  circuitbreaker:
    instances:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
