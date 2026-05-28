package com.example.greenhouse.web;

import com.example.greenhouse.service.AuditTrailService;
import com.example.greenhouse.service.GitCommitScannerService;
import com.example.greenhouse.service.GitHubActionsService;
import com.example.greenhouse.service.PipelineAuditService;
import com.example.greenhouse.service.PipelineValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DevOps", description = "Panel de control DevOps — pipelines reales de GitHub Actions, auditoria, trazabilidad y validacion")
@RestController
@RequestMapping("/api/devops")
public class DevOpsController {

  private final PipelineAuditService pipelineAudit;
  private final GitHubActionsService githubActions;
  private final AuditTrailService auditTrail;
  private final GitCommitScannerService commitScanner;
  private final PipelineValidationService pipelineValidation;

  public DevOpsController(PipelineAuditService pipelineAudit, GitHubActionsService githubActions,
      AuditTrailService auditTrail, GitCommitScannerService commitScanner,
      PipelineValidationService pipelineValidation) {
    this.pipelineAudit = pipelineAudit;
    this.githubActions = githubActions;
    this.auditTrail = auditTrail;
    this.commitScanner = commitScanner;
    this.pipelineValidation = pipelineValidation;
  }

  @Operation(summary = "Resumen DevOps completo", description = "Estadisticas globales: GitHub Actions, auditoria local, commits y trazabilidad")
  @GetMapping("/summary")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> summary() {
    return Map.of(
        "github", githubActions.summary(),
        "local", pipelineAudit.summary(),
        "audit", auditTrail.summary(),
        "commits", commitScanner.stats(),
        "githubEnabled", githubActions.isEnabled());
  }

  @Operation(summary = "Workflows de GitHub Actions", description = "Ultimos 30 workflow runs sincronizados desde GitHub Actions API")
  @GetMapping("/workflows")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> workflows() {
    return githubActions.latestWorkflows().stream()
        .map(w -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("id", w.id);
          m.put("githubRunId", w.githubRunId);
          m.put("workflowName", w.workflowName);
          m.put("branch", w.branch);
          m.put("commitSha", w.commitSha);
          m.put("commitMessage", w.commitMessage);
          m.put("actor", w.actor);
          m.put("status", w.status);
          m.put("conclusion", w.conclusion);
          m.put("durationMs", w.durationMs);
          m.put("runUrl", w.runUrl);
          m.put("event", w.event);
          m.put("startedAt", w.startedAt != null ? w.startedAt.toString() : null);
          m.put("completedAt", w.completedAt != null ? w.completedAt.toString() : null);
          m.put("syncedAt", w.syncedAt.toString());
          return m;
        })
        .toList();
  }

  @Operation(summary = "Jobs de un workflow run", description = "Jobs individuales de un workflow run especifico")
  @GetMapping("/workflows/{runId}/jobs")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> jobs(@PathVariable Long runId) {
    return githubActions.jobsForRun(runId).stream()
        .map(j -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("id", j.id);
          m.put("githubJobId", j.githubJobId);
          m.put("jobName", j.jobName);
          m.put("status", j.status);
          m.put("conclusion", j.conclusion);
          m.put("durationMs", j.durationMs);
          m.put("logsUrl", j.logsUrl);
          return m;
        })
        .toList();
  }

  @Operation(summary = "Sincronizar workflows manualmente", description = "Fuerza la sincronizacion inmediata con GitHub Actions API")
  @PostMapping("/sync")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> sync() {
    return githubActions.syncWorkflowRuns();
  }

  @Operation(summary = "Historial local de pipelines", description = "Ultimas 50 ejecuciones del pipeline CI/CD local")
  @GetMapping("/pipelines")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> pipelines() {
    return pipelineAudit.latest().stream()
        .map(p -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("id", p.id);
          m.put("workflowName", p.workflowName);
          m.put("jobName", p.jobName);
          m.put("status", p.status);
          m.put("branch", p.branch);
          m.put("commitHash", p.commitHash);
          m.put("durationMs", p.durationMs);
          m.put("testsPassed", p.testsPassed);
          m.put("testsFailed", p.testsFailed);
          m.put("coveragePercent", p.coveragePercent);
          m.put("errorMessage", p.errorMessage);
          m.put("createdAt", p.createdAt != null ? p.createdAt.toString() : null);
          return m;
        })
        .toList();
  }

  @Operation(summary = "Auditoria DevOps", description = "Trail de auditoria completo: commits, pipelines, historias, issues, validaciones")
  @GetMapping("/audit")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> audit() {
    return auditTrail.latest().stream()
        .map(a -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("id", a.id);
          m.put("eventType", a.eventType);
          m.put("externalId", a.externalId);
          m.put("description", a.description);
          m.put("actor", a.actor);
          m.put("branch", a.branch);
          m.put("status", a.status);
          m.put("sourceService", a.sourceService);
          m.put("correlationId", a.correlationId);
          m.put("durationMs", a.durationMs);
          m.put("metadata", a.metadata);
          m.put("errorMessage", a.errorMessage);
          m.put("createdAt", a.createdAt.toString());
          return m;
        })
        .toList();
  }

  @Operation(summary = "Resumen de auditoria", description = "Metricas agregadas de la auditoria DevOps")
  @GetMapping("/audit/summary")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> auditSummary() {
    return auditTrail.summary();
  }

  @Operation(summary = "Commits recientes", description = "Analisis granular de commits con estadisticas de cambios")
  @GetMapping("/commits")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> commits(@RequestParam(defaultValue = "20") int limit) {
    return Map.of(
        "commits", commitScanner.scanRecentCommits(limit),
        "stats", commitScanner.stats());
  }

  @Operation(summary = "Validar pipeline run", description = "Valida un pipeline run y crea issue en Taiga si fallo")
  @PostMapping("/validate")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> validatePipeline(
      @RequestParam Long runId,
      @RequestParam String jobName,
      @RequestParam String conclusion,
      @RequestParam(required = false) String errorMsg,
      @RequestParam String branch,
      @RequestParam String commitSha) {
    return pipelineValidation.validatePipelineRun(runId, jobName, conclusion, errorMsg, branch, commitSha);
  }
}
