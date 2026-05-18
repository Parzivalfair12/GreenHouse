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
import java.time.LocalDate;

/** Crop planted inside a greenhouse. */
@Entity
public class Crop {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  public String variety;
  public LocalDate plantedAt;
  public LocalDate expectedHarvestAt;

  @Enumerated(EnumType.STRING)
  public CropStatus status = CropStatus.GERMINATING;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;
}
