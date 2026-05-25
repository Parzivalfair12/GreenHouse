package com.example.greenhouse.service;

import com.example.greenhouse.web.dto.IaPredictionResponse;
import com.example.greenhouse.web.dto.IaRecommendationResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IaService {

  private final RestTemplate rest;
  private final String iaUrl;

  public IaService(@Value("${app.ia-url:http://localhost:5000}") String iaUrl) {
    this.iaUrl = iaUrl;
    this.rest = new RestTemplate();
  }

  public IaPredictionResponse predict(List<Double> temperatures, List<Double> humidities) {
    try {
      var body = Map.of("temperature", temperatures, "humidity", humidities);
      return rest.postForObject(iaUrl + "/ia/predict", body, IaPredictionResponse.class);
    } catch (Exception e) {
      return new IaPredictionResponse(null, null, "UNAVAILABLE",
          Map.of("temperature", false, "humidity", false));
    }
  }

  public IaRecommendationResponse recommend(Double tempPred, Double humPred, String riskLevel) {
    try {
      var body = Map.of("predictedTemperature", tempPred, "predictedHumidity", humPred, "riskLevel", riskLevel);
      return rest.postForObject(iaUrl + "/ia/recommend", body, IaRecommendationResponse.class);
    } catch (Exception e) {
      return new IaRecommendationResponse("IA_OFFLINE", "El servicio de IA no esta disponible");
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> health() {
    try {
      return rest.getForObject(iaUrl + "/ia/health", Map.class);
    } catch (Exception e) {
      return Map.of("status", "DOWN", "service", "greenhouse-ia", "error", e.getMessage());
    }
  }
}
