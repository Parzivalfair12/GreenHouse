package com.example.greenhouse.web.dto;

public record TaigaEpicResponse(
    int id,
    String ref,
    String subject,
    String description,
    String status,
    int projectId) {}
