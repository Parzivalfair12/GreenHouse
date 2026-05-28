package com.example.greenhouse.service;

import com.example.greenhouse.domain.PipelineExecution;
import com.example.greenhouse.repository.PipelineExecutionRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineAuditService {

  private static final Logger log = LoggerFactory.getLogger(PipelineAuditService.class);

  private final PipelineExecutionRepository repository;

  public PipelineAuditService(PipelineExecutionRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void recordBuild(PipelineExecution exec) {
    repository.save(exec);
    log.info("Pipeline registrado: job={}, status={}, duration={}ms", exec.jobName, exec.status, exec.durationMs);
  }

  @Transactional(readOnly = true)
  public List<PipelineExecution> latest() {
    return repository.findTop50ByOrderByCreatedAtDesc();
  }

  @Transactional(readOnly = true)
  public Map<String, Object> summary() {
    long total = repository.count();
    long passed = repository.countByStatus("SUCCESS");
    long failed = repository.countByStatus("FAILURE");
    long running = repository.countByStatus("RUNNING");
    return Map.of(
        "total", total,
        "passed", passed,
        "failed", failed,
        "running", running,
        "passRate", total > 0 ? Math.round((double) passed / total * 100) : 0);
  }
}
