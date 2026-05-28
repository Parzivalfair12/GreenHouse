package com.example.greenhouse.web.dto;

import java.util.List;

public record TaigaStoryResponse(
    int id,
    String ref,
    String subject,
    String description,
    String status,
    String epic,
    String priority,
    int projectId,
    String assignedTo,
    String createdDate,
    String modifiedDate,
    List<TaigaCriteriaResponse> criteria) {}
