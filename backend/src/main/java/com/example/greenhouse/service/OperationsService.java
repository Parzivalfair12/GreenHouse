package com.example.greenhouse.service;

import com.example.greenhouse.domain.ActionOrigin;
import com.example.greenhouse.domain.Actuator;
import com.example.greenhouse.domain.ActuatorType;
import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.domain.AlertSeverity;
import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.IrrigationEvent;
import com.example.greenhouse.domain.IrrigationMode;
import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.RuleType;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.domain.Zone;
import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.AutomationRuleRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.IrrigationEventRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio operativo central que agrupa CRUD de zonas, sensores, lecturas,
 * actuadores y reglas con auditoría automática.
 *
 * Cada operación de escritura registra un evento en la bitácora de auditoría
 * ({@link AuditLogService}) con origen MANUAL. Las lecturas adicionalmente
 * disparan el motor de reglas ({@link RuleEngineService}) para evaluación
 * de umbrales y automatizaciones.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class OperationsService {
  private static final Logger log = LoggerFactory.getLogger(OperationsService.class);

  private final GreenhouseRepository greenhouses;
  private final ZoneRepository zones;
  private final SensorRepository sensors;
  private final ReadingRepository readings;
  private final ActuatorRepository actuators;
  private final AlertRepository alerts;
  private final AutomationRuleRepository rules;
  private final IrrigationEventRepository irrigationEvents;
  private final AuditLogService audit;
  private final RuleEngineService ruleEngine;

  public OperationsService(
      GreenhouseRepository greenhouses,
      ZoneRepository zones,
      SensorRepository sensors,
      ReadingRepository readings,
      ActuatorRepository actuators,
      AlertRepository alerts,
      AutomationRuleRepository rules,
      IrrigationEventRepository irrigationEvents,
      AuditLogService audit,
      RuleEngineService ruleEngine) {
    this.greenhouses = greenhouses;
    this.zones = zones;
    this.sensors = sensors;
    this.readings = readings;
    this.actuators = actuators;
    this.alerts = alerts;
    this.rules = rules;
    this.irrigationEvents = irrigationEvents;
    this.audit = audit;
    this.ruleEngine = ruleEngine;
  }

  /**
   * Obtiene todas las zonas registradas.
   *
   * @return lista de zonas, vacía si no existen
   * @since 2.1.0
   */
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
  public Sensor createSensor(SensorRequest request) {
    Sensor sensor = new Sensor();
    sensor.code = request.code();
    sensor.type = request.type();
    sensor.unit = request.unit();
    sensor.minThreshold = request.minThreshold();
    sensor.maxThreshold = request.maxThreshold();
    sensor.greenhouse = greenhouses.findById(request.greenhouseId()).orElseThrow();
    audit.log("Crear sensor", sensor.code, ActionOrigin.MANUAL);
    return sensors.save(sensor);
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

  /**
   * Registra una nueva lectura de sensor y dispara el motor de reglas.
   *
   * Flujo:
   * <ol>
   *   <li>Busca el sensor asociado.</li>
   *   <li>Crea la lectura con timestamp actual.</li>
   *   <li>Audita la operación.</li>
   *   <li>Evalúa la lectura contra umbrales y reglas automáticas.</li>
   * </ol>
   *
   * @param request datos de la lectura (sensorId, valor)
   * @return la lectura persistida
   * @since 2.1.0
   */
  @Transactional
  public Reading createReading(ReadingRequest request) {
    Sensor sensor = sensors.findById(request.sensorId()).orElseThrow();
    Reading reading = new Reading();
    reading.sensor = sensor;
    reading.value = request.value();
    reading.recordedAt = LocalDateTime.now();
    readings.save(reading);
    audit.log("Registrar lectura", sensor.code + " = " + request.value() + " " + sensor.unit, ActionOrigin.MANUAL);
    ruleEngine.evaluateReading(sensor, reading);
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
    if (request.sensorId() != null) {
      rule.sensor = sensors.findById(request.sensorId()).orElse(null);
    }
    if (request.actuatorId() != null) {
      rule.actuator = actuators.findById(request.actuatorId()).orElse(null);
    }
  }
}
