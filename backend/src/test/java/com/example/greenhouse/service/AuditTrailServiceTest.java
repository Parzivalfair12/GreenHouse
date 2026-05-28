package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.greenhouse.domain.AuditTrail;
import com.example.greenhouse.repository.AuditTrailRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditTrailServiceTest {

  AuditTrailRepository repo;
  AuditTrailService service;

  @BeforeEach
  void setUp() {
    repo = mock(AuditTrailRepository.class);
    service = new AuditTrailService(repo);
  }

  @Test
  void recordCommit_savesEntry() {
    service.recordCommit("abc123", "feat: test", "Dev", "main", "corr-1");
    verify(repo).save(any(AuditTrail.class));
  }

  @Test
  void recordPipeline_savesEntry() {
    service.recordPipeline(123L, "CI", "completed", "success", "Dev", "main", "corr-2", 5000);
    verify(repo).save(any(AuditTrail.class));
  }

  @Test
  void recordStory_savesEntry() {
    service.recordStory(99, "US test", "DONE", "corr-3");
    verify(repo).save(any(AuditTrail.class));
  }

  @Test
  void recordIssue_savesEntry() {
    service.recordIssue(42, "Bug", "OPEN", "corr-4", "error msg");
    verify(repo).save(any(AuditTrail.class));
  }

  @Test
  void recordValidation_passed() {
    service.recordValidation("Test val", true, "corr-5", null);
    verify(repo).save(argThat(e -> "SUCCESS".equals(e.status)));
  }

  @Test
  void recordValidation_failed() {
    service.recordValidation("Test val", false, "corr-6", "fail");
    verify(repo).save(argThat(e -> "FAILURE".equals(e.status)));
  }

  @Test
  void recordSync_success() {
    service.recordSync("Taiga", 5, "corr-7", null);
    verify(repo).save(argThat(e -> "SUCCESS".equals(e.status)));
  }

  @Test
  void recordSync_failure() {
    service.recordSync("Taiga", 0, "corr-8", "error");
    verify(repo).save(argThat(e -> "FAILURE".equals(e.status)));
  }

  @Test
  void summary_withData() {
    when(repo.count()).thenReturn(10L);
    when(repo.countByEventType("COMMIT")).thenReturn(3L);
    when(repo.countByEventType("PIPELINE")).thenReturn(4L);
    when(repo.countByEventType("STORY")).thenReturn(2L);
    when(repo.countByEventType("ISSUE")).thenReturn(1L);
    when(repo.countByEventType("VALIDATION")).thenReturn(0L);
    when(repo.countByStatus("SUCCESS")).thenReturn(8L);
    when(repo.countByStatus("FAILURE")).thenReturn(2L);

    Map<String, Object> summary = service.summary();
    assertEquals(10L, summary.get("totalEvents"));
    assertEquals(3L, summary.get("commits"));
    assertEquals(4L, summary.get("pipelines"));
    assertEquals(2L, summary.get("stories"));
    assertEquals(1L, summary.get("issues"));
    assertEquals(0L, summary.get("validations"));
    assertEquals(8L, summary.get("successCount"));
    assertEquals(2L, summary.get("failureCount"));
    assertEquals(80L, summary.get("passRate"));
  }

  @Test
  void summary_withNoData() {
    when(repo.count()).thenReturn(0L);
    Map<String, Object> summary = service.summary();
    assertEquals(0L, summary.get("totalEvents"));
    assertEquals(0L, summary.get("passRate"));
  }

  @Test
  void latest_delegatesToRepo() {
    when(repo.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of());
    assertTrue(service.latest().isEmpty());
  }

  @Test
  void byEventType_delegatesToRepo() {
    when(repo.findByEventTypeOrderByCreatedAtDesc("COMMIT")).thenReturn(List.of());
    assertTrue(service.byEventType("COMMIT").isEmpty());
  }
}
