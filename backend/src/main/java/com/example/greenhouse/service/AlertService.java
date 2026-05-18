package com.example.greenhouse.service;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.repository.AlertRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business operations for alerts and incident resolution. */
@Service
public class AlertService {
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
  }
}
