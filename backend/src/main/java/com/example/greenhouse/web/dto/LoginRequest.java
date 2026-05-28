package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Email/password login credentials with Jakarta Bean Validation i18n keys.
 *
 * @since 2.1.0
 */
public record LoginRequest(
    @Email(message = "{validation.email.invalid}") @NotBlank(message = "{validation.email.required}") String email,
    @NotBlank(message = "{validation.password.required}") String password,
    String fullName) {}
