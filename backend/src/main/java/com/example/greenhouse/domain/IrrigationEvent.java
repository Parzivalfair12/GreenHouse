package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registered irrigation activity. */
@Entity
public class IrrigationEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public LocalDateTime startedAt;

  @Positive
  public int durationMinutes;

  @Positive
  public BigDecimal waterLiters;

  @Enumerated(EnumType.STRING)
  public IrrigationMode mode = IrrigationMode.AUTOMATIC;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;

  @ManyToOne(fetch = FetchType.LAZY)
  public Actuator actuator;

  @ManyToOne(fetch = FetchType.LAZY)
  public Zone zone;

  @ManyToOne(fetch = FetchType.LAZY)
  public AutomationRule rule;
}
