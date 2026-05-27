package com.example.greenhouse.service;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.repository.AlertRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business operations for alerts and incident resolution. */
@Service
public class AlertService {
  private static final Logger log = LoggerFactory.getLogger(AlertService.class);

  private final AlertRepository repository;

  public AlertService(AlertRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public List<Alert> findOpenAlerts() {
    return repository.findByResolvedFalseOrderByCreatedAtDesc();
  }

  @Transactional
  public void resolve(long id) {
    Alert alert = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Alert not found"));
    alert.resolved = true;
    repository.save(alert);
    log.info("Alert {} resolved", id);
  }
}
