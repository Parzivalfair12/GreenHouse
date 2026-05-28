package com.example.greenhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class PipelineExecution {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String workflowName;
  public String jobName;
  public String status;
  public String branch;
  public String commitHash;
  public String commitMessage;
  public String triggerEvent;
  public int durationMs;
  public int testsPassed;
  public int testsFailed;
  public int testsSkipped;
  public double coveragePercent;
  public String errorMessage;
  public String artifactUrl;
  public LocalDateTime createdAt = LocalDateTime.now();
}
