package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Reading;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReadingResponse(Long id, BigDecimal value, String unit, Long sensorId, String sensorCode, LocalDateTime recordedAt) {
  public static ReadingResponse from(Reading reading) {
    return new ReadingResponse(
        reading.id,
        reading.value,
        reading.sensor.unit,
        reading.sensor.id,
        reading.sensor.code,
        reading.recordedAt);
  }
}
