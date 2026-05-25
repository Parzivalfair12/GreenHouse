package com.example.greenhouse.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IaControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void iaHealthReturnsServiceInfo() throws Exception {
    mvc.perform(get("/api/ia/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.service").value("greenhouse-ia"));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerCanAccessIaHealth() throws Exception {
    mvc.perform(get("/api/ia/health"))
        .andExpect(status().isOk());
  }

  @Test
  void unauthenticatedUserCannotAccessIa() throws Exception {
    mvc.perform(get("/api/ia/health"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void predictReturnsFallbackWhenFlaskOffline() throws Exception {
    mvc.perform(post("/api/ia/predict")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"temperature":[28,29,30],"humidity":[70,71,72]}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskLevel").value("UNAVAILABLE"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void recommendReturnsFallbackWhenFlaskOffline() throws Exception {
    mvc.perform(post("/api/ia/recommend")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"predictedTemperature":32,"predictedHumidity":60,"riskLevel":"MEDIUM"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.action").value("IA_OFFLINE"));
  }
}
