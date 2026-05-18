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
import java.util.ArrayList;
import java.util.List;

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
  public List<Crop> crops = new ArrayList<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Sensor> sensors = new ArrayList<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Zone> zones = new ArrayList<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Actuator> actuators = new ArrayList<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<AutomationRule> rules = new ArrayList<>();

  @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<IrrigationEvent> irrigationEvents = new ArrayList<>();
}
