package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.greenhouse.domain.PipelineExecution;
import com.example.greenhouse.repository.PipelineExecutionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PipelineAuditServiceTest {

  PipelineExecutionRepository repo;
  PipelineAuditService service;

  @BeforeEach
  void setUp() {
    repo = mock(PipelineExecutionRepository.class);
    service = new PipelineAuditService(repo);
  }

  @Test
  void summary_withNoData_returnsZero() {
    when(repo.count()).thenReturn(0L);
    Map<String, Object> summary = service.summary();
    assertEquals(0L, summary.get("total"));
    assertEquals(0L, summary.get("passed"));
    assertEquals(0L, summary.get("failed"));
    assertEquals(0L, summary.get("passRate"));
  }

  @Test
  void summary_withData_calculatesPassRate() {
    when(repo.count()).thenReturn(10L);
    when(repo.countByStatus("SUCCESS")).thenReturn(8L);
    when(repo.countByStatus("FAILURE")).thenReturn(2L);
    when(repo.countByStatus("RUNNING")).thenReturn(0L);

    Map<String, Object> summary = service.summary();
    assertEquals(10L, summary.get("total"));
    assertEquals(8L, summary.get("passed"));
    assertEquals(2L, summary.get("failed"));
    assertEquals(80L, summary.get("passRate"));
  }

  @Test
  void recordBuild_savesExecution() {
    PipelineExecution exec = new PipelineExecution();
    exec.jobName = "test-job";
    exec.status = "SUCCESS";
    service.recordBuild(exec);
    verify(repo).save(exec);
  }

  @Test
  void latest_delegatesToRepository() {
    when(repo.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of());
    assertTrue(service.latest().isEmpty());
  }
}
