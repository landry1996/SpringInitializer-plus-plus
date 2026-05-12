package com.springforge.blueprint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlueprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listBlueprints_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk());
    }

    @Test
    void listBlueprints_filterByType_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints").param("type", "HEXAGONAL"))
                .andExpect(status().isOk());
    }
}
