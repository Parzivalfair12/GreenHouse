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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({SimulationService.class, RuleEngineService.class, AuditLogService.class, AlertService.class})
@ActiveProfiles("test")
class SimulationServiceTest {

  @Autowired
  GreenhouseRepository greenhouses;

  @Autowired
  SensorRepository sensors;

  @Autowired
  ReadingRepository readings;

  @Autowired
  SimulationService simulation;

  private Sensor tempSensor;

  @BeforeEach
  void setUp() {
    simulation.stop(); // reset shared state between tests
    Greenhouse gh = new Greenhouse();
    gh.name = "Test GH";
    gh.location = "A";
    gh.areaSquareMeters = BigDecimal.valueOf(100);
    gh.active = true;
    greenhouses.save(gh);

    tempSensor = new Sensor();
    tempSensor.code = "TEMP-TEST";
    tempSensor.type = SensorType.TEMPERATURE;
    tempSensor.unit = "C";
    tempSensor.minThreshold = BigDecimal.valueOf(18);
    tempSensor.maxThreshold = BigDecimal.valueOf(32);
    tempSensor.greenhouse = gh;
    sensors.save(tempSensor);
  }

  @Test
  void generatesAndPersistsReading() {
    simulation.start();
    int before = readings.findAll().size();

    simulation.generateAndSaveReading(tempSensor);

    List<Reading> all = readings.findAll();
    assertThat(all).hasSize(before + 1);
    Reading created = all.get(all.size() - 1);
    assertThat(created.sensor.id).isEqualTo(tempSensor.id);
    assertThat(created.value).isNotNull();
  }

  @Test
  void generatedValueIsWithinSoftBounds() {
    simulation.start();
    // Generate many readings to ensure bounds hold statistically
    for (int i = 0; i < 20; i++) {
      simulation.generateAndSaveReading(tempSensor);
    }

    List<Reading> all = readings.findAll();
    assertThat(all).isNotEmpty();
    for (Reading r : all) {
      double v = r.value.doubleValue();
      // Soft bounds allow 5% outside thresholds
      assertThat(v).isGreaterThan(15.0).isLessThan(36.0);
    }
  }

  @Test
  void startAndStopChangesState() {
    assertThat(simulation.isRunning()).isFalse();

    simulation.start();
    assertThat(simulation.isRunning()).isTrue();

    simulation.stop();
    assertThat(simulation.isRunning()).isFalse();
  }

  @Test
  void tickDoesNothingWhenStopped() {
    int before = readings.findAll().size();
    simulation.tick(); // running is false by default
    int after = readings.findAll().size();
    assertThat(after).isEqualTo(before);
  }

  @Test
  void tickGeneratesReadingsWhenRunning() {
    simulation.start();
    int before = readings.findAll().size();
    simulation.tick();
    int after = readings.findAll().size();
    assertThat(after).isGreaterThan(before);
  }
}
