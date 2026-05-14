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

<#if configServer?? && configServer>
  config-server:
    build: ./config-server
    ports:
      - "8888:8888"
    depends_on:
      service-registry:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/

</#if>
<#list services as service>
  ${service.name}:
    build: ./${service.name}
    ports:
      - "${service.port!((8080 + service?index + 1)?string)}:${service.port!((8080 + service?index + 1)?string)}"
    depends_on:
      service-registry:
        condition: service_healthy
<#if service.databases?? && (service.databases?size > 0)>
<#list service.databases as db>
<#if db.purpose == "PRIMARY_STORE">
      ${service.name}-<#if db.type == "POSTGRESQL">postgres<#elseif db.type == "MYSQL">mysql<#elseif db.type == "MONGODB">mongo<#elseif db.type == "CASSANDRA">cassandra<#elseif db.type == "NEO4J">neo4j<#else>${db.type?lower_case}</#if>:
        condition: service_healthy
</#if>
</#list>
</#if>
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
<#if service.databases?? && (service.databases?size > 0)>
<#list service.databases as db>
<#if db.type == "POSTGRESQL" && db.purpose == "PRIMARY_STORE">
      SPRING_DATASOURCE_URL: jdbc:postgresql://${service.name}-postgres:5432/${service.name}
      SPRING_DATASOURCE_USERNAME: ${service.name}
      SPRING_DATASOURCE_PASSWORD: ${service.name}
<#elseif db.type == "MYSQL" && db.purpose == "PRIMARY_STORE">
      SPRING_DATASOURCE_URL: jdbc:mysql://${service.name}-mysql:3306/${service.name}
      SPRING_DATASOURCE_USERNAME: ${service.name}
      SPRING_DATASOURCE_PASSWORD: ${service.name}
<#elseif db.type == "MONGODB" && db.purpose == "PRIMARY_STORE">
      SPRING_DATA_MONGODB_URI: mongodb://${service.name}:${service.name}@${service.name}-mongo:27017/${service.name}
<#elseif db.type == "REDIS">
      SPRING_DATA_REDIS_HOST: ${service.name}-redis
      SPRING_DATA_REDIS_PORT: 6379
</#if>
</#list>
<#else>
      SPRING_DATASOURCE_URL: jdbc:postgresql://${service.name}-postgres:5432/${service.name}
      SPRING_DATASOURCE_USERNAME: ${service.name}
      SPRING_DATASOURCE_PASSWORD: ${service.name}
</#if>

<#if service.databases?? && (service.databases?size > 0)>
<#list service.databases as db>
<#if db.type == "POSTGRESQL">
  ${service.name}-postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${service.name}
      POSTGRES_USER: ${service.name}
      POSTGRES_PASSWORD: ${service.name}
    volumes:
      - ${service.name}-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${service.name}"]
      interval: 5s
      retries: 5

<#elseif db.type == "MYSQL">
  ${service.name}-mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ${service.name}
      MYSQL_USER: ${service.name}
      MYSQL_PASSWORD: ${service.name}
    volumes:
      - ${service.name}-mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 5

<#elseif db.type == "MONGODB">
  ${service.name}-mongo:
    image: mongo:7
    environment:
      MONGO_INITDB_ROOT_USERNAME: ${service.name}
      MONGO_INITDB_ROOT_PASSWORD: ${service.name}
      MONGO_INITDB_DATABASE: ${service.name}
    volumes:
      - ${service.name}-mongo-data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 5s
      retries: 5

<#elseif db.type == "REDIS">
  ${service.name}-redis:
    image: redis:7-alpine
    volumes:
      - ${service.name}-redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      retries: 5

<#elseif db.type == "CASSANDRA">
  ${service.name}-cassandra:
    image: cassandra:4
    volumes:
      - ${service.name}-cassandra-data:/var/lib/cassandra
    healthcheck:
      test: ["CMD-SHELL", "cqlsh -e 'describe cluster'"]
      interval: 15s
      retries: 10

<#elseif db.type == "NEO4J">
  ${service.name}-neo4j:
    image: neo4j:5
    environment:
      NEO4J_AUTH: neo4j/${service.name}
    volumes:
      - ${service.name}-neo4j-data:/data
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:7474 || exit 1"]
      interval: 10s
      retries: 5

</#if>
</#list>
<#else>
  ${service.name}-postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${service.name}
      POSTGRES_USER: ${service.name}
      POSTGRES_PASSWORD: ${service.name}
    volumes:
      - ${service.name}-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${service.name}"]
      interval: 5s
      retries: 5

</#if>
</#list>
<#if asyncCommunications?? && (asyncCommunications?size > 0)>
<#assign hasBroker = false>
<#list asyncCommunications as comm>
<#if !hasBroker>
<#assign hasBroker = true>
<#if comm.broker == "KAFKA">
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:29093
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      CLUSTER_ID: microservices-kafka-cluster
    ports:
      - "9092:9092"
    volumes:
      - kafka-data:/var/lib/kafka/data
    healthcheck:
      test: ["CMD-SHELL", "kafka-topics --bootstrap-server localhost:9092 --list"]
      interval: 10s
      retries: 5

<#elseif comm.broker == "RABBITMQ">
  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 10s
      retries: 5

</#if>
</#if>
</#list>
</#if>
<#if msObservability??>
<#if msObservability.distributedTracing == "ZIPKIN">
  zipkin:
    image: openzipkin/zipkin:latest
    ports:
      - "9411:9411"

<#elseif msObservability.distributedTracing == "JAEGER">
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"
      - "4318:4318"

</#if>
<#if msObservability.metricsExporter == "PROMETHEUS">
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    depends_on:
      - prometheus

</#if>
<#if msObservability.centralizedLogging == "ELK">
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch

<#elseif msObservability.centralizedLogging == "LOKI">
  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"

</#if>
</#if>
volumes:
<#list services as service>
<#if service.databases?? && (service.databases?size > 0)>
<#list service.databases as db>
<#if db.type == "POSTGRESQL">
  ${service.name}-postgres-data:
<#elseif db.type == "MYSQL">
  ${service.name}-mysql-data:
<#elseif db.type == "MONGODB">
  ${service.name}-mongo-data:
<#elseif db.type == "REDIS">
  ${service.name}-redis-data:
<#elseif db.type == "CASSANDRA">
  ${service.name}-cassandra-data:
<#elseif db.type == "NEO4J">
  ${service.name}-neo4j-data:
</#if>
</#list>
<#else>
  ${service.name}-postgres-data:
</#if>
</#list>
<#if asyncCommunications?? && (asyncCommunications?size > 0)>
<#list asyncCommunications as comm>
<#if comm?is_first>
<#if comm.broker == "KAFKA">
  kafka-data:
<#elseif comm.broker == "RABBITMQ">
  rabbitmq-data:
</#if>
</#if>
</#list>
</#if>
<#if msObservability?? && msObservability.centralizedLogging == "ELK">
  elasticsearch-data:
</#if>
