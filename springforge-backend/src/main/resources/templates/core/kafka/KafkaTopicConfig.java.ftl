package ${packageName}.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
<#list topics as topic>

    @Bean
    public NewTopic ${topic.name?replace("-", "")?replace(".", "")}Topic() {
        return TopicBuilder.name("${topic.name}")
            .partitions(${topic.partitions!"3"})
            .replicas(${topic.replicas!"1"})
            .build();
    }
</#list>

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name("${artifactId}.dlq")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
