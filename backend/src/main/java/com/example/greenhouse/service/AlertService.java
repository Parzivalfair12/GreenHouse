package com.example.greenhouse.service;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.repository.AlertRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de operaciones del ciclo de vida de alertas — listar incidentes
 * abiertos y resolverlos.
 *
 * Las alertas son creadas por el {@link RuleEngineService} cuando las lecturas
 * de sensores superan los umbrales o las reglas de automatización se activan.
 * Este servicio maneja el lado de lectura y el flujo de resolución consumido
 * por el dashboard y el panel de alertas.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class AlertService {
  private static final Logger log = LoggerFactory.getLogger(AlertService.class);

  private final AlertRepository repository;

  public AlertService(AlertRepository repository) {
    this.repository = repository;
  }

  /**
   * Retrieves all unresolved alerts ordered by creation time descending
   * (most recent first).
   *
   * @return list of open alerts, never null
   * @since 2.1.0
   */
  @Transactional(readOnly = true)
  public List<Alert> findOpenAlerts() {
    return repository.findByResolvedFalseOrderByCreatedAtDesc();
  }

  /**
   * Marks a specific alert as resolved.
   *
   * Once resolved the alert no longer appears in the open-alerts list
   * but remains in the database for audit trail purposes.
   *
   * @param id the alert primary key
   * @throws IllegalArgumentException if no alert exists with the given id
   * @since 2.1.0
   */
  @Transactional
  public void resolve(long id) {
    Alert alert = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Alert not found"));
    alert.resolved = true;
    repository.save(alert);
    log.info("Alert {} resolved", id);
  }
}
