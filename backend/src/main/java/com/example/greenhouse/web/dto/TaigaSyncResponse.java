package com.example.greenhouse.web.dto;

import java.util.List;

public record TaigaSyncResponse(
    boolean success,
    String message,
    int storiesCreated,
    int storiesUpdated,
    int storiesSkipped,
    List<String> errors) {}
