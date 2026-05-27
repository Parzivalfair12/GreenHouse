package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SensorRequest(
    @NotBlank String code,
    @NotNull SensorType type,
    @NotBlank String unit,
    BigDecimal minThreshold,
    BigDecimal maxThreshold,
    @NotNull Long greenhouseId) {}
