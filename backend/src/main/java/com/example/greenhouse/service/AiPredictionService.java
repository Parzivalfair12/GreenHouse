package com.example.greenhouse.service;

import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de predicción IA local basado en lecturas históricas de sensores.
 * Utiliza medias móviles ponderadas, proyección de tendencia lineal y análisis
 * de varianza sobre datos reales de PostgreSQL.
 *
 * Reglas de negocio:
 * <ul>
 *   <li>No requiere APIs externas — todos los cálculos son locales.</li>
 *   <li>Predice el próximo valor usando media móvil ponderada + pendiente lineal
 *       sobre las últimas 20 lecturas.</li>
 *   <li>Evalúa riesgo combinando temperatura, humedad, tendencias y umbrales
 *       configurados en los sensores.</li>
 *   <li>Genera recomendaciones accionables basadas en reglas agronómicas.</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class AiPredictionService {

  private static final Logger log = LoggerFactory.getLogger(AiPredictionService.class);

  private final ReadingRepository readings;
  private final SensorRepository sensors;

  public AiPredictionService(ReadingRepository readings, SensorRepository sensors) {
    this.readings = readings;
    this.sensors = sensors;
  }

  /**
   * Ejecuta el pipeline completo de predicción: carga lecturas históricas,
   * calcula valores futuros, evalúa riesgo y genera recomendaciones.
   *
   * Flujo:
   * <ol>
   *   <li>Busca sensores de temperatura y humedad.</li>
   *   <li>Carga las últimas 20 lecturas de cada sensor.</li>
   *   <li>Calcula predicción mediante media móvil ponderada + pendiente lineal.</li>
   *   <li>Evalúa nivel de riesgo (HIGH, MEDIUM, LOW) según umbrales.</li>
   *   <li>Genera recomendación textual según condiciones.</li>
   * </ol>
   *
   * @return resultado de predicción con valores pronosticados, riesgo y recomendación
   * @since 2.1.0
   */
  public PredictionResult predict() {
    List<Sensor> allSensors = sensors.findAll();

    Sensor tempSensor = findSensorByType(allSensors, SensorType.TEMPERATURE);
    Sensor humSensor = findSensorByType(allSensors, SensorType.HUMIDITY);

    List<Reading> tempHistory = tempSensor != null
        ? readings.findBySensorIdOrderByRecordedAtDesc(tempSensor.id).stream().limit(20).toList()
        : List.of();

    List<Reading> humHistory = humSensor != null
        ? readings.findBySensorIdOrderByRecordedAtDesc(humSensor.id).stream().limit(20).toList()
        : List.of();

    // Reverse to chronological order
    List<Reading> tempChrono = new ArrayList<>(tempHistory);
    List<Reading> humChrono = new ArrayList<>(humHistory);

    double tempPred = predictNextValue(tempChrono);
    double humPred = predictNextValue(humChrono);

    double tempTrend = calculateTrend(tempChrono);
    double humTrend = calculateTrend(humChrono);

    String riskLevel = assessRisk(tempPred, humPred, tempTrend, humTrend, tempSensor, humSensor);
    String recommendation = generateRecommendation(tempPred, humPred, tempTrend, humTrend, riskLevel);
    String trendLabel = formatTrend(tempTrend, humTrend);

    log.info("AI prediction: temp={} (trend={}), hum={} (trend={}), risk={}",
        String.format("%.1f", tempPred), String.format("%.2f", tempTrend),
        String.format("%.1f", humPred), String.format("%.2f", humTrend), riskLevel);

    return new PredictionResult(
        round(tempPred), round(humPred), riskLevel, recommendation, trendLabel,
        tempHistory.size(), humHistory.size(), tempTrend, humTrend);
  }

  private Sensor findSensorByType(List<Sensor> sensors, SensorType type) {
    return sensors.stream().filter(s -> s.type == type).findFirst().orElse(null);
  }

  /**
   * Predict next value using weighted moving average + linear trend projection.
   */
  private double predictNextValue(List<Reading> history) {
    if (history.isEmpty()) {
      return 25.0;
    }
    if (history.size() == 1) {
      return history.get(0).value.doubleValue();
    }

    // Weighted moving average (recent readings have higher weight)
    double weightedSum = 0;
    double weightSum = 0;
    for (int i = 0; i < history.size(); i++) {
      double weight = i + 1;
      weightedSum += history.get(i).value.doubleValue() * weight;
      weightSum += weight;
    }
    double wma = weightedSum / weightSum;

    // Linear trend slope (last 5 points)
    int n = Math.min(5, history.size());
    if (n < 2) {
      return wma;
    }
    double slope = calculateSlope(history.subList(history.size() - n, history.size()));

    // Project one step forward
    return wma + slope;
  }

  private double calculateSlope(List<Reading> points) {
    int n = points.size();
    double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
    for (int i = 0; i < n; i++) {
      double x = i;
      double y = points.get(i).value.doubleValue();
      sumX += x;
      sumY += y;
      sumXY += x * y;
      sumX2 += x * x;
    }
    double denominator = n * sumX2 - sumX * sumX;
    if (denominator == 0) return 0;
    return (n * sumXY - sumX * sumY) / denominator;
  }

  private double calculateTrend(List<Reading> history) {
    if (history.size() < 2) return 0;
    double first = history.get(0).value.doubleValue();
    double last = history.get(history.size() - 1).value.doubleValue();
    return last - first;
  }

  private String assessRisk(double temp, double hum, double tempTrend, double humTrend,
      Sensor tempSensor, Sensor humSensor) {
    int riskScore = 0;

    // Temperature risk
    if (temp > 35) riskScore += 3;
    else if (temp > 30) riskScore += 2;
    else if (temp < 15) riskScore += 3;
    else if (temp < 18) riskScore += 1;

    // Humidity risk
    if (hum < 30) riskScore += 3;
    else if (hum < 40) riskScore += 2;
    else if (hum > 85) riskScore += 2;

    // Trend risk
    if (tempTrend > 2.0) riskScore += 1;
    if (humTrend < -5.0) riskScore += 2;

    // Threshold-based risk from sensor config
    if (tempSensor != null && tempSensor.maxThreshold != null && temp > tempSensor.maxThreshold.doubleValue()) {
      riskScore += 2;
    }
    if (humSensor != null && humSensor.minThreshold != null && hum < humSensor.minThreshold.doubleValue()) {
      riskScore += 2;
    }

    if (riskScore >= 5) return "HIGH";
    if (riskScore >= 2) return "MEDIUM";
    return "LOW";
  }

  private String generateRecommendation(double temp, double hum, double tempTrend, double humTrend, String risk) {
    List<String> recs = new ArrayList<>();

    if (risk.equals("HIGH")) {
      recs.add("Condiciones criticas detectadas. Verificar invernadero inmediatamente.");
    }

    if (temp > 32) {
      recs.add("Activar ventilacion para reducir temperatura.");
    } else if (temp < 16) {
      recs.add("Activar calefaccion para aumentar temperatura.");
    } else if (tempTrend > 1.5) {
      recs.add("Temperatura subiendo rapidamente. Monitorear.");
    } else if (tempTrend < -1.5) {
      recs.add("Temperatura descendiendo. Preparar calefaccion.");
    } else {
      recs.add("Temperatura estable.");
    }

    if (hum < 35) {
      recs.add("Activar riego preventivo. Humedad muy baja.");
    } else if (hum < 45) {
      recs.add("Humedad baja. Considerar riego.");
    } else if (humTrend < -3.0) {
      recs.add("Humedad descendiendo rapidamente. Activar riego.");
    } else if (hum > 80) {
      recs.add("Humedad alta. Verificar ventilacion.");
    } else {
      recs.add("Humedad en rango optimo.");
    }

    return String.join(" ", recs);
  }

  private String formatTrend(double tempTrend, double humTrend) {
    String tempArrow = tempTrend > 0.5 ? "↑" : tempTrend < -0.5 ? "↓" : "→";
    String humArrow = humTrend > 1.0 ? "↑" : humTrend < -1.0 ? "↓" : "→";
    return "Temp " + tempArrow + " / Hum " + humArrow;
  }

  private static double round(double value) {
    return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }

  public record PredictionResult(
      double predictedTemperature,
      double predictedHumidity,
      String riskLevel,
      String recommendation,
      String trend,
      int temperatureSampleSize,
      int humiditySampleSize,
      double temperatureTrend,
      double humidityTrend
  ) {}
}
