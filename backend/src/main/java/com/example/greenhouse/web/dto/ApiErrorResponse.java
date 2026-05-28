package com.example.greenhouse.web.dto;

/**
 * Standard RFC 7807-compatible error body returned for all API exceptions.
 *
 * @param timestamp ISO-8601 instant of the error
 * @param status    HTTP status code
 * @param error     HTTP reason phrase
 * @param message   human-readable error message (i18n-resolved)
 * @param path      request URI that triggered the error
 * @since 2.1.0
 */
public record ApiErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path) {}
