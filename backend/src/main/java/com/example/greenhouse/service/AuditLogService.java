package com.example.greenhouse.service;

import com.example.greenhouse.domain.ActionOrigin;
import com.example.greenhouse.domain.AuditLog;
import com.example.greenhouse.repository.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {
  private final AuditLogRepository repository;

  public AuditLogService(AuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void log(String action, String detail, ActionOrigin origin) {
    AuditLog log = new AuditLog();
    log.action = action;
    log.detail = detail;
    log.origin = origin;
    repository.save(log);
  }

  @Transactional(readOnly = true)
  public List<AuditLog> latest() {
    return repository.findTop100ByOrderByCreatedAtDesc();
  }
}
