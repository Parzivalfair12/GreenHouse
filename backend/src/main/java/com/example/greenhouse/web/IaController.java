package com.example.greenhouse.web;

import com.example.greenhouse.service.IaService;
import com.example.greenhouse.web.dto.IaPredictionResponse;
import com.example.greenhouse.web.dto.IaRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "IA", description = "Predicciones y recomendaciones del modulo de inteligencia artificial")
@RestController
@RequestMapping("/api/ia")
public class IaController {

  private final IaService iaService;

  public IaController(IaService iaService) {
    this.iaService = iaService;
  }

  @Operation(summary = "Health check del servicio IA", description = "Verifica que el modulo de inteligencia artificial este operativo")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Servicio IA operativo",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"status\":\"UP\",\"model\":\"v2.1.0\",\"lastTraining\":\"2026-03-14T10:00:00\"}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping("/health")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> health() {
    return iaService.health();
  }

  @Operation(summary = "Predecir temperatura y humedad", description = "Recibe lecturas historicas y predice los proximos valores")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Prediccion generada exitosamente",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"predictedTemperature\":28.5,\"predictedHumidity\":65.2,\"riskLevel\":\"LOW\",\"anomalies\":{\"temperature\":false,\"humidity\":false}}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos insuficientes)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/predict")
  @PreAuthorize("isAuthenticated()")
  public IaPredictionResponse predict(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lecturas historicas de temperatura y humedad", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"temperature\":[26.5,27.0,27.8,28.1,28.3],\"humidity\":[70.0,68.5,67.0,66.2,65.8]}")))
      @RequestBody Map<String, List<Double>> body) {
    return iaService.predict(
        body.getOrDefault("temperature", List.of()),
        body.getOrDefault("humidity", List.of()));
  }

  @Operation(summary = "Obtener recomendacion", description = "Sugiere acciones basadas en predicciones")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Recomendacion generada exitosamente",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"action\":\"ACTIVAR_RIEGO\",\"reason\":\"La humedad predicha (55%) esta por debajo del umbral minimo (60%)\"}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (parametros incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/recommend")
  @PreAuthorize("isAuthenticated()")
  public IaRecommendationResponse recommend(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de prediccion para generar recomendacion", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"predictedTemperature\":28.5,\"predictedHumidity\":55.0,\"riskLevel\":\"MEDIUM\"}")))
      @RequestBody Map<String, Object> body) {
    Double tempPred = body.get("predictedTemperature") instanceof Number n ? n.doubleValue() : null;
    Double humPred = body.get("predictedHumidity") instanceof Number n ? n.doubleValue() : null;
    String riskLevel = (String) body.getOrDefault("riskLevel", "LOW");
    return iaService.recommend(tempPred, humPred, riskLevel);
  }
}
