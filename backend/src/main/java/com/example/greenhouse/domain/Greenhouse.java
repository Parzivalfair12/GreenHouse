package com.example.greenhouse.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/** Physical greenhouse that groups crops, sensors and irrigation events. */
@Entity
public class Greenhouse {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotBlank
  public String name;

  @NotBlank
  public String location;

  @Positive
  public BigDecimal areaSquareMeters;

  public boolean active = true;

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<Crop> crops = new LinkedHashSet<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<Sensor> sensors = new LinkedHashSet<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<Zone> zones = new LinkedHashSet<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<Actuator> actuators = new LinkedHashSet<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<AutomationRule> rules = new LinkedHashSet<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<IrrigationEvent> irrigationEvents = new LinkedHashSet<>();
}
