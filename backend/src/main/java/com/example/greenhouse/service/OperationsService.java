package com.example.greenhouse.service;

import com.example.greenhouse.domain.ActionOrigin;
import com.example.greenhouse.domain.Actuator;
import com.example.greenhouse.domain.ActuatorType;
import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.domain.AlertSeverity;
import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.RuleType;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.domain.Zone;
import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.AutomationRuleRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import com.example.greenhouse.repository.ZoneRepository;
import com.example.greenhouse.web.dto.ActuatorRequest;
import com.example.greenhouse.web.dto.AutomationRuleRequest;
import com.example.greenhouse.web.dto.ReadingRequest;
import com.example.greenhouse.web.dto.SensorRequest;
import com.example.greenhouse.web.dto.ZoneRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationsService {
  private final GreenhouseRepository greenhouses;
  private final ZoneRepository zones;
  private final SensorRepository sensors;
  private final ReadingRepository readings;
  private final ActuatorRepository actuators;
  private final AlertRepository alerts;
  private final AutomationRuleRepository rules;
  private final AuditLogService audit;

  public OperationsService(
      GreenhouseRepository greenhouses,
      ZoneRepository zones,
      SensorRepository sensors,
      ReadingRepository readings,
      ActuatorRepository actuators,
      AlertRepository alerts,
      AutomationRuleRepository rules,
      AuditLogService audit) {
    this.greenhouses = greenhouses;
    this.zones = zones;
    this.sensors = sensors;
    this.readings = readings;
    this.actuators = actuators;
    this.alerts = alerts;
    this.rules = rules;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public List<Zone> findZones() {
    return zones.findAll();
  }

  @Transactional
  public Zone createZone(ZoneRequest request) {
    Zone zone = new Zone();
    applyZone(zone, request);
    audit.log("Crear zona", zone.name, ActionOrigin.MANUAL);
    return zones.save(zone);
  }

  @Transactional
  public Zone updateZone(long id, ZoneRequest request) {
    Zone zone = zones.findById(id).orElseThrow();
    applyZone(zone, request);
    audit.log("Actualizar zona", zone.name, ActionOrigin.MANUAL);
    return zones.save(zone);
  }

  @Transactional
  public void deleteZone(long id) {
    zones.deleteById(id);
    audit.log("Eliminar zona", "Zona " + id, ActionOrigin.MANUAL);
  }

  @Transactional(readOnly = true)
  public List<Sensor> findSensors() {
    return sensors.findAll();
  }

  @Transactional
  public Sensor updateSensor(long id, SensorRequest request) {
    Sensor sensor = sensors.findById(id).orElseThrow();
    sensor.code = request.code();
    sensor.type = request.type();
    sensor.unit = request.unit();
    sensor.minThreshold = request.minThreshold();
    sensor.maxThreshold = request.maxThreshold();
    audit.log("Actualizar sensor", sensor.code, ActionOrigin.MANUAL);
    return sensors.save(sensor);
  }

  @Transactional
  public void deleteSensor(long id) {
    sensors.deleteById(id);
    audit.log("Eliminar sensor", "Sensor " + id, ActionOrigin.MANUAL);
  }

  @Transactional(readOnly = true)
  public List<Reading> findReadings() {
    return readings.findAll();
  }

  @Transactional
  public Reading createReading(ReadingRequest request) {
    Sensor sensor = sensors.findById(request.sensorId()).orElseThrow();
    Reading reading = new Reading();
    reading.sensor = sensor;
    reading.value = request.value();
    reading.recordedAt = LocalDateTime.now();
    readings.save(reading);
    audit.log("Registrar lectura", sensor.code + " = " + request.value() + " " + sensor.unit, ActionOrigin.MANUAL);
    evaluateThresholds(sensor, reading);
    evaluateRules(sensor, reading);
    return reading;
  }

  @Transactional
  public Reading updateReading(long id, ReadingRequest request) {
    Reading reading = readings.findById(id).orElseThrow();
    reading.sensor = sensors.findById(request.sensorId()).orElseThrow();
    reading.value = request.value();
    audit.log("Actualizar lectura", "Lectura " + id, ActionOrigin.MANUAL);
    return readings.save(reading);
  }

  @Transactional
  public void deleteReading(long id) {
    readings.deleteById(id);
    audit.log("Eliminar lectura", "Lectura " + id, ActionOrigin.MANUAL);
  }

  @Transactional(readOnly = true)
  public List<Actuator> findActuators() {
    return actuators.findAll();
  }

  @Transactional
  public Actuator createActuator(ActuatorRequest request) {
    Actuator actuator = new Actuator();
    applyActuator(actuator, request);
    audit.log("Crear actuador", actuator.name, ActionOrigin.MANUAL);
    return actuators.save(actuator);
  }

  @Transactional
  public Actuator updateActuator(long id, ActuatorRequest request) {
    Actuator actuator = actuators.findById(id).orElseThrow();
    applyActuator(actuator, request);
    audit.log("Actualizar actuador", actuator.name, ActionOrigin.MANUAL);
    return actuators.save(actuator);
  }

  @Transactional
  public void deleteActuator(long id) {
    actuators.deleteById(id);
    audit.log("Eliminar actuador", "Actuador " + id, ActionOrigin.MANUAL);
  }

  @Transactional(readOnly = true)
  public List<AutomationRule> findRules() {
    return rules.findAll();
  }

  @Transactional
  public AutomationRule createRule(AutomationRuleRequest request) {
    AutomationRule rule = new AutomationRule();
    applyRule(rule, request);
    audit.log("Crear regla", rule.name, ActionOrigin.MANUAL);
    return rules.save(rule);
  }

  @Transactional
  public AutomationRule updateRule(long id, AutomationRuleRequest request) {
    AutomationRule rule = rules.findById(id).orElseThrow();
    applyRule(rule, request);
    audit.log("Actualizar regla", rule.name, ActionOrigin.MANUAL);
    return rules.save(rule);
  }

  @Transactional
  public void deleteRule(long id) {
    rules.deleteById(id);
    audit.log("Eliminar regla", "Regla " + id, ActionOrigin.MANUAL);
  }

  private void applyZone(Zone zone, ZoneRequest request) {
    zone.name = request.name();
    zone.description = request.description();
    zone.active = request.active();
    zone.greenhouse = greenhouses.findById(request.greenhouseId()).orElseThrow();
  }

  private void applyActuator(Actuator actuator, ActuatorRequest request) {
    actuator.name = request.name();
    actuator.type = request.type();
    actuator.enabled = request.enabled();
    actuator.active = request.active();
    actuator.greenhouse = greenhouses.findById(request.greenhouseId()).orElseThrow();
  }

  private void applyRule(AutomationRule rule, AutomationRuleRequest request) {
    rule.name = request.name();
    rule.type = request.type();
    rule.threshold = request.threshold();
    rule.enabled = request.enabled();
    rule.greenhouse = greenhouses.findById(request.greenhouseId()).orElseThrow();
  }

  private void evaluateThresholds(Sensor sensor, Reading reading) {
    if (sensor.maxThreshold != null && reading.value.compareTo(sensor.maxThreshold) > 0) {
      createAlert(sensor, "Lectura por encima del limite configurado");
    }
    if (sensor.minThreshold != null && reading.value.compareTo(sensor.minThreshold) < 0) {
      createAlert(sensor, "Lectura por debajo del limite configurado");
    }
  }

  private void evaluateRules(Sensor sensor, Reading reading) {
    if (sensor.type != SensorType.HUMIDITY && sensor.type != SensorType.SOIL_MOISTURE) {
      return;
    }
    List<AutomationRule> activeRules = rules.findByEnabledTrueAndTypeAndGreenhouseId(
        RuleType.LOW_HUMIDITY_IRRIGATION,
        sensor.greenhouse.id);
    for (AutomationRule rule : activeRules) {
      if (reading.value.compareTo(rule.threshold) < 0) {
        actuators.findFirstByGreenhouseIdAndTypeAndActiveTrue(sensor.greenhouse.id, ActuatorType.IRRIGATION)
            .ifPresent(actuator -> {
              actuator.enabled = true;
              actuators.save(actuator);
              audit.log("Regla automatica", "Humedad baja activo " + actuator.name, ActionOrigin.AUTOMATIC);
            });
        createAlert(sensor, "Humedad baja: riego simulado activado");
      }
    }
  }

  private void createAlert(Sensor sensor, String message) {
    Alert alert = new Alert();
    alert.sensor = sensor;
    alert.message = message;
    alert.severity = AlertSeverity.WARNING;
    alerts.save(alert);
    audit.log("Generar alerta", sensor.code + ": " + message, ActionOrigin.AUTOMATIC);
  }
}
