package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CropRequest(
    @NotBlank String name,
    String variety,
    LocalDate plantedAt,
    LocalDate expectedHarvestAt) {}
