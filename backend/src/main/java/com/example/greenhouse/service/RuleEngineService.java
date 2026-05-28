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
import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.AutomationRuleRepository;
import com.example.greenhouse.repository.IrrigationEventRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule engine that evaluates sensor readings against thresholds and automation rules.
 * Triggered after every reading creation (manual or simulated).
 */
@Service
public class RuleEngineService {

  private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

  private final AlertRepository alerts;
  private final ActuatorRepository actuators;
  private final AutomationRuleRepository rules;
  private final IrrigationEventRepository irrigationEvents;
  private final AuditLogService audit;
  private final MessageSource messages;

  public RuleEngineService(
      AlertRepository alerts,
      ActuatorRepository actuators,
      AutomationRuleRepository rules,
      IrrigationEventRepository irrigationEvents,
      AuditLogService audit,
      MessageSource messages) {
    this.alerts = alerts;
    this.actuators = actuators;
    this.rules = rules;
    this.irrigationEvents = irrigationEvents;
    this.audit = audit;
    this.messages = messages;
  }

  private String msg(String key, Object... args) {
    return messages.getMessage(key, args, key, LocaleContextHolder.getLocale());
  }

  @Transactional
  public void evaluateReading(Sensor sensor, Reading reading) {
    evaluateThresholds(sensor, reading);
    evaluateRules(sensor, reading);
  }

  private void evaluateThresholds(Sensor sensor, Reading reading) {
    if (sensor.maxThreshold != null && reading.value.compareTo(sensor.maxThreshold) > 0) {
      createAlert(sensor, msg("alert.reading.above.threshold", reading.value, sensor.maxThreshold), AlertSeverity.WARNING);
    }
    if (sensor.minThreshold != null && reading.value.compareTo(sensor.minThreshold) < 0) {
      createAlert(sensor, msg("alert.reading.below.threshold", reading.value, sensor.minThreshold), AlertSeverity.CRITICAL);
    }
  }

  private void evaluateRules(Sensor sensor, Reading reading) {
    if (sensor.type != SensorType.HUMIDITY && sensor.type != SensorType.SOIL_MOISTURE) {
      return;
    }
    if (sensor.greenhouse == null || sensor.greenhouse.id == null) {
      return;
    }

    List<AutomationRule> activeRules = rules.findByEnabledTrueAndTypeAndGreenhouseId(
        RuleType.LOW_HUMIDITY_IRRIGATION, sensor.greenhouse.id);

    for (AutomationRule rule : activeRules) {
      if (rule.sensor != null && !rule.sensor.id.equals(sensor.id)) {
        continue;
      }
      if (rule.threshold == null) {
        continue;
      }

      if (reading.value.compareTo(rule.threshold) < 0) {
        activateIrrigation(rule, sensor, reading);
      } else {
        deactivateIrrigation(rule, sensor, reading);
      }
    }
  }

  private void activateIrrigation(AutomationRule rule, Sensor sensor, Reading reading) {
    Actuator target = rule.actuator;
    if (target == null) {
      target = actuators.findFirstByGreenhouseIdAndTypeAndActiveTrue(
              sensor.greenhouse.id, ActuatorType.IRRIGATION)
          .orElse(null);
    }
    if (target == null) {
      log.warn("No irrigation actuator found for greenhouse {} on rule {}",
          sensor.greenhouse.id, rule.id);
      createAlert(sensor, msg("alert.no.actuator", reading.value), AlertSeverity.CRITICAL);
      return;
    }

    if (!target.enabled) {
      target.enabled = true;
      actuators.save(target);

      IrrigationEvent event = new IrrigationEvent();
      event.startedAt = LocalDateTime.now();
      event.durationMinutes = 10;
      event.waterLiters = BigDecimal.valueOf(5.0);
      event.mode = IrrigationMode.AUTOMATIC;
      event.greenhouse = sensor.greenhouse;
      event.actuator = target;
      event.rule = rule;
      if (sensor.zone != null) {
        event.zone = sensor.zone;
      }
      irrigationEvents.save(event);

      audit.log(msg("audit.rule.activated.title"),
          msg("audit.rule.activated.body", reading.value, target.name, rule.name),
          ActionOrigin.AUTOMATIC, sensor.greenhouse, null);
      log.info("Activated actuator {} for rule {} in greenhouse {}",
          target.id, rule.id, sensor.greenhouse.id);
    }

    // Only create alert if not already resolved recently for this sensor
    boolean alreadyOpen = alerts.findByResolvedFalseOrderByCreatedAtDesc().stream()
        .anyMatch(a -> a.sensor != null && a.sensor.id.equals(sensor.id)
            && a.severity == AlertSeverity.WARNING && !a.resolved);
    if (!alreadyOpen) {
      createAlert(sensor, msg("alert.irrigation.activated", reading.value, rule.name), AlertSeverity.WARNING);
    }
  }

  private void deactivateIrrigation(AutomationRule rule, Sensor sensor, Reading reading) {
    Actuator target = rule.actuator;
    if (target == null) {
      target = actuators.findFirstByGreenhouseIdAndTypeAndActiveTrue(
              sensor.greenhouse.id, ActuatorType.IRRIGATION)
          .orElse(null);
    }
    if (target != null && target.enabled) {
      target.enabled = false;
      actuators.save(target);
      audit.log(msg("audit.rule.deactivated.title"),
          msg("audit.rule.deactivated.body", reading.value, target.name, rule.name),
          ActionOrigin.AUTOMATIC, sensor.greenhouse, null);
      log.info("Deactivated actuator {} for rule {} in greenhouse {}",
          target.id, rule.id, sensor.greenhouse.id);
    }
  }

  private void createAlert(Sensor sensor, String message, AlertSeverity severity) {
    Alert alert = new Alert();
    alert.sensor = sensor;
    alert.message = message;
    alert.severity = severity;
    alert.resolved = false;
    alert.createdAt = LocalDateTime.now();
    alerts.save(alert);
    audit.log("Generar alerta", sensor.code + ": " + message, ActionOrigin.AUTOMATIC,
        sensor.greenhouse != null ? sensor.greenhouse : null, null);
  }
}
