package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Registro de auditoria del ciclo de vida DevOps.
 * Cada entrada representa un evento trazable: commit, pipeline, historia, issue, release.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Entity
public class AuditTrail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** Tipo de evento: COMMIT, PIPELINE, STORY, ISSUE, RELEASE, SYNC, VALIDATION */
  public String eventType;

  /** Identificador del recurso externo: commit SHA, runId, storyId, issueId */
  public String externalId;

  /** Descripcion legible del evento */
  public String description;

  /** Autor del commit o actor del pipeline */
  public String actor;

  /** Branch relacionada */
  public String branch;

  /** Estado: SUCCESS, FAILURE, PENDING, SKIPPED */
  public String status;

  /** Servicio origen: GitHubActions, Taiga, StoryGeneration, PipelineValidation */
  public String sourceService;

  /** Correlation ID para trazabilidad distribuida */
  public String correlationId;

  /** Duracion en ms si aplica */
  public Integer durationMs;

  /** Metadata JSON adicional */
  public String metadata;

  /** Error message si fallo */
  public String errorMessage;

  public LocalDateTime createdAt = LocalDateTime.now();
}
