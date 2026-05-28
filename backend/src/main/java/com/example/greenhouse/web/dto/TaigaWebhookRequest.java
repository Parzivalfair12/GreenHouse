package com.example.greenhouse.web.dto;

import java.util.Map;

public record TaigaWebhookRequest(
    String action,
    String type,
    int id,
    String ref,
    Map<String, Object> data) {}
