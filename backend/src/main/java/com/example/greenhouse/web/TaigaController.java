package com.example.greenhouse.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Taiga", description = "Historias de usuario, criterios de aceptacion y seguimiento del proyecto")
@RestController
@RequestMapping("/api/taiga")
public class TaigaController {

  private static final List<Map<String, Object>> STORIES = List.of(
      Map.of(
          "id", 1,
          "epic", "Autenticacion",
          "title", "Autenticacion con Google",
          "description", "Como usuario quiero iniciar sesion con mi correo de Google para acceder sin crear una clave nueva.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 1, "description", "El boton redirige a /oauth2/authorization/google", "status", "PASSED"),
              Map.of("id", 2, "description", "El backend usa OAuth2 Client de Spring Security", "status", "PASSED"),
              Map.of("id", 3, "description", "El endpoint /api/auth/me devuelve nombre, correo y rol", "status", "PASSED")
          )),
      Map.of(
          "id", 2,
          "epic", "Gestion",
          "title", "Consulta de invernaderos",
          "description", "Como operador quiero consultar los invernaderos activos para conocer su estado general.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 4, "description", "GET /api/greenhouses devuelve id, nombre, ubicacion, area, estado", "status", "PASSED"),
              Map.of("id", 5, "description", "El frontend muestra los invernaderos en tabla responsive", "status", "PASSED"),
              Map.of("id", 6, "description", "La pantalla funciona en espanol e ingles", "status", "PASSED")
          )),
      Map.of(
          "id", 3,
          "epic", "Alertas",
          "title", "Gestion de alertas",
          "description", "Como operador quiero revisar y resolver alertas para dejar trazabilidad.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 7, "description", "GET /api/alerts/open lista alertas no resueltas", "status", "PASSED"),
              Map.of("id", 8, "description", "PATCH /api/alerts/{id}/resolve cambia resolved a true", "status", "PASSED"),
              Map.of("id", 9, "description", "La respuesta respeta Accept-Language", "status", "PASSED")
          )),
      Map.of(
          "id", 4,
          "epic", "Modelo de datos",
          "title", "Validacion del modelo JSON",
          "description", "Como docente quiero validar que el modelo JSON tenga datos completos.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 10, "description", "docs/modelo-invernadero.json contiene invernaderos, alertas y usuarios", "status", "PASSED"),
              Map.of("id", 11, "description", "Pruebas Python validan secciones obligatorias", "status", "PASSED"),
              Map.of("id", 12, "description", "GitHub Actions ejecuta validacion automatica", "status", "PASSED")
          )),
      Map.of(
          "id", 5,
          "epic", "IA e IoT",
          "title", "Prediccion y simulacion de sensores",
          "description", "Como administrador quiero predecir condiciones climaticas y simular sensores IoT.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 13, "description", "Flask IA predice temperatura y humedad futura", "status", "PASSED"),
              Map.of("id", 14, "description", "Python IoT simulator genera lecturas periodicas", "status", "PASSED"),
              Map.of("id", 15, "description", "Las alertas se activan automaticamente ante anomalias", "status", "PASSED")
          )),
      Map.of(
          "id", 6,
          "epic", "Seguridad",
          "title", "Autenticacion JWT y autorizacion por roles",
          "description", "Como administrador quiero controlar el acceso por roles.",
          "status", "COMPLETED",
          "criteria", List.of(
              Map.of("id", 16, "description", "JWT con expiracion de 24h", "status", "PASSED"),
              Map.of("id", 17, "description", "Roles ADMIN, OPERATOR, VIEWER con permisos diferenciados", "status", "PASSED"),
              Map.of("id", 18, "description", "@PreAuthorize protege todos los endpoints", "status", "PASSED")
          ))
  );

  @Operation(summary = "Listar todas las historias de usuario", description = "Devuelve el backlog completo con criterios de aceptacion")
  @GetMapping("/stories")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> listStories() {
    return STORIES;
  }

  @Operation(summary = "Obtener una historia por ID", description = "Devuelve una historia de usuario con sus criterios de aceptacion")
  @GetMapping("/stories/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getStory(@PathVariable int id) {
    return STORIES.stream()
        .filter(s -> s.get("id").equals(id))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Obtener resumen del proyecto", description = "Estadisticas de completitud del proyecto")
  @GetMapping("/summary")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> summary() {
    long total = STORIES.size();
    long completed = STORIES.stream().filter(s -> "COMPLETED".equals(s.get("status"))).count();
    return Map.of(
        "totalStories", total,
        "completedStories", completed,
        "completionPercent", total > 0 ? (completed * 100 / total) : 0,
        "project", "GreenHouse Manager"
    );
  }
}
