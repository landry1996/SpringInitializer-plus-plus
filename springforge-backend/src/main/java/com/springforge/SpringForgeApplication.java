package com.springforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Modulithic(
        systemName = "SpringForge",
        sharedModules = "shared"
)
public class SpringForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringForgeApplication.class, args);
    }
}
