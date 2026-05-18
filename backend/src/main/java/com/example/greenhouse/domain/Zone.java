package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

/** Functional area inside a greenhouse, such as germination or irrigation zone. */
@Entity
public class Zone {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  public String description;
  public boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;
}
