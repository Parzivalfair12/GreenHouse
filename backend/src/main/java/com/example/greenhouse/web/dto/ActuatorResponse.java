package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Actuator;

public record ActuatorResponse(Long id, String name, String type, boolean enabled, boolean active, Long greenhouseId) {
  public static ActuatorResponse from(Actuator actuator) {
    return new ActuatorResponse(
        actuator.id,
        actuator.name,
        actuator.type.name(),
        actuator.enabled,
        actuator.active,
        actuator.greenhouse.id);
  }
}
