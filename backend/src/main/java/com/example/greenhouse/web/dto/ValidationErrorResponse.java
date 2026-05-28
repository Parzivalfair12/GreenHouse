package com.example.greenhouse.web.dto;

import java.util.Map;

/**
 * Validation error body containing per-field error messages for
 * {@code 400 Bad Request} responses from Jakarta Bean Validation.
 *
 * @since 2.1.0
 */
public record ValidationErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors) {}
