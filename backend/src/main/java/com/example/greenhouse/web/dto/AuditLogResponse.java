package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.AuditLog;
import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String action, String detail, String origin, LocalDateTime createdAt) {
  public static AuditLogResponse from(AuditLog log) {
    return new AuditLogResponse(log.id, log.action, log.detail, log.origin.name(), log.createdAt);
  }
}
