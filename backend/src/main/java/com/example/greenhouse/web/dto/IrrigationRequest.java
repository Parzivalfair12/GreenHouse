package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.IrrigationMode;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record IrrigationRequest(
    @Positive int durationMinutes,
    @Positive BigDecimal waterLiters,
    IrrigationMode mode) {}
