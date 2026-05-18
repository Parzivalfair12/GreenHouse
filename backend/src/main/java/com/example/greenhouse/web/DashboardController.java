package com.example.greenhouse.web;

import com.example.greenhouse.repository.ActuatorRepository;
import com.example.greenhouse.repository.AlertRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import com.example.greenhouse.repository.ZoneRepository;
import com.example.greenhouse.web.dto.DashboardResponse;
import com.example.greenhouse.web.dto.ReadingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final GreenhouseRepository greenhouses;
  private final ZoneRepository zones;
  private final SensorRepository sensors;
  private final ActuatorRepository actuators;
  private final AlertRepository alerts;
  private final ReadingRepository readings;

  public DashboardController(
      GreenhouseRepository greenhouses,
      ZoneRepository zones,
      SensorRepository sensors,
      ActuatorRepository actuators,
      AlertRepository alerts,
      ReadingRepository readings) {
    this.greenhouses = greenhouses;
    this.zones = zones;
    this.sensors = sensors;
    this.actuators = actuators;
    this.alerts = alerts;
    this.readings = readings;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public DashboardResponse summary() {
    long openAlerts = alerts.findByResolvedFalseOrderByCreatedAtDesc().size();
    ReadingResponse last = readings.findAll().stream()
        .max(java.util.Comparator.comparing(reading -> reading.recordedAt))
        .map(ReadingResponse::from)
        .orElse(null);
    return new DashboardResponse(
        greenhouses.count(),
        greenhouses.findAll().stream().filter(greenhouse -> greenhouse.active).count(),
        zones.count(),
        sensors.count(),
        actuators.findAll().stream().filter(actuator -> actuator.enabled).count(),
        openAlerts,
        last,
        openAlerts > 0 ? "REQUIERE_ATENCION" : "ESTABLE");
  }
}
