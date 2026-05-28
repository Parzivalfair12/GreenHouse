package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Auditoria de releases y despliegues.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Entity
public class ReleaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String tagName;
  public String releaseName;
  public String commitSha;
  public String actor;
  public String changelog;
  public boolean published;
  public String artifacts;
  public LocalDateTime releasedAt;
  public LocalDateTime syncedAt = LocalDateTime.now();
}
