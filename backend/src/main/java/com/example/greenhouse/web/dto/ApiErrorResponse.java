package com.example.greenhouse.web.dto;

public record ApiErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path) {}
