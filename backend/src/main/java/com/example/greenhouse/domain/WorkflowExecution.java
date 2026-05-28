package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Ejecucion de workflow de GitHub Actions.
 * Mapea 1:1 con un workflow run de GitHub.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Entity
public class WorkflowExecution {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** ID del workflow run en GitHub (external). */
  public Long githubRunId;

  public String workflowName;
  public String branch;
  public String commitSha;
  public String commitMessage;
  public String actor;
  public String status;
  public String conclusion;
  public int durationMs;
  public LocalDateTime startedAt;
  public LocalDateTime completedAt;
  public String runUrl;
  public String event;
  public LocalDateTime syncedAt = LocalDateTime.now();
}
