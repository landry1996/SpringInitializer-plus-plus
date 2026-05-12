services:
  service-registry:
    build: ./service-registry
    ports:
      - "8761:8761"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      retries: 5

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      service-registry:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/

<#list services as service>
  ${service.name}:
    build: ./${service.name}
    depends_on:
      service-registry:
        condition: service_healthy
      ${service.name}-db:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
      SPRING_DATASOURCE_URL: jdbc:postgresql://${service.name}-db:5432/${service.name}
      SPRING_DATASOURCE_USERNAME: ${service.name}
      SPRING_DATASOURCE_PASSWORD: ${service.name}

  ${service.name}-db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${service.name}
      POSTGRES_USER: ${service.name}
      POSTGRES_PASSWORD: ${service.name}
    volumes:
      - ${service.name}-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${service.name}"]
      interval: 5s
      retries: 5

</#list>
volumes:
<#list services as service>
  ${service.name}-data:
</#list>
