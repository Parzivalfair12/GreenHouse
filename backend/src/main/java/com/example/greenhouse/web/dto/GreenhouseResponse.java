package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Greenhouse;
import java.math.BigDecimal;
import java.util.List;

public record GreenhouseResponse(
    Long id,
    String name,
    String location,
    BigDecimal areaSquareMeters,
    boolean active,
    int cropCount,
    int sensorCount,
    List<CropSummary> crops,
    List<SensorSummary> sensors,
    List<IrrigationSummary> irrigationEvents) {
  public static GreenhouseResponse from(Greenhouse greenhouse) {
    return new GreenhouseResponse(
        greenhouse.id,
        greenhouse.name,
        greenhouse.location,
        greenhouse.areaSquareMeters,
        greenhouse.active,
        greenhouse.crops.size(),
        greenhouse.sensors.size(),
        greenhouse.crops.stream()
            .map(crop -> new CropSummary(crop.id, crop.name, crop.variety, crop.status.name(), crop.plantedAt, crop.expectedHarvestAt))
            .toList(),
        greenhouse.sensors.stream()
            .map(sensor -> new SensorSummary(sensor.id, sensor.code, sensor.type.name(), sensor.unit, sensor.minThreshold, sensor.maxThreshold))
            .toList(),
        greenhouse.irrigationEvents.stream()
            .map(event -> new IrrigationSummary(event.id, event.durationMinutes, event.waterLiters, event.mode.name()))
            .toList());
  }

  public record CropSummary(Long id, String name, String variety, String status, java.time.LocalDate plantedAt, java.time.LocalDate expectedHarvestAt) {}

  public record SensorSummary(Long id, String code, String type, String unit, BigDecimal minThreshold, BigDecimal maxThreshold) {}

  public record IrrigationSummary(Long id, int durationMinutes, BigDecimal waterLiters, String mode) {}
}
