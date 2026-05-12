package com.springforge.blueprint.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springforge.blueprint.domain.ArchitectureType;
import com.springforge.blueprint.domain.Blueprint;
import com.springforge.blueprint.domain.BlueprintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class BlueprintDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BlueprintDataInitializer.class);

    private final BlueprintRepository blueprintRepository;
    private final ObjectMapper objectMapper;

    public BlueprintDataInitializer(BlueprintRepository blueprintRepository, ObjectMapper objectMapper) {
        this.blueprintRepository = blueprintRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:blueprints/*.json");

        for (Resource resource : resources) {
            JsonNode node = objectMapper.readTree(resource.getInputStream());
            String name = node.get("name").asText();

            if (blueprintRepository.findByName(name).isPresent()) {
                log.debug("Blueprint already exists: {}", name);
                continue;
            }

            String description = node.get("description").asText();
            ArchitectureType type = ArchitectureType.valueOf(node.get("type").asText());
            String constraintsJson = objectMapper.writeValueAsString(node.get("constraints"));
            String defaultsJson = objectMapper.writeValueAsString(node.get("defaults"));
            String structureJson = objectMapper.writeValueAsString(node.get("structure"));

            Blueprint blueprint = new Blueprint(name, description, type, constraintsJson, defaultsJson, structureJson, true);
            blueprintRepository.save(blueprint);
            log.info("Loaded built-in blueprint: {}", name);
        }
    }
}
