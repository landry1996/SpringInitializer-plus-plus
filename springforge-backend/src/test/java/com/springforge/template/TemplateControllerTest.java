package com.springforge.template;

import com.springforge.shared.security.JwtService;
import com.springforge.template.application.CreateTemplateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Test
    void listTemplates_returnsOk() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "admin@test.com", "ADMIN");

        mockMvc.perform(get("/api/v1/templates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void createTemplate_withoutAdminRole_returns403() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "user@test.com", "USER");
        var request = new CreateTemplateRequest("Test", "test/path", "common", "content", null);

        mockMvc.perform(post("/api/v1/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTemplate_withAdminRole_returns201() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "admin@test.com", "ADMIN");
        var request = new CreateTemplateRequest("Test Template", "test/unique/path", "common", "<content/>", null);

        mockMvc.perform(post("/api/v1/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
