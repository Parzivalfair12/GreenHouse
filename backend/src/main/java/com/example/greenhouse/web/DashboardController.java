package com.example.greenhouse.web;

import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import com.example.greenhouse.repository.ZoneRepository;
import com.example.greenhouse.web.dto.DashboardResponse;
import com.example.greenhouse.web.dto.ReadingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Resumen general del sistema de invernaderos")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final GreenhouseRepository greenhouses;
  private final ZoneRepository zones;
  private final SensorRepository sensors;
  private final ActuatorRepository actuators;
  private final AlertRepository alerts;
  private final ReadingRepository readings;
  private final MessageSource messages;

  public DashboardController(
      GreenhouseRepository greenhouses,
      ZoneRepository zones,
      SensorRepository sensors,
      ActuatorRepository actuators,
      AlertRepository alerts,
      ReadingRepository readings,
      MessageSource messages) {
    this.greenhouses = greenhouses;
    this.zones = zones;
    this.sensors = sensors;
    this.actuators = actuators;
    this.alerts = alerts;
    this.readings = readings;
    this.messages = messages;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Resumen del dashboard", description = "Devuelve metricas globales: conteos, ultima lectura y estado general")
  @ApiResponse(responseCode = "200", description = "Datos del dashboard")
  public DashboardResponse summary(Locale locale) {
    long openAlerts = alerts.countByResolvedFalse();
    ReadingResponse last = readings.findFirstByOrderByRecordedAtDesc()
        .map(ReadingResponse::from)
        .orElse(null);
    return new DashboardResponse(
        greenhouses.count(),
        greenhouses.countByActiveTrue(),
        zones.count(),
        sensors.count(),
        actuators.countByEnabledTrue(),
        openAlerts,
        last,
        openAlerts > 0
            ? messages.getMessage("dashboard.status.attention", null, locale)
            : messages.getMessage("dashboard.status.stable", null, locale));
  }
}
