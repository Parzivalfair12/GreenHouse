package com.example.greenhouse.web.dto;

public record DashboardResponse(
    long greenhouses,
    long activeGreenhouses,
    long zones,
    long sensors,
    long actuatorsEnabled,
    long openAlerts,
    ReadingResponse lastReading,
    String globalStatus) {}
