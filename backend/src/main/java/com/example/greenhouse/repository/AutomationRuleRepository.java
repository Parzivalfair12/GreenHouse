package com.example.greenhouse.repository;

import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.RuleType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
  List<AutomationRule> findByEnabledTrueAndType(RuleType type);
  List<AutomationRule> findByEnabledTrueAndTypeAndGreenhouseId(RuleType type, Long greenhouseId);
}
