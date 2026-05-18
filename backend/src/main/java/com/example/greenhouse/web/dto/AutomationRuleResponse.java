package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.AutomationRule;
import java.math.BigDecimal;

public record AutomationRuleResponse(Long id, String name, String type, BigDecimal threshold, boolean enabled, Long greenhouseId) {
  public static AutomationRuleResponse from(AutomationRule rule) {
    return new AutomationRuleResponse(rule.id, rule.name, rule.type.name(), rule.threshold, rule.enabled, rule.greenhouse.id);
  }
}
