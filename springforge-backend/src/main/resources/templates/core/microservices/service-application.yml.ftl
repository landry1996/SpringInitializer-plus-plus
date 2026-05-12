server:
  port: 0

spring:
  application:
    name: ${serviceName}
  datasource:
    url: jdbc:postgresql://localhost:5432/${serviceName}
    username: ${serviceName}
    password: ${serviceName}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true

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
        include: health,info
