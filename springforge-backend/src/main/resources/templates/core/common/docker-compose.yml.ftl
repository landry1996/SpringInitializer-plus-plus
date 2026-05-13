services:
<#if database??>
  ${database.serviceName}:
    image: ${database.image}
    container_name: ${artifactId}-db
    environment:
<#list database.envVars as key, value>
      ${key}: ${value}
</#list>
    ports:
      - "${database.port}:${database.port}"
    volumes:
      - db-data:/var/lib/${database.volumePath}
<#if database.healthCheck??>
    healthcheck:
      test: ${database.healthCheck}
      interval: 10s
      timeout: 5s
      retries: 5
</#if>
</#if>

volumes:
<#if database??>
  db-data:
</#if>
