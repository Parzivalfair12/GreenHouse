package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
    @Email(message = "{validation.email.invalid}") @NotBlank(message = "{validation.email.required}") String email,
    @NotBlank(message = "{validation.fullName.required}") String fullName,
    @NotBlank(message = "{validation.password.required}") String password,
    @NotNull(message = "{validation.role.required}") UserRole role) {}
