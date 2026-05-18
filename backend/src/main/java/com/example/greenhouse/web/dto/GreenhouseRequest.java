package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GreenhouseRequest(
    @NotBlank String name,
    @NotBlank String location,
    @Positive BigDecimal areaSquareMeters,
    boolean active) {}
