package com.example.greenhouse.web;

import com.example.greenhouse.service.IaService;
import com.example.greenhouse.web.dto.IaPredictionResponse;
import com.example.greenhouse.web.dto.IaRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
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

  @Operation(summary = "Health check del servicio IA")
  @GetMapping("/health")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> health() {
    return iaService.health();
  }

  @Operation(summary = "Predecir temperatura y humedad", description = "Recibe lecturas historicas y predice los proximos valores")
  @PostMapping("/predict")
  @PreAuthorize("isAuthenticated()")
  public IaPredictionResponse predict(@RequestBody Map<String, List<Double>> body) {
    return iaService.predict(
        body.getOrDefault("temperature", List.of()),
        body.getOrDefault("humidity", List.of()));
  }

  @Operation(summary = "Obtener recomendacion", description = "Sugiere acciones basadas en predicciones")
  @PostMapping("/recommend")
  @PreAuthorize("isAuthenticated()")
  public IaRecommendationResponse recommend(@RequestBody Map<String, Object> body) {
    Double tempPred = body.get("predictedTemperature") instanceof Number n ? n.doubleValue() : null;
    Double humPred = body.get("predictedHumidity") instanceof Number n ? n.doubleValue() : null;
    String riskLevel = (String) body.getOrDefault("riskLevel", "LOW");
    return iaService.recommend(tempPred, humPred, riskLevel);
  }
}
