package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ZoneRequest(@NotBlank String name, String description, boolean active, @NotNull Long greenhouseId) {}
