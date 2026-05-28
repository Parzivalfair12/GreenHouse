package com.example.greenhouse.web;

import com.example.greenhouse.service.AlertService;
import com.example.greenhouse.web.dto.AlertResponse;
import com.example.greenhouse.web.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consulta y resolución de alertas operativas.
 *
 * Las alertas son generadas por el {@link RuleEngineService} cuando las
 * lecturas de sensores superan los umbrales configurados o cuando las
 * reglas de automatización detectan condiciones críticas.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Tag(name = "Alertas", description = "Consulta y resolucion de alertas operativas")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
  private final AlertService service;
  private final MessageSource messages;

  public AlertController(AlertService service, MessageSource messages) {
    this.service = service;
    this.messages = messages;
  }

  @Operation(summary = "Listar alertas abiertas", description = "Devuelve todas las alertas no resueltas del sistema")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de alertas abiertas",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "[{\"id\":1,\"severity\":\"CRITICAL\",\"message\":\"Temperatura excedida en sensor TEMP-001\",\"resolved\":false,\"createdAt\":\"2026-03-15T14:30:00\",\"sensorCode\":\"TEMP-001\"},{\"id\":2,\"severity\":\"WARNING\",\"message\":\"Humedad baja en sensor HUM-002\",\"resolved\":false,\"createdAt\":\"2026-03-15T14:25:00\",\"sensorCode\":\"HUM-002\"}]"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping("/open")
  @PreAuthorize("isAuthenticated()")
  public List<AlertResponse> findOpenAlerts() {
    return service.findOpenAlerts().stream().map(AlertResponse::from).toList();
  }

  @Operation(summary = "Resolver alerta", description = "Marca una alerta como resuelta (requiere ADMIN u OPERATOR)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Alerta resuelta exitosamente"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN u OPERATOR)"),
      @ApiResponse(responseCode = "404", description = "Alerta no encontrada"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PatchMapping("/{id}/resolve")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public MessageResponse resolve(
      @Parameter(description = "ID de la alerta", required = true, example = "1") @PathVariable long id, Locale locale) {
    service.resolve(id);
    return new MessageResponse(messages.getMessage("alert.resolved", null, locale));
  }
}
