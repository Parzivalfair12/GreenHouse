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

/**
 * Servicio proxy para el microservicio Flask de IA que proporciona
 * predicciones ML y recomendaciones agronómicas.
 *
 * Se conecta al servicio Python externo via HTTP con timeouts configurables
 * (5s conexión, 10s lectura). Todos los métodos degradan gracefulmente
 * cuando el servicio IA no está disponible, retornando estado
 * {@code "UNAVAILABLE"} o {@code "IA_OFFLINE"}.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
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

  /**
   * Requests ML-based prediction for temperature and humidity trends based on
   * historical sensor values.
   *
   * When the IA microservice is unreachable, returns a prediction response with
   * {@code riskLevel="UNAVAILABLE"} and trend flags set to {@code false}.
   *
   * @param temperatures historical temperature readings (float list)
   * @param humidities   historical humidity readings (float list)
   * @return prediction with forecasted values and risk level
   * @since 2.1.0
   */
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

  /**
   * Requests an agronomic recommendation from the IA service based on predicted
   * temperature and humidity values.
   *
   * On failure, returns a fallback response with action
   * {@code "IA_OFFLINE"}.
   *
   * @param tempPred  predicted temperature value
   * @param humPred   predicted humidity value
   * @param riskLevel risk classification from the prediction step
   * @return recommendation with suggested action
   * @since 2.1.0
   */
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

  /**
   * Performs a health check against the IA microservice endpoint.
   *
   * @return a map with {@code status} key ("UP" or "DOWN") and service metadata
   * @since 2.1.0
   */
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
