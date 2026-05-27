package com.example.greenhouse.web;

import com.example.greenhouse.service.AiPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local AI prediction controller based on historical sensor readings.
 * No external APIs. All predictions are computed locally from PostgreSQL data.
 */
@Tag(name = "AI Predictivo", description = "Predicciones locales basadas en historico de lecturas")
@RestController
@RequestMapping("/api/ai")
public class AiPredictionController {

  private final AiPredictionService ai;

  public AiPredictionController(AiPredictionService ai) {
    this.ai = ai;
  }

  @Operation(summary = "Prediccion inteligente", description = "Analiza ultimas lecturas y predice temperatura, humedad y riesgo")
  @GetMapping("/prediction")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> prediction() {
    AiPredictionService.PredictionResult r = ai.predict();
    return Map.of(
        "predictedTemperature", r.predictedTemperature(),
        "predictedHumidity", r.predictedHumidity(),
        "riskLevel", r.riskLevel(),
        "recommendation", r.recommendation(),
        "trend", r.trend(),
        "temperatureSampleSize", r.temperatureSampleSize(),
        "humiditySampleSize", r.humiditySampleSize(),
        "temperatureTrend", r.temperatureTrend(),
        "humidityTrend", r.humidityTrend()
    );
  }
}
