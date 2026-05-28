package com.example.greenhouse.web;

import com.example.greenhouse.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Simulates IoT sensors generating periodic readings. */
@Tag(name = "Simulador", description = "Simulacion de sensores IoT para generar lecturas periodicas")
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {
  private static final Logger log = LoggerFactory.getLogger(SimulatorController.class);

  private final SimulationService simulation;
  private final MessageSource messages;

  public SimulatorController(SimulationService simulation, MessageSource messages) {
    this.simulation = simulation;
    this.messages = messages;
  }

  @Operation(summary = "Iniciar simulador", description = "Genera lecturas automaticas cada 5 segundos")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Simulador iniciado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"status\":\"STARTED\",\"intervalSeconds\":5}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN u OPERATOR)"),
      @ApiResponse(responseCode = "409", description = "El simulador ya esta en ejecucion"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/start")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public Map<String, Object> start(Locale locale) {
    if (simulation.isRunning()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          messages.getMessage("simulator.already.running", null, locale));
    }
    simulation.start();
    return Map.of("status", "STARTED", "intervalSeconds", 5);
  }

  @Operation(summary = "Detener simulador", description = "Detiene la generacion automatica de lecturas")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Simulador detenido",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"status\":\"STOPPED\"}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN u OPERATOR)"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/stop")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public Map<String, Object> stop() {
    simulation.stop();
    return Map.of("status", "STOPPED");
  }

  @Operation(summary = "Estado del simulador", description = "Devuelve si el simulador esta activo")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Estado del simulador",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"running\":true,\"intervalSeconds\":5}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping("/status")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> status() {
    return Map.of("running", simulation.isRunning(), "intervalSeconds", 5);
  }
}
