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

/** Simulated device that can be enabled by manual actions or automation rules. */
@Entity
public class Actuator {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  @Enumerated(EnumType.STRING)
  public ActuatorType type = ActuatorType.IRRIGATION;

  public boolean enabled;
  public boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;
}
