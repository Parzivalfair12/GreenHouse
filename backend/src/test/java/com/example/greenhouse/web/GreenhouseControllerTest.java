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
class GreenhouseControllerTest {
  @Autowired
  MockMvc mvc;

  @Test
  void listsSeededGreenhouses() throws Exception {
    mvc.perform(get("/api/greenhouses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Invernadero Norte"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreatesGreenhouse() throws Exception {
    mvc.perform(post("/api/greenhouses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Casa sur","location":"Bloque B","areaSquareMeters":42.5,"active":true}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Casa sur"));
  }
}
