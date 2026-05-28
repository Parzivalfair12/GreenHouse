package com.example.greenhouse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de OpenAPI/Swagger para la documentación interactiva
 * de la API REST de GreenHouse Manager.
 *
 * Configura:
 * <ul>
 *   <li>Información del API: título, versión, descripción, contacto, licencia.</li>
 *   <li>Servidores de desarrollo y producción.</li>
 *   <li>Esquema de seguridad JWT Bearer token.</li>
 *   <li>Header global Accept-Language para i18n.</li>
 *   <li>Requerimiento de seguridad global para endpoints protegidos.</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Configuration
public class OpenApiConfig {

  /**
   * Construye la configuración OpenAPI personalizada con metadatos del sistema,
   * esquemas de seguridad y headers globales.
   *
   * @return instancia de {@link OpenAPI} configurada
   */
  @Bean
  OpenAPI greenhouseOpenAPI() {
    var jwtScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description(
            "Token JWT obtenido de POST /api/auth/login. "
          + "Incluir en el header Authorization: Bearer <token>.");

    var securityRequirement = new SecurityRequirement().addList("bearer-jwt");

    var acceptLanguageHeader = new Header()
        .description("Idioma de respuesta (es = español, en = inglés)")
        .schema(new StringSchema()._default("es")._enum(List.of("es", "en")));

    var globalAcceptLanguageParam = new Parameter()
        .in("header")
        .name("Accept-Language")
        .description("Idioma de la respuesta. Los mensajes de error, validaciones "
            + "y descripciones se traducen al idioma solicitado.")
        .required(false)
        .schema(new StringSchema()._default("es")._enum(List.of("es", "en")));

    return new OpenAPI()
        .info(new Info()
            .title("GreenHouse Manager API")
            .version("2.1.0")
            .description(
                "API REST para la gestión inteligente de invernaderos agrícolas.\n\n"
              + "## Funcionalidades\n"
              + "- Autenticación JWT + OAuth2 Google\n"
              + "- CRUD de invernaderos, cultivos, sensores, lecturas\n"
              + "- Motor de reglas y alertas automatizadas\n"
              + "- Simulación IoT de sensores\n"
              + "- Predicciones IA con microservicio Flask\n"
              + "- Roles: ADMIN, OPERATOR, VIEWER\n"
              + "- Internacionalización español/inglés (Accept-Language)\n\n"
              + "## Autenticación\n"
              + "1. POST /api/auth/login con email/password → recibe JWT\n"
              + "2. Incluir el token en el header Authorization: Bearer <token>\n"
              + "3. El token expira según app.jwt-expiration-ms (default 24h)\n"
              + "4. Usar POST /api/auth/refresh para renovar antes de expirar")
            .contact(new Contact()
                .name("Equipo GreenHouse")
                .email("greenhouse@example.com")
                .url("https://github.com/greenhouse"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Servidor de desarrollo local"),
            new Server()
                .url("https://api.greenhouse.example.com")
                .description("Servidor de producción")))
        .components(new Components()
            .addSecuritySchemes("bearer-jwt", jwtScheme)
            .addParameters("Accept-Language", globalAcceptLanguageParam))
        .addSecurityItem(securityRequirement);
  }
}
