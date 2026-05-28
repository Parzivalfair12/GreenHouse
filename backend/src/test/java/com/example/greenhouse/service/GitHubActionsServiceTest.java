package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.greenhouse.repository.PipelineJobRepository;
import com.example.greenhouse.repository.WorkflowExecutionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class GitHubActionsServiceTest {

  RestTemplate rest;
  WorkflowExecutionRepository workflowRepo;
  PipelineJobRepository jobRepo;
  GitHubActionsService service;

  @BeforeEach
  void setUp() {
    rest = mock(RestTemplate.class);
    workflowRepo = mock(WorkflowExecutionRepository.class);
    jobRepo = mock(PipelineJobRepository.class);
    service = new GitHubActionsService(rest, workflowRepo, jobRepo, "test-owner", "test-repo", "test-token");
  }

  @Test
  void isEnabled_withToken_returnsTrue() {
    assertTrue(service.isEnabled());
  }

  @Test
  void isEnabled_withoutToken_returnsFalse() {
    GitHubActionsService disabled = new GitHubActionsService(rest, workflowRepo, jobRepo, "owner", "repo", "");
    assertFalse(disabled.isEnabled());
  }

  @Test
  void syncWorkflowRuns_whenDisabled_returnsWarning() {
    GitHubActionsService disabled = new GitHubActionsService(rest, workflowRepo, jobRepo, "", "", "");
    Map<String, Object> result = disabled.syncWorkflowRuns();
    assertEquals(0, result.get("synced"));
    assertEquals("GitHub Actions no configurado", result.get("message"));
  }

  @Test
  void syncWorkflowRuns_whenEnabled_syncsData() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Object>> resp = mock(ResponseEntity.class);
    when(resp.getBody()).thenReturn(Map.of("workflow_runs", List.of(
        Map.of("id", 123, "name", "CI", "head_branch", "main", "status", "completed", "conclusion", "success")
    )));
    when(rest.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(resp);

    Map<String, Object> result = service.syncWorkflowRuns();
    assertEquals(1, result.get("synced"));
    verify(workflowRepo).save(any());
  }

  @Test
  void summary_calculatesMetrics() {
    when(workflowRepo.count()).thenReturn(10L);
    when(workflowRepo.countByConclusion("success")).thenReturn(7L);
    when(workflowRepo.countByConclusion("failure")).thenReturn(3L);

    Map<String, Object> summary = service.summary();
    assertEquals(10L, summary.get("total"));
    assertEquals(7L, summary.get("success"));
    assertEquals(3L, summary.get("failure"));
    assertEquals(70L, summary.get("passRate"));
    assertEquals(true, summary.get("githubEnabled"));
  }
}
