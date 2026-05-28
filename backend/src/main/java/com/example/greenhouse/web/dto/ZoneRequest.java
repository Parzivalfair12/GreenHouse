package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ZoneRequest(
    @NotBlank(message = "{validation.name.required}") String name,
    String description,
    boolean active,
    @NotNull(message = "{validation.greenhouseId.required}") Long greenhouseId) {}
