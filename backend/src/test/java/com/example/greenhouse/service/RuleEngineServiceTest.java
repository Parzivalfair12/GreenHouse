package com.example.greenhouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.greenhouse.domain.Actuator;
import com.example.greenhouse.domain.ActuatorType;
import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.IrrigationEvent;
import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.RuleType;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.AuditLogRepository;
import com.example.greenhouse.repository.AutomationRuleRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.IrrigationEventRepository;
import com.example.greenhouse.repository.SensorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({RuleEngineService.class, AuditLogService.class, AlertService.class})
@ActiveProfiles("test")
class RuleEngineServiceTest {

  @Autowired
  GreenhouseRepository greenhouses;
  @Autowired
  SensorRepository sensors;
  @Autowired
  ActuatorRepository actuators;
  @Autowired
  AutomationRuleRepository rules;
  @Autowired
  AlertRepository alerts;
  @Autowired
  IrrigationEventRepository irrigationEvents;
  @Autowired
  AuditLogRepository auditLogs;
  @Autowired
  RuleEngineService ruleEngine;

  private Greenhouse gh;
  private Sensor humSensor;
  private Actuator irrigation;

  @BeforeEach
  void setUp() {
    gh = new Greenhouse();
    gh.name = "Test GH";
    gh.location = "B";
    gh.areaSquareMeters = BigDecimal.valueOf(50);
    gh.active = true;
    greenhouses.save(gh);

    humSensor = new Sensor();
    humSensor.code = "HUM-TEST";
    humSensor.type = SensorType.HUMIDITY;
    humSensor.unit = "%";
    humSensor.minThreshold = BigDecimal.valueOf(40);
    humSensor.maxThreshold = BigDecimal.valueOf(80);
    humSensor.greenhouse = gh;
    sensors.save(humSensor);

    irrigation = new Actuator();
    irrigation.name = "Riego Test";
    irrigation.type = ActuatorType.IRRIGATION;
    irrigation.active = true;
    irrigation.enabled = false;
    irrigation.greenhouse = gh;
    actuators.save(irrigation);

    AutomationRule rule = new AutomationRule();
    rule.name = "Regla baja humedad";
    rule.type = RuleType.LOW_HUMIDITY_IRRIGATION;
    rule.enabled = true;
    rule.threshold = BigDecimal.valueOf(35);
    rule.greenhouse = gh;
    rule.sensor = humSensor;
    rule.actuator = irrigation;
    rules.save(rule);
  }

  @Test
  void lowHumidityCreatesAlert() {
    Reading reading = createReading(BigDecimal.valueOf(30));
    ruleEngine.evaluateReading(humSensor, reading);

    List<Alert> allAlerts = alerts.findAll();
    assertThat(allAlerts).isNotEmpty();
    // Either threshold alert or rule alert is acceptable
    boolean hasLowHumidity = allAlerts.stream().anyMatch(a -> a.message.contains("Humedad baja"));
    boolean hasThreshold = allAlerts.stream().anyMatch(a -> a.message.contains("por debajo del limite"));
    assertThat(hasLowHumidity || hasThreshold).isTrue();
    assertThat(allAlerts.get(0).resolved).isFalse();
  }

  @Test
  void lowHumidityActivatesIrrigation() {
    Reading reading = createReading(BigDecimal.valueOf(30));
    ruleEngine.evaluateReading(humSensor, reading);

    Actuator updated = actuators.findById(irrigation.id).orElseThrow();
    assertThat(updated.enabled).isTrue();

    List<IrrigationEvent> events = irrigationEvents.findAll();
    assertThat(events).isNotEmpty();
    IrrigationEvent ev = events.get(0);
    assertThat(ev.mode.name()).isEqualTo("AUTOMATIC");
    assertThat(ev.greenhouse.id).isEqualTo(gh.id);
  }

  @Test
  void lowHumidityCreatesAuditLog() {
    int before = auditLogs.findAll().size();
    Reading reading = createReading(BigDecimal.valueOf(30));
    ruleEngine.evaluateReading(humSensor, reading);
    int after = auditLogs.findAll().size();

    assertThat(after).isGreaterThan(before);
  }

  @Test
  void normalHumidityDeactivatesIrrigation() {
    // First activate it
    Reading lowReading = createReading(BigDecimal.valueOf(30));
    ruleEngine.evaluateReading(humSensor, lowReading);
    Actuator activated = actuators.findById(irrigation.id).orElseThrow();
    assertThat(activated.enabled).isTrue();

    // Now send normal reading
    Reading normalReading = createReading(BigDecimal.valueOf(60));
    ruleEngine.evaluateReading(humSensor, normalReading);

    Actuator deactivated = actuators.findById(irrigation.id).orElseThrow();
    assertThat(deactivated.enabled).isFalse();
  }

  @Test
  void thresholdBreachAboveMaxCreatesAlert() {
    Sensor temp = new Sensor();
    temp.code = "TEMP-TEST";
    temp.type = SensorType.TEMPERATURE;
    temp.unit = "C";
    temp.minThreshold = BigDecimal.valueOf(18);
    temp.maxThreshold = BigDecimal.valueOf(32);
    temp.greenhouse = gh;
    sensors.save(temp);

    Reading reading = new Reading();
    reading.sensor = temp;
    reading.value = BigDecimal.valueOf(40);
    reading.recordedAt = LocalDateTime.now();

    ruleEngine.evaluateReading(temp, reading);

    List<Alert> allAlerts = alerts.findAll();
    assertThat(allAlerts).isNotEmpty();
    assertThat(allAlerts.get(0).message).contains("por encima del limite");
  }

  private Reading createReading(BigDecimal value) {
    Reading r = new Reading();
    r.sensor = humSensor;
    r.value = value;
    r.recordedAt = LocalDateTime.now();
    return r;
  }
}
