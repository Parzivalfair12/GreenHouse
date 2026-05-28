package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.greenhouse.config.TaigaConfig;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PipelineValidationServiceTest {

  TaigaService taigaService;
  AuditTrailService auditTrail;
  TaigaConfig taigaConfig;
  PipelineValidationService service;

  @BeforeEach
  void setUp() {
    taigaService = mock(TaigaService.class);
    auditTrail = mock(AuditTrailService.class);
    taigaConfig = mock(TaigaConfig.class);
    service = new PipelineValidationService(taigaService, auditTrail, taigaConfig);
  }

  @Test
  void validatePipelineRun_success_noIssueCreated() {
    when(taigaConfig.isEnabled()).thenReturn(true);
    Map<String, Object> result = service.validatePipelineRun(1L, "CI", "success", null, "main", "abc");
    assertTrue((Boolean) result.get("passed"));
    assertFalse((Boolean) result.get("issueCreated"));
    verify(auditTrail).recordValidation(anyString(), eq(true), anyString(), eq(null));
    verify(taigaService, never()).createStory(any());
  }

  @Test
  void validatePipelineRun_failure_createsIssue() {
    when(taigaConfig.isEnabled()).thenReturn(true);
    when(taigaService.createStory(any())).thenReturn(Map.of("id", 42));
    Map<String, Object> result = service.validatePipelineRun(1L, "CI", "failure", "test failed", "main", "abc");
    assertFalse((Boolean) result.get("passed"));
    assertTrue((Boolean) result.get("issueCreated"));
    verify(auditTrail).recordValidation(anyString(), eq(false), anyString(), eq("test failed"));
    verify(auditTrail).recordIssue(eq(42), anyString(), eq("OPEN"), anyString(), eq("test failed"));
  }

  @Test
  void validatePipelineRun_failure_taigaDisabled_noIssue() {
    when(taigaConfig.isEnabled()).thenReturn(false);
    Map<String, Object> result = service.validatePipelineRun(1L, "CI", "failure", "err", "main", "abc");
    assertFalse((Boolean) result.get("passed"));
    assertFalse((Boolean) result.get("issueCreated"));
    verify(taigaService, never()).createStory(any());
  }

  @Test
  void validatePipelineRun_cancelled_treatedAsFailure() {
    when(taigaConfig.isEnabled()).thenReturn(true);
    when(taigaService.createStory(any())).thenReturn(Map.of("id", 99));
    Map<String, Object> result = service.validatePipelineRun(1L, "CI", "cancelled", null, "main", "abc");
    assertFalse((Boolean) result.get("passed"));
    assertTrue((Boolean) result.get("issueCreated"));
  }

  @Test
  void validatePipelineRun_containsCorrelationId() {
    when(taigaConfig.isEnabled()).thenReturn(true);
    Map<String, Object> result = service.validatePipelineRun(1L, "CI", "success", null, "main", "abc");
    assertNotNull(result.get("correlationId"));
    assertEquals(16, ((String) result.get("correlationId")).length());
  }
}
