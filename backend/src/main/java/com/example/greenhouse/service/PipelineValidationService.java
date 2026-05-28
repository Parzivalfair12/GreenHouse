package com.example.greenhouse.service;

import com.example.greenhouse.config.TaigaConfig;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de validacion de pipelines que detecta fallos
 * y crea issues automaticos en Taiga cuando algo falla.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Service
public class PipelineValidationService {

  private static final Logger log = LoggerFactory.getLogger(PipelineValidationService.class);

  private final TaigaService taigaService;
  private final AuditTrailService auditTrail;
  private final TaigaConfig taigaConfig;

  public PipelineValidationService(TaigaService taigaService, AuditTrailService auditTrail, TaigaConfig taigaConfig) {
    this.taigaService = taigaService;
    this.auditTrail = auditTrail;
    this.taigaConfig = taigaConfig;
  }

  /**
   * Valida un pipeline run y crea issue en Taiga si fallo.
   *
   * @param runId      ID del workflow run
   * @param jobName    nombre del job
   * @param conclusion conclusion del job (success, failure, cancelled)
   * @param errorMsg   mensaje de error si aplica
   * @param branch     rama
   * @param commitSha  SHA del commit
   * @return resultado de la validacion
   */
  public Map<String, Object> validatePipelineRun(Long runId, String jobName, String conclusion,
      String errorMsg, String branch, String commitSha) {

    boolean passed = "success".equalsIgnoreCase(conclusion);
    String correlationId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);

    auditTrail.recordValidation(
        "Pipeline " + jobName + " run " + runId,
        passed, correlationId, errorMsg);

    if (!passed) {
      String issueSubject = "[AUTO] Fallo pipeline: " + jobName + " en " + branch;
      String issueDesc = buildIssueDescription(runId, jobName, conclusion, errorMsg, branch, commitSha);

      if (taigaConfig.isEnabled()) {
        Map<String, Object> issue = createTaigaIssue(issueSubject, issueDesc);
        if (issue != null && !issue.isEmpty()) {
          Integer issueId = (Integer) issue.get("id");
          auditTrail.recordIssue(issueId, issueSubject, "OPEN", correlationId, errorMsg);
          log.warn("Issue automatico creado en Taiga: id={} para fallo pipeline {}", issueId, runId);
        }
      } else {
        log.warn("Taiga no configurado — issue no creado para fallo pipeline {}", runId);
      }
    }

    return Map.of(
        "passed", passed,
        "runId", runId,
        "jobName", jobName,
        "correlationId", correlationId,
        "issueCreated", !passed && taigaConfig.isEnabled());
  }

  private String buildIssueDescription(Long runId, String jobName, String conclusion,
      String errorMsg, String branch, String commitSha) {
    StringBuilder sb = new StringBuilder();
    sb.append("## Fallo de Pipeline Detectado Automaticamente\n\n");
    sb.append("- **Workflow Run:** ").append(runId).append("\n");
    sb.append("- **Job:** ").append(jobName).append("\n");
    sb.append("- **Conclusion:** ").append(conclusion).append("\n");
    sb.append("- **Branch:** ").append(branch).append("\n");
    sb.append("- **Commit:** `").append(commitSha).append("`\n");
    if (errorMsg != null && !errorMsg.isBlank()) {
      sb.append("- **Error:** ").append(errorMsg).append("\n");
    }
    sb.append("\n---\n");
    sb.append("*Este issue fue creado automaticamente por el sistema de validacion de pipelines.*");
    return sb.toString();
  }

  private Map<String, Object> createTaigaIssue(String subject, String description) {
    try {
      Map<String, Object> body = new java.util.HashMap<>();
      body.put("project", taigaConfig.getProjectId());
      body.put("subject", subject);
      body.put("description", description);
      body.put("type", 1); // Bug type en Taiga
      body.put("severity", 2); // Alta
      body.put("priority", 2); // Alta
      return taigaService.createStory(body);
    } catch (Exception e) {
      log.error("Error creando issue en Taiga: {}", e.getMessage());
      return Map.of();
    }
  }
}
