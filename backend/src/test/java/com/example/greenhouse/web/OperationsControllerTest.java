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
class OperationsControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void listsSensors() throws Exception {
    mvc.perform(get("/api/sensors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsReadingAndReturnsIt() throws Exception {
    mvc.perform(get("/api/sensors"))
        .andExpect(status().isOk());

    // First get existing sensors to use a real sensorId
    String sensorsJson = mvc.perform(get("/api/sensors"))
        .andReturn().getResponse().getContentAsString();

    // If no sensors exist, create one via greenhouse endpoint
    if (sensorsJson.equals("[]")) {
      mvc.perform(post("/api/greenhouses")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {"name":"Ops Test","location":"X","areaSquareMeters":50,"active":true}
                  """))
          .andExpect(status().isCreated());
    }

    // Read sensors again after potential creation
    String updatedSensors = mvc.perform(get("/api/sensors"))
        .andReturn().getResponse().getContentAsString();

    // Try to create a reading with sensorId=1 (seeded or just created)
    mvc.perform(post("/api/readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"sensorId":1,"value":25.5,"recordedAt":"2024-01-01T10:00:00"}
                """))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/readings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listsActuators() throws Exception {
    mvc.perform(get("/api/actuators"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void listsAuditLogs() throws Exception {
    mvc.perform(get("/api/audit-logs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createsZone() throws Exception {
    mvc.perform(post("/api/zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Zona A","description":"Test zone","greenhouseId":1}
                """))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/zones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerCannotCreateReading() throws Exception {
    mvc.perform(post("/api/readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"sensorId":1,"value":25.5}
                """))
        .andExpect(status().isForbidden());
  }
}
