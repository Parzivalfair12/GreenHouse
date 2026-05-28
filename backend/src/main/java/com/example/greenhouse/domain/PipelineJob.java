package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Job individual dentro de un workflow run de GitHub Actions.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Entity
public class PipelineJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** ID del job en GitHub (external). */
  public Long githubJobId;

  public Long githubRunId;
  public String jobName;
  public String status;
  public String conclusion;
  public int durationMs;
  public LocalDateTime startedAt;
  public LocalDateTime completedAt;
  public String logsUrl;
  public LocalDateTime syncedAt = LocalDateTime.now();
}
