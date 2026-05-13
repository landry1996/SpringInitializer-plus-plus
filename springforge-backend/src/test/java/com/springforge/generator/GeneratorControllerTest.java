package com.springforge.generator;

import com.springforge.generator.application.GenerateRequest;
import com.springforge.generator.domain.BuildTool;
import com.springforge.generator.domain.ProjectConfiguration;
import com.springforge.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void generate_withValidConfig_returns202() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "user@test.com", "USER");

        var config = new ProjectConfiguration(
                new ProjectConfiguration.Metadata("com.example", "demo-app", "Demo App", "A demo",
                        "com.example.demo", "21", "3.3.5", BuildTool.MAVEN),
                new ProjectConfiguration.Architecture("HEXAGONAL", List.of("user", "order"), false, false),
                List.of("spring-boot-starter-web"),
                null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/projects/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateRequest(config))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.generationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void generate_withoutAuth_returns401() throws Exception {
        var config = new ProjectConfiguration(
                new ProjectConfiguration.Metadata("com.example", "demo", "Demo", "", "com.example.demo", "21", "3.3.5", BuildTool.MAVEN),
                new ProjectConfiguration.Architecture("LAYERED", List.of(), false, false),
                List.of(), null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/projects/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateRequest(config))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStatus_withNonExistentId_returns404() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "user@test.com", "USER");

        mockMvc.perform(get("/api/v1/generations/" + UUID.randomUUID() + "/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
