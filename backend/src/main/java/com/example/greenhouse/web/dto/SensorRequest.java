package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SensorRequest(
    @NotBlank(message = "{validation.sensorCode.required}") String code,
    @NotNull(message = "{validation.sensorType.required}") SensorType type,
    @NotBlank(message = "{validation.unit.required}") String unit,
    BigDecimal minThreshold,
    BigDecimal maxThreshold,
    @NotNull(message = "{validation.greenhouseId.required}") Long greenhouseId) {}
