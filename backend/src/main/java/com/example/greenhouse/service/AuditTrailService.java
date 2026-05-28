package com.example.greenhouse.service;

import com.example.greenhouse.domain.AuditTrail;
import com.example.greenhouse.repository.AuditTrailRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de auditoria trazable que persiste cada evento del ciclo de vida DevOps.
 *
 * Registra: commits, pipelines, historias, issues, releases, syncs, validaciones.
 * Todos los eventos incluyen correlationId para trazabilidad end-to-end.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Service
public class AuditTrailService {

  private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);

  private final AuditTrailRepository repository;

  public AuditTrailService(AuditTrailRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void record(String eventType, String externalId, String description,
      String actor, String branch, String status, String sourceService,
      String correlationId, Integer durationMs, String metadata, String errorMessage) {
    AuditTrail entry = new AuditTrail();
    entry.eventType = eventType;
    entry.externalId = externalId;
    entry.description = description;
    entry.actor = actor;
    entry.branch = branch;
    entry.status = status;
    entry.sourceService = sourceService;
    entry.correlationId = correlationId;
    entry.durationMs = durationMs;
    entry.metadata = metadata;
    entry.errorMessage = errorMessage;
    repository.save(entry);
    log.info("AuditTrail: {} {} — {} (status={}, service={})",
        eventType, externalId, description, status, sourceService);
  }

  @Transactional
  public void recordCommit(String sha, String message, String author, String branch, String correlationId) {
    record("COMMIT", sha, message, author, branch, "SUCCESS", "GitTraceability",
        correlationId, null, null, null);
  }

  @Transactional
  public void recordPipeline(Long runId, String workflowName, String status, String conclusion,
      String actor, String branch, String correlationId, int durationMs) {
    record("PIPELINE", String.valueOf(runId), workflowName, actor, branch,
        conclusion != null ? conclusion : status, "GitHubActions",
        correlationId, durationMs, null, null);
  }

  @Transactional
  public void recordStory(Integer storyId, String subject, String status, String correlationId) {
    record("STORY", String.valueOf(storyId), subject, null, null, status, "Taiga",
        correlationId, null, null, null);
  }

  @Transactional
  public void recordIssue(Integer issueId, String subject, String status, String correlationId, String errorMessage) {
    record("ISSUE", String.valueOf(issueId), subject, null, null, status, "Taiga",
        correlationId, null, null, errorMessage);
  }

  @Transactional
  public void recordValidation(String description, boolean passed, String correlationId, String errorMessage) {
    record("VALIDATION", null, description, null, null,
        passed ? "SUCCESS" : "FAILURE", "PipelineValidation",
        correlationId, null, null, errorMessage);
  }

  @Transactional
  public void recordSync(String service, int itemsSynced, String correlationId, String errorMessage) {
    record("SYNC", null, service + " sync: " + itemsSynced + " items", null, null,
        errorMessage == null ? "SUCCESS" : "FAILURE", service,
        correlationId, null, null, errorMessage);
  }

  @Transactional(readOnly = true)
  public List<AuditTrail> latest() {
    return repository.findTop50ByOrderByCreatedAtDesc();
  }

  @Transactional(readOnly = true)
  public List<AuditTrail> byEventType(String eventType) {
    return repository.findByEventTypeOrderByCreatedAtDesc(eventType);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> summary() {
    long total = repository.count();
    long commits = repository.countByEventType("COMMIT");
    long pipelines = repository.countByEventType("PIPELINE");
    long stories = repository.countByEventType("STORY");
    long issues = repository.countByEventType("ISSUE");
    long validations = repository.countByEventType("VALIDATION");
    long success = repository.countByStatus("SUCCESS");
    long failure = repository.countByStatus("FAILURE");
    Map<String, Object> result = new HashMap<>();
    result.put("totalEvents", total);
    result.put("commits", commits);
    result.put("pipelines", pipelines);
    result.put("stories", stories);
    result.put("issues", issues);
    result.put("validations", validations);
    result.put("successCount", success);
    result.put("failureCount", failure);
    result.put("passRate", total > 0 ? Math.round((double) success / total * 100) : 0);
    return result;
  }
}
