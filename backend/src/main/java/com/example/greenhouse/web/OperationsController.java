package com.example.greenhouse.web;

import com.example.greenhouse.service.AuditLogService;
import com.example.greenhouse.service.OperationsService;
import com.example.greenhouse.web.dto.ActuatorRequest;
import com.example.greenhouse.web.dto.ActuatorResponse;
import com.example.greenhouse.web.dto.AuditLogResponse;
import com.example.greenhouse.web.dto.AutomationRuleRequest;
import com.example.greenhouse.web.dto.AutomationRuleResponse;
import com.example.greenhouse.web.dto.ReadingRequest;
import com.example.greenhouse.web.dto.ReadingResponse;
import com.example.greenhouse.web.dto.SensorRequest;
import com.example.greenhouse.web.dto.ZoneRequest;
import com.example.greenhouse.web.dto.ZoneResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api")
public class OperationsController {
  private final OperationsService service;
  private final AuditLogService audit;

  public OperationsController(OperationsService service, AuditLogService audit) {
    this.service = service;
    this.audit = audit;
  }

  @GetMapping("/zones")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<ZoneResponse> zones() {
    return service.findZones().stream().map(ZoneResponse::from).toList();
  }

  @PostMapping("/zones")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ZoneResponse createZone(@Valid @RequestBody ZoneRequest request) {
    return ZoneResponse.from(service.createZone(request));
  }

  @PutMapping("/zones/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ZoneResponse updateZone(@PathVariable long id, @Valid @RequestBody ZoneRequest request) {
    return ZoneResponse.from(service.updateZone(id, request));
  }

  @DeleteMapping("/zones/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public void deleteZone(@PathVariable long id) {
    service.deleteZone(id);
  }

  @GetMapping("/sensors")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<?> sensors() {
    return service.findSensors().stream()
        .map(sensor -> java.util.Map.of(
            "id", sensor.id,
            "code", sensor.code,
            "type", sensor.type.name(),
            "unit", sensor.unit,
            "minThreshold", sensor.minThreshold,
            "maxThreshold", sensor.maxThreshold,
            "greenhouseId", sensor.greenhouse.id,
            "greenhouseName", sensor.greenhouse.name))
        .toList();
  }

  @PutMapping("/sensors/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public Object updateSensor(@PathVariable long id, @Valid @RequestBody SensorRequest request) {
    var sensor = service.updateSensor(id, request);
    return java.util.Map.of("id", sensor.id, "code", sensor.code, "type", sensor.type.name(), "unit", sensor.unit);
  }

  @DeleteMapping("/sensors/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public void deleteSensor(@PathVariable long id) {
    service.deleteSensor(id);
  }

  @GetMapping("/readings")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<ReadingResponse> readings() {
    return service.findReadings().stream().map(ReadingResponse::from).toList();
  }

  @PostMapping("/readings")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ReadingResponse createReading(@Valid @RequestBody ReadingRequest request) {
    return ReadingResponse.from(service.createReading(request));
  }

  @PutMapping("/readings/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ReadingResponse updateReading(@PathVariable long id, @Valid @RequestBody ReadingRequest request) {
    return ReadingResponse.from(service.updateReading(id, request));
  }

  @DeleteMapping("/readings/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public void deleteReading(@PathVariable long id) {
    service.deleteReading(id);
  }

  @GetMapping("/actuators")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<ActuatorResponse> actuators() {
    return service.findActuators().stream().map(ActuatorResponse::from).toList();
  }

  @PostMapping("/actuators")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ActuatorResponse createActuator(@Valid @RequestBody ActuatorRequest request) {
    return ActuatorResponse.from(service.createActuator(request));
  }

  @PutMapping("/actuators/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public ActuatorResponse updateActuator(@PathVariable long id, @Valid @RequestBody ActuatorRequest request) {
    return ActuatorResponse.from(service.updateActuator(id, request));
  }

  @DeleteMapping("/actuators/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public void deleteActuator(@PathVariable long id) {
    service.deleteActuator(id);
  }

  @GetMapping("/rules")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<AutomationRuleResponse> rules() {
    return service.findRules().stream().map(AutomationRuleResponse::from).toList();
  }

  @PostMapping("/rules")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public AutomationRuleResponse createRule(@Valid @RequestBody AutomationRuleRequest request) {
    return AutomationRuleResponse.from(service.createRule(request));
  }

  @PutMapping("/rules/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public AutomationRuleResponse updateRule(@PathVariable long id, @Valid @RequestBody AutomationRuleRequest request) {
    return AutomationRuleResponse.from(service.updateRule(id, request));
  }

  @DeleteMapping("/rules/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
  public void deleteRule(@PathVariable long id) {
    service.deleteRule(id);
  }

  @GetMapping("/audit-logs")
  @PreAuthorize("isAuthenticated()")
  public List<AuditLogResponse> logs() {
    return audit.latest().stream().map(AuditLogResponse::from).toList();
  }
}
