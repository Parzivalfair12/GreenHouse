package com.example.greenhouse.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class I18nIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void forgotPasswordReturnsSpanishMessageWithAcceptLanguageEs() throws Exception {
    mockMvc.perform(post("/api/auth/forgot-password")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "es")
            .content("{\"email\":\"unknown@test.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Si el correo existe, se ha enviado un enlace de recuperacion."));
  }

  @Test
  void forgotPasswordReturnsEnglishMessageWithAcceptLanguageEn() throws Exception {
    mockMvc.perform(post("/api/auth/forgot-password")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .content("{\"email\":\"unknown@test.com\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("If the email exists, a recovery link has been sent."));
  }

  @Test
  void loginValidationReturnsTranslatedMessages() throws Exception {
    // Spanish validation
    MvcResult esResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "es")
            .content("{\"email\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();

    String esBody = esResult.getResponse().getContentAsString();
    assertThat(esBody.contains("El correo es obligatorio") || esBody.contains("La contrasena es obligatoria")).isTrue();

    // English validation
    MvcResult enResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .content("{\"email\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();

    String enBody = enResult.getResponse().getContentAsString();
    assertThat(enBody.contains("Email is required") || enBody.contains("Password is required")).isTrue();
  }

  @Test
  void verifyEmailReturnsTranslatedMessage() throws Exception {
    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "es")
            .content("{\"token\":\"invalid-token\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Token invalido o expirado."));

    mockMvc.perform(post("/api/auth/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "en")
            .content("{\"token\":\"invalid-token\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid or expired token."));
  }
}
