package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AutomationRuleRequest(
    @NotBlank String name,
    @NotNull RuleType type,
    @NotNull BigDecimal threshold,
    boolean enabled,
    @NotNull Long greenhouseId) {}
