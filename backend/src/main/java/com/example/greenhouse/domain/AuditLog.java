package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** Operational log for manual and automatic actions. */
@Entity
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String action;

  public String detail;
  public LocalDateTime createdAt = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  public ActionOrigin origin = ActionOrigin.MANUAL;
}
