package com.springforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        KafkaAutoConfiguration.class
})
@EnableAsync
@EnableScheduling
@Modulithic(
        systemName = "SpringForge",
        sharedModules = "shared"
)
public class SpringForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringForgeApplication.class, args);
    }
}
