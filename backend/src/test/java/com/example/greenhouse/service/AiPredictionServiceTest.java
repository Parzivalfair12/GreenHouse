package com.example.greenhouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(AiPredictionService.class)
@ActiveProfiles("test")
class AiPredictionServiceTest {

  @Autowired
  GreenhouseRepository greenhouses;
  @Autowired
  SensorRepository sensors;
  @Autowired
  ReadingRepository readings;
  @Autowired
  AiPredictionService aiService;

  private Sensor tempSensor;
  private Sensor humSensor;

  @BeforeEach
  void setUp() {
    Greenhouse gh = new Greenhouse();
    gh.name = "AI Test GH";
    gh.location = "C";
    gh.areaSquareMeters = BigDecimal.valueOf(60);
    gh.active = true;
    greenhouses.save(gh);

    tempSensor = new Sensor();
    tempSensor.code = "TEMP-AI";
    tempSensor.type = SensorType.TEMPERATURE;
    tempSensor.unit = "C";
    tempSensor.minThreshold = BigDecimal.valueOf(18);
    tempSensor.maxThreshold = BigDecimal.valueOf(32);
    tempSensor.greenhouse = gh;
    sensors.save(tempSensor);

    humSensor = new Sensor();
    humSensor.code = "HUM-AI";
    humSensor.type = SensorType.HUMIDITY;
    humSensor.unit = "%";
    humSensor.minThreshold = BigDecimal.valueOf(40);
    humSensor.maxThreshold = BigDecimal.valueOf(80);
    humSensor.greenhouse = gh;
    sensors.save(humSensor);
  }

  @Test
  void predictWithNoDataReturnsDefaults() {
    AiPredictionService.PredictionResult result = aiService.predict();

    assertThat(result).isNotNull();
    assertThat(result.predictedTemperature()).isEqualTo(25.0);
    assertThat(result.predictedHumidity()).isEqualTo(25.0);
    assertThat(result.temperatureSampleSize()).isEqualTo(0);
    assertThat(result.humiditySampleSize()).isEqualTo(0);
  }

  @Test
  void predictWithDataReturnsCalculatedValues() {
    // Insert rising temperature readings (chronological order)
    for (int i = 0; i < 5; i++) {
      Reading r = new Reading();
      r.sensor = tempSensor;
      r.value = BigDecimal.valueOf(20 + i * 2); // 20, 22, 24, 26, 28
      r.recordedAt = LocalDateTime.now().minusMinutes(5 - i);
      readings.save(r);
    }

    AiPredictionService.PredictionResult result = aiService.predict();

    assertThat(result).isNotNull();
    assertThat(result.temperatureSampleSize()).isEqualTo(5);
    assertThat(result.predictedTemperature()).isBetween(15.0, 35.0);
    assertThat(result.temperatureTrend()).isNotEqualTo(0.0);
  }

  @Test
  void predictHighTemperatureGivesMediumOrHighRisk() {
    // Insert high temperature readings
    for (int i = 0; i < 5; i++) {
      Reading r = new Reading();
      r.sensor = tempSensor;
      r.value = BigDecimal.valueOf(33 + i); // above threshold
      r.recordedAt = LocalDateTime.now().minusMinutes(5 - i);
      readings.save(r);
    }

    AiPredictionService.PredictionResult result = aiService.predict();

    assertThat(result.riskLevel()).isIn("MEDIUM", "HIGH");
    assertThat(result.recommendation()).containsIgnoringCase("ventilacion");
  }

  @Test
  void predictLowHumidityGivesRiskAndRecommendation() {
    for (int i = 0; i < 5; i++) {
      Reading r = new Reading();
      r.sensor = humSensor;
      r.value = BigDecimal.valueOf(30 - i); // below threshold
      r.recordedAt = LocalDateTime.now().minusMinutes(5 - i);
      readings.save(r);
    }

    AiPredictionService.PredictionResult result = aiService.predict();

    assertThat(result.humiditySampleSize()).isEqualTo(5);
    assertThat(result.predictedHumidity()).isLessThan(40.0);
    assertThat(result.riskLevel()).isIn("MEDIUM", "HIGH");
    assertThat(result.recommendation().toLowerCase()).contains("riego");
  }
}
