package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Sensor;

public record SensorResponse(
    Long id,
    String code,
    String type,
    String unit,
    java.math.BigDecimal minThreshold,
    java.math.BigDecimal maxThreshold,
    Long greenhouseId,
    String greenhouseName
) {
  public static SensorResponse from(Sensor sensor) {
    return new SensorResponse(
        sensor.id,
        sensor.code,
        sensor.type != null ? sensor.type.name() : null,
        sensor.unit,
        sensor.minThreshold,
        sensor.maxThreshold,
        sensor.greenhouse != null ? sensor.greenhouse.id : null,
        sensor.greenhouse != null ? sensor.greenhouse.name : null
    );
  }
}
