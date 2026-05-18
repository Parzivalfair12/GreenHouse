package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Measurement captured by a sensor. */
@Entity
public class Reading {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull
  @Column(name = "reading_value")
  public BigDecimal value;

  @NotNull
  public LocalDateTime recordedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  public Sensor sensor;
}
