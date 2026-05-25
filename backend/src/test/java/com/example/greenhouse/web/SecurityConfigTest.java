package com.example.greenhouse.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {
  @Autowired
  MockMvc mvc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanAccessUsers() throws Exception {
    mvc.perform(get("/api/users"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerCannotAccessUsers() throws Exception {
    mvc.perform(get("/api/users"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreatesUser() throws Exception {
    mvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"test@test.com","fullName":"Test","password":"pass1234","role":"VIEWER"}
                """))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "OPERATOR")
  void operatorCanResolveAlerts() throws Exception {
    mvc.perform(post("/api/alerts/999/resolve")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanResolveAlerts() throws Exception {
    mvc.perform(post("/api/alerts/999/resolve")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerCannotResolveAlerts() throws Exception {
    mvc.perform(post("/api/alerts/999/resolve")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
