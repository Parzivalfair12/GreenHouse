package com.example.greenhouse.web.dto;

import java.util.Map;

public record IaPredictionResponse(
    Double predictedTemperature,
    Double predictedHumidity,
    String riskLevel,
    Map<String, Boolean> anomalies) {}
