package com.example.greenhouse.web.dto;

import java.util.Map;

public record ValidationErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors) {}
