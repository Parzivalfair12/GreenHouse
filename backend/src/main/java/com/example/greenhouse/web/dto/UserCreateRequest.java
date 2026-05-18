package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
    @Email @NotBlank String email,
    @NotBlank String fullName,
    @NotBlank String password,
    @NotNull UserRole role) {}
