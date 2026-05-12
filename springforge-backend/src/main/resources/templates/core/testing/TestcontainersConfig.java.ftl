package ${packageName}.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
<#if kafka?? && kafka>
import org.testcontainers.kafka.KafkaContainer;
</#if>
<#if redis?? && redis>
import org.testcontainers.containers.GenericContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
</#if>

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("${artifactId}_test");
    }
<#if kafka?? && kafka>

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka:3.7.0");
    }
</#if>
<#if redis?? && redis>

    @Bean
    public GenericContainer<?> redisContainer(DynamicPropertyRegistry registry) {
        GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        return redis;
    }
</#if>
}
