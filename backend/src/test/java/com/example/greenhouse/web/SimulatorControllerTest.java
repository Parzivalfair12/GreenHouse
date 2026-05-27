package com.example.greenhouse.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.greenhouse.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimulatorControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  SimulationService simulation;

  @BeforeEach
  void reset() {
    simulation.stop();
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void startReturnsStartedStatus() throws Exception {
    mvc.perform(post("/api/simulator/start"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STARTED"))
        .andExpect(jsonPath("$.intervalSeconds").value(5));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void startWhenAlreadyRunningReturnsConflict() throws Exception {
    mvc.perform(post("/api/simulator/start"))
        .andExpect(status().isOk());

    mvc.perform(post("/api/simulator/start"))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "OPERATOR")
  void operatorCanStartSimulator() throws Exception {
    mvc.perform(post("/api/simulator/start"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STARTED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void stopReturnsStoppedStatus() throws Exception {
    mvc.perform(post("/api/simulator/start")).andExpect(status().isOk());

    mvc.perform(post("/api/simulator/stop"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STOPPED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void statusReflectsRunningState() throws Exception {
    // Ensure stopped first
    mvc.perform(post("/api/simulator/stop")).andExpect(status().isOk());

    mvc.perform(get("/api/simulator/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.running").value(false));

    mvc.perform(post("/api/simulator/start")).andExpect(status().isOk());

    mvc.perform(get("/api/simulator/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.running").value(true));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerCannotStartSimulator() throws Exception {
    mvc.perform(post("/api/simulator/start"))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedCannotAccessSimulator() throws Exception {
    mvc.perform(get("/api/simulator/status"))
        .andExpect(status().is3xxRedirection());
  }
}
