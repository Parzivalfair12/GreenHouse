package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.ActuatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActuatorRequest(
    @NotBlank String name,
    @NotNull ActuatorType type,
    boolean enabled,
    boolean active,
    @NotNull Long greenhouseId) {}
