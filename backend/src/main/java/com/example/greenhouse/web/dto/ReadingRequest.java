package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReadingRequest(@NotNull Long sensorId, @NotNull BigDecimal value) {}
