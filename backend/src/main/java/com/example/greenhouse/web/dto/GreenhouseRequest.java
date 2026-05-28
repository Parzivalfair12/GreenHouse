package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record GreenhouseRequest(
    @NotBlank(message = "{validation.name.required}") String name,
    @NotBlank(message = "{validation.location.required}") String location,
    @Positive(message = "{validation.area.positive}") BigDecimal areaSquareMeters,
    boolean active) {}
