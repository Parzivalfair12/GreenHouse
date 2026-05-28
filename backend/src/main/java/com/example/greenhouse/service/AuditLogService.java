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

/**
 * Servicio de bitácora de auditoría de solo-escritura que registra cada
 * operación de negocio — manual y automática — con contexto de usuario
 * e invernadero.
 *
 * Las entradas de auditoría son append-only; nunca se actualizan ni eliminan.
 * El servicio expone las 100 entradas más recientes para el panel de
 * bitácora de la UI.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class AuditLogService {
  private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

  private final AuditLogRepository repository;

  public AuditLogService(AuditLogRepository repository) {
    this.repository = repository;
  }

  /**
   * Core log method accepting action description, detail, origin and
   * optional greenhouse/user context.
   *
   * @param action      short action label (e.g. "Login", "Crear sensor")
   * @param detail      human-readable description of what changed
   * @param origin      {@link ActionOrigin#MANUAL} or {@link ActionOrigin#AUTOMATIC}
   * @param greenhouse  optional parent greenhouse (nullable)
   * @param user        optional authenticated user (nullable for automatic actions)
   * @since 2.1.0
   */
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

  /**
   * Convenience log with only action, detail and origin (no entity context).
   */
  @Transactional
  public void log(String action, String detail, ActionOrigin origin) {
    log(action, detail, origin, null, null);
  }

  /**
   * Records a successful user login event.
   */
  @Transactional
  public void logLogin(AppUser user) {
    log("Login", "Usuario autenticado: " + user.email, ActionOrigin.MANUAL, null, user);
  }

  /** Records entity creation. */
  @Transactional
  public void logCreate(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Crear " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  /** Records entity update. */
  @Transactional
  public void logUpdate(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Actualizar " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  /** Records entity deletion. */
  @Transactional
  public void logDelete(String entity, String identifier, Greenhouse greenhouse, AppUser user) {
    log("Eliminar " + entity, identifier, ActionOrigin.MANUAL, greenhouse, user);
  }

  /** Records an automation rule activation. */
  @Transactional
  public void logRuleActivation(AutomationRule rule, String detail, Greenhouse greenhouse) {
    log("Activar regla", detail, ActionOrigin.AUTOMATIC, greenhouse, null);
  }

  /** Records an actuator state change. */
  @Transactional
  public void logActuatorActivation(String actuatorName, String detail, Greenhouse greenhouse) {
    log("Activar actuador", actuatorName + ": " + detail, ActionOrigin.AUTOMATIC, greenhouse, null);
  }

  /** Records an alert resolution by a user. */
  @Transactional
  public void logAlertResolution(String alertMessage, Greenhouse greenhouse, AppUser user) {
    log("Resolver alerta", alertMessage, ActionOrigin.MANUAL, greenhouse, user);
  }

  /**
   * Returns the 100 most recent audit log entries ordered by creation time
   * descending.
   *
   * @return latest 100 entries
   * @since 2.1.0
   */
  @Transactional(readOnly = true)
  public List<AuditLog> latest() {
    return repository.findTop100ByOrderByCreatedAtDesc();
  }
}
