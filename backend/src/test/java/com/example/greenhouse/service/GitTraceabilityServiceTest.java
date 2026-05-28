package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GitTraceabilityServiceTest {

  GitTraceabilityService service = new GitTraceabilityService();

  @Test
  void extractUserStoryRefs_findsUSRefs() {
    List<String> refs = service.extractUserStoryRefs("feat(auth): US#1 login con Google");
    assertEquals(List.of("US#1"), refs);
  }

  @Test
  void extractUserStoryRefs_caseInsensitive() {
    List<String> refs = service.extractUserStoryRefs("fix: us#42 correccion");
    assertEquals(List.of("US#42"), refs);
  }

  @Test
  void extractUserStoryRefs_noRefs_returnsEmpty() {
    assertTrue(service.extractUserStoryRefs("feat: algo sin referencia").isEmpty());
  }

  @Test
  void parseCommitMessage_conventionalCommit() {
    Map<String, String> parsed = service.parseCommitMessage("feat(auth): login con Google");
    assertEquals("feat", parsed.get("type"));
    assertEquals("auth", parsed.get("scope"));
    assertEquals("login con Google", parsed.get("description"));
  }

  @Test
  void parseCommitMessage_noScope() {
    Map<String, String> parsed = service.parseCommitMessage("docs: actualizar README");
    assertEquals("docs", parsed.get("type"));
    assertEquals("", parsed.get("scope"));
    assertEquals("actualizar README", parsed.get("description"));
  }

  @Test
  void parseCommitMessage_nonConventional() {
    Map<String, String> parsed = service.parseCommitMessage("random message");
    assertEquals("other", parsed.get("type"));
    assertEquals("random message", parsed.get("description"));
  }

  @Test
  void buildTraceabilityMatrix_returnsList() {
    List<Map<String, Object>> matrix = service.buildTraceabilityMatrix();
    assertNotNull(matrix);
    // El resultado depende del historial git real del entorno
  }

  @Test
  void stats_returnsMetrics() {
    Map<String, Object> stats = service.stats();
    assertNotNull(stats);
    assertTrue(stats.containsKey("totalCommits"));
    assertTrue(stats.containsKey("withUserStories"));
    assertTrue(stats.containsKey("withoutUserStories"));
    assertTrue(stats.containsKey("types"));
  }
}
