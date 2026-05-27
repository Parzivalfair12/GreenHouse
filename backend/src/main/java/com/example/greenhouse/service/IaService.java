package com.example.greenhouse.service;

import com.example.greenhouse.web.dto.IaPredictionResponse;
import com.example.greenhouse.web.dto.IaRecommendationResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IaService {

  private static final Logger log = LoggerFactory.getLogger(IaService.class);

  private final RestTemplate rest;
  private final String iaUrl;

  public IaService(@Value("${app.ia-url:http://localhost:5000}") String iaUrl) {
    this.iaUrl = iaUrl;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
    factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
    this.rest = new RestTemplate(factory);
  }

  public IaPredictionResponse predict(List<Double> temperatures, List<Double> humidities) {
    long start = System.currentTimeMillis();
    try {
      var body = Map.of("temperature", temperatures, "humidity", humidities);
      IaPredictionResponse response = rest.postForObject(iaUrl + "/ia/predict", body, IaPredictionResponse.class);
      log.info("IA predict success in {}ms", System.currentTimeMillis() - start);
      return response;
    } catch (Exception e) {
      log.error("IA predict failed after {}ms: {}", System.currentTimeMillis() - start, e.getMessage());
      return new IaPredictionResponse(null, null, "UNAVAILABLE",
          Map.of("temperature", false, "humidity", false));
    }
  }

  public IaRecommendationResponse recommend(Double tempPred, Double humPred, String riskLevel) {
    long start = System.currentTimeMillis();
    try {
      var body = Map.of("predictedTemperature", tempPred, "predictedHumidity", humPred, "riskLevel", riskLevel);
      IaRecommendationResponse response = rest.postForObject(iaUrl + "/ia/recommend", body, IaRecommendationResponse.class);
      log.info("IA recommend success in {}ms", System.currentTimeMillis() - start);
      return response;
    } catch (Exception e) {
      log.error("IA recommend failed after {}ms: {}", System.currentTimeMillis() - start, e.getMessage());
      return new IaRecommendationResponse("IA_OFFLINE", "El servicio de IA no esta disponible");
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> health() {
    try {
      Map<String, Object> response = rest.getForObject(iaUrl + "/ia/health", Map.class);
      log.info("IA health check: {}", response);
      return response;
    } catch (Exception e) {
      log.error("IA health check failed: {}", e.getMessage());
      return Map.of("status", "DOWN", "service", "greenhouse-ia", "error", e.getMessage());
    }
  }
}
