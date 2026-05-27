package com.example.greenhouse.service;

import com.example.greenhouse.domain.ActionOrigin;
import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.AuditLog;
import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.repository.AuditLogRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for operational audit logging with user and greenhouse tracking. */
@Service
public class AuditLogService {
  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

  private final AuditLogRepository repository;

  public AuditLogService(AuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void log(String action, String detail, ActionOrigin origin) {
    log(action, detail, origin, null, null);
  }

  @Transactional
  public void log(String action, String detail, ActionOrigin origin, Greenhouse greenhouse, AppUser user) {
    AuditLog entry = new AuditLog();
    entry.action = action;
    entry.detail = detail;
    entry.origin = origin;
    entry.greenhouse = greenhouse;
    entry.user = user;
    repository.save(entry);
    log.debug("AuditLog: {} - {} (origin={})", action, detail, origin);
  }

  @Transactional
  public void logLogin(AppUser user) {
    log("Login", "Usuario autenticado: " + user.email, ActionOrigin.MANUAL, null, user);
  }

  @Transactional
  public void logCreate(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Crear " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  @Transactional
  public void logUpdate(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Actualizar " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  @Transactional
  public void logDelete(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Eliminar " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  @Transactional
  public void logRuleActivation(AutomationRule rule, String detail, Greenhouse greenhouse) {
    log("Activar regla", detail, ActionOrigin.AUTOMATIC, greenhouse, null);
  }

  @Transactional
  public void logActuatorActivation(String actuatorName, String detail, Greenhouse greenhouse) {
    log("Activar actuador", actuatorName + ": " + detail, ActionOrigin.AUTOMATIC, greenhouse, null);
  }

  @Transactional
  public void logAlertResolution(String alertMessage, Greenhouse greenhouse, AppUser user) {
    log("Resolver alerta", alertMessage, ActionOrigin.MANUAL, greenhouse, user);
  }

  @Transactional(readOnly = true)
  public List<AuditLog> latest() {
    return repository.findTop100ByOrderByCreatedAtDesc();
  }
}
