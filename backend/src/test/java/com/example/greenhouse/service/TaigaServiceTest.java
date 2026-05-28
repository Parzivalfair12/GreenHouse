package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.greenhouse.config.TaigaConfig;
import com.example.greenhouse.web.dto.TaigaStatusRequest;
import com.example.greenhouse.web.dto.TaigaSyncResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class TaigaServiceTest {

  TaigaConfig config;
  RestTemplate rest;
  TaigaService service;

  @BeforeEach
  void setUp() {
    config = mock(TaigaConfig.class);
    rest = mock(RestTemplate.class);
    when(config.isEnabled()).thenReturn(true);
    when(config.getApiUrl()).thenReturn("https://api.taiga.io/api/v1");
    when(config.getProjectId()).thenReturn(123);
    when(config.getToken()).thenReturn("test-token");
    when(config.getRestTemplate()).thenReturn(rest);
    service = new TaigaService(config);
  }

  @Test
  void listStories_whenDisabled_returnsEmpty() {
    when(config.isEnabled()).thenReturn(false);
    assertTrue(service.listStories().isEmpty());
  }

  @Test
  void listStories_whenEnabled_returnsStories() {
    @SuppressWarnings("unchecked")
    ResponseEntity<List<Map<String, Object>>> resp = mock(ResponseEntity.class);
    when(resp.getBody()).thenReturn(List.of(Map.of("id", 1, "subject", "Test")));
    when(rest.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(resp);

    List<Map<String, Object>> stories = service.listStories();
    assertEquals(1, stories.size());
    assertEquals("Test", stories.get(0).get("subject"));
  }

  @Test
  void summary_whenDisabled_returnsZeroMetrics() {
    when(config.isEnabled()).thenReturn(false);
    Map<String, Object> summary = service.summary();
    assertEquals(0, summary.get("totalStories"));
    assertEquals(false, summary.get("taigaEnabled"));
  }

  @Test
  void summary_whenEnabled_countsCompleted() {
    @SuppressWarnings("unchecked")
    ResponseEntity<List<Map<String, Object>>> resp = mock(ResponseEntity.class);
    when(resp.getBody()).thenReturn(List.of(
        Map.of("id", 1, "status", Map.of("name", "done")),
        Map.of("id", 2, "status", Map.of("name", "new"))
    ));
    when(rest.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(resp);

    Map<String, Object> summary = service.summary();
    assertEquals(2L, summary.get("totalStories"));
    assertEquals(1L, summary.get("completedStories"));
    assertEquals(50L, summary.get("completionPercent"));
    assertEquals(true, summary.get("taigaEnabled"));
  }

  @Test
  void syncStories_whenDisabled_returnsErrorResponse() {
    when(config.isEnabled()).thenReturn(false);
    TaigaSyncResponse result = service.syncStories(List.of(Map.of("subject", "Test")));
    assertFalse(result.success());
    assertEquals("TAIGA_DISABLED", result.errors().get(0));
  }

  @Test
  void isEnabled_delegatesToConfig() {
    assertTrue(service.isEnabled());
    when(config.isEnabled()).thenReturn(false);
    assertFalse(service.isEnabled());
  }
}
