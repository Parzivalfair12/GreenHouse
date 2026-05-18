package com.example.greenhouse.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Device that captures environmental measurements. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Sensor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String code;

  @Enumerated(EnumType.STRING)
  public SensorType type;

  @NotBlank
  public String unit;

  public BigDecimal minThreshold;
  public BigDecimal maxThreshold;

  @ManyToOne(fetch = FetchType.LAZY)
  public Greenhouse greenhouse;

  @ManyToOne(fetch = FetchType.LAZY)
  public Zone zone;

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Reading> readings = new ArrayList<>();

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Alert> alerts = new ArrayList<>();
}
