package com.example.greenhouse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI greenhouseOpenAPI() {
    var securityScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("Ingrese el token JWT obtenido de POST /api/auth/login");
    var securityRequirement = new SecurityRequirement().addList("bearer-jwt");

    return new OpenAPI()
        .info(new Info()
            .title("GreenHouse Manager API")
            .version("2.1.0")
            .description("API REST para la gestion inteligente de invernaderos. "
                + "Soporta autenticacion via JWT y OAuth2 Google, "
                + "gestion de cultivos, sensores, lecturas, riegos, alertas y automatizaciones.")
            .contact(new Contact()
                .name("Equipo GreenHouse")
                .email("greenhouse@example.com")))
        .components(new Components()
            .addSecuritySchemes("bearer-jwt", securityScheme))
        .addSecurityItem(securityRequirement);
  }
}
