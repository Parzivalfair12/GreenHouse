package com.example.greenhouse.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload para actualizar el perfil del usuario autenticado.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
public record UserProfileUpdateRequest(
    @Email(message = "{validation.email.invalid}") @NotBlank(message = "{validation.email.required}") String email,
    @NotBlank(message = "{validation.fullName.required}") String fullName) {}
