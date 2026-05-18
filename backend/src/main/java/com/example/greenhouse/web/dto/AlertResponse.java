package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.domain.AlertSeverity;
import java.time.LocalDateTime;

public record AlertResponse(
    Long id,
    AlertSeverity severity,
    String message,
    boolean resolved,
    LocalDateTime createdAt,
    String sensorCode) {
  public static AlertResponse from(Alert alert) {
    String code = alert.sensor == null ? null : alert.sensor.code;
    return new AlertResponse(alert.id, alert.severity, alert.message, alert.resolved, alert.createdAt, code);
  }
}
