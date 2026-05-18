package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/** Automation rule evaluated when readings are registered. */
@Entity
public class AutomationRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  @Enumerated(EnumType.STRING)
  public RuleType type = RuleType.LOW_HUMIDITY_IRRIGATION;

  public BigDecimal threshold;
  public boolean enabled = true;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;
}
