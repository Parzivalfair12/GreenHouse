package com.example.greenhouse.service;

import com.example.greenhouse.domain.PipelineJob;
import com.example.greenhouse.domain.WorkflowExecution;
import com.example.greenhouse.repository.PipelineJobRepository;
import com.example.greenhouse.repository.WorkflowExecutionRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio de integracion con GitHub Actions API.
 * Consume la API REST real de GitHub para obtener workflow runs, jobs y releases.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Service
public class GitHubActionsService {

  private static final Logger log = LoggerFactory.getLogger(GitHubActionsService.class);
  private static final String GITHUB_API_BASE = "https://api.github.com";

  private final RestTemplate rest;
  private final WorkflowExecutionRepository workflowRepo;
  private final PipelineJobRepository jobRepo;
  private final String owner;
  private final String repo;
  private final String token;

  public GitHubActionsService(
      RestTemplate rest,
      WorkflowExecutionRepository workflowRepo,
      PipelineJobRepository jobRepo,
      @Value("${app.github.owner:}") String owner,
      @Value("${app.github.repo:}") String repo,
      @Value("${app.github.token:}") String token) {
    this.rest = rest;
    this.workflowRepo = workflowRepo;
    this.jobRepo = jobRepo;
    this.owner = owner;
    this.repo = repo;
    this.token = token;
  }

  public boolean isEnabled() {
    return token != null && !token.isBlank() && owner != null && !owner.isBlank() && repo != null && !repo.isBlank();
  }

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/vnd.github+json");
    headers.set("X-GitHub-Api-Version", "2022-11-28");
    if (token != null && !token.isBlank()) {
      headers.setBearerAuth(token);
    }
    return headers;
  }

  /**
   * Sincroniza los ultimos 30 workflow runs de GitHub Actions a la base de datos local.
   *
   * @return mapa con estadisticas de la sincronizacion
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> syncWorkflowRuns() {
    if (!isEnabled()) {
      log.warn("GitHub Actions API no configurada — configure GITHUB_TOKEN, GITHUB_OWNER y GITHUB_REPO");
      return Map.of("synced", 0, "message", "GitHub Actions no configurado");
    }

    String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs?per_page=30";
    int synced = 0;
    int jobsSynced = 0;

    try {
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

      List<Map<String, Object>> runs = (List<Map<String, Object>>) response.getBody().get("workflow_runs");
      if (runs == null) {
        return Map.of("synced", 0, "message", "No se encontraron workflow runs");
      }

      for (Map<String, Object> run : runs) {
        Long runId = Long.valueOf(String.valueOf(run.get("id")));
        Optional<WorkflowExecution> existing = workflowRepo.findByGithubRunId(runId);
        WorkflowExecution exec = existing.orElse(new WorkflowExecution());
        exec.githubRunId = runId;
        exec.workflowName = String.valueOf(run.getOrDefault("name", ""));
        exec.branch = String.valueOf(run.getOrDefault("head_branch", ""));
        exec.commitSha = String.valueOf(run.getOrDefault("head_sha", ""));
    Object headCommit = run.get("head_commit");
    if (headCommit instanceof Map<?, ?> hm) {
      Object msg = hm.get("message");
      exec.commitMessage = msg != null ? String.valueOf(msg) : "";
    } else {
      exec.commitMessage = "";
    }
    Object actorObj = run.get("actor");
    if (actorObj instanceof Map<?, ?> am) {
      Object login = am.get("login");
      exec.actor = login != null ? String.valueOf(login) : "";
    } else {
      exec.actor = "";
    }
        exec.status = String.valueOf(run.getOrDefault("status", ""));
        exec.conclusion = String.valueOf(run.getOrDefault("conclusion", ""));
        exec.runUrl = String.valueOf(run.getOrDefault("html_url", ""));
        exec.event = String.valueOf(run.getOrDefault("event", ""));
        exec.startedAt = parseDate(String.valueOf(run.getOrDefault("run_started_at", "")));
        exec.completedAt = parseDate(String.valueOf(run.getOrDefault("updated_at", "")));

        Long runDuration = run.get("run_duration_ms") != null
            ? Long.valueOf(String.valueOf(run.get("run_duration_ms"))) : 0L;
        if (runDuration == 0 && exec.startedAt != null && exec.completedAt != null) {
          runDuration = Duration.between(exec.startedAt, exec.completedAt).toMillis();
        }
        exec.durationMs = runDuration.intValue();
        exec.syncedAt = LocalDateTime.now();

        workflowRepo.save(exec);
        synced++;

        // Sincronizar jobs del workflow run
        int jobCount = syncJobsForRun(runId);
        jobsSynced += jobCount;
      }

      log.info("GitHub Actions sync completado: {} workflows, {} jobs", synced, jobsSynced);
      return Map.of("synced", synced, "jobsSynced", jobsSynced, "message", "Sincronizacion exitosa");

    } catch (Exception e) {
      log.error("Error al sincronizar workflow runs de GitHub: {}", e.getMessage());
      return Map.of("synced", 0, "message", "Error: " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private int syncJobsForRun(Long runId) {
    String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs/" + runId + "/jobs";
    int count = 0;
    try {
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
      List<Map<String, Object>> jobs = (List<Map<String, Object>>) response.getBody().get("jobs");
      if (jobs == null) return 0;

      for (Map<String, Object> job : jobs) {
        Long jobId = Long.valueOf(String.valueOf(job.get("id")));
        PipelineJob pj = jobRepo.findByGithubRunId(runId).stream()
            .filter(j -> j.githubJobId.equals(jobId))
            .findFirst()
            .orElse(new PipelineJob());
        pj.githubJobId = jobId;
        pj.githubRunId = runId;
        pj.jobName = String.valueOf(job.getOrDefault("name", ""));
        pj.status = String.valueOf(job.getOrDefault("status", ""));
        pj.conclusion = String.valueOf(job.getOrDefault("conclusion", ""));
        pj.logsUrl = String.valueOf(job.getOrDefault("html_url", ""));
        pj.startedAt = parseDate(String.valueOf(job.getOrDefault("started_at", "")));
        pj.completedAt = parseDate(String.valueOf(job.getOrDefault("completed_at", "")));
        Long dur = job.get("duration_ms") != null ? Long.valueOf(String.valueOf(job.get("duration_ms"))) : 0L;
        pj.durationMs = dur.intValue();
        pj.syncedAt = LocalDateTime.now();
        jobRepo.save(pj);
        count++;
      }
    } catch (Exception e) {
      log.warn("Error al sincronizar jobs para run {}: {}", runId, e.getMessage());
    }
    return count;
  }

  private LocalDateTime parseDate(String iso) {
    if (iso == null || iso.isBlank() || "null".equals(iso)) return null;
    try {
      return LocalDateTime.parse(iso.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (Exception e) {
      return null;
    }
  }

  public List<WorkflowExecution> latestWorkflows() {
    return workflowRepo.findTop30ByOrderBySyncedAtDesc();
  }

  public List<PipelineJob> jobsForRun(Long runId) {
    return jobRepo.findByGithubRunId(runId);
  }

  public Map<String, Object> summary() {
    long total = workflowRepo.count();
    long success = workflowRepo.countByConclusion("success");
    long failure = workflowRepo.countByConclusion("failure");
    return Map.of(
        "total", total,
        "success", success,
        "failure", failure,
        "passRate", total > 0 ? Math.round((double) success / total * 100) : 0,
        "githubEnabled", isEnabled());
  }
}
