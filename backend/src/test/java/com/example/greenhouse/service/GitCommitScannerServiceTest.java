package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GitCommitScannerServiceTest {

  GitCommitScannerService service = new GitCommitScannerService();

  @Test
  void scanRecentCommits_returnsList() {
    List<Map<String, Object>> commits = service.scanRecentCommits(10);
    assertNotNull(commits);
    // El resultado depende del repo git real
  }

  @Test
  void stats_returnsMetrics() {
    Map<String, Object> stats = service.stats();
    assertNotNull(stats);
    assertTrue(stats.containsKey("totalCommits"));
    assertTrue(stats.containsKey("feat"));
    assertTrue(stats.containsKey("fix"));
    assertTrue(stats.containsKey("docs"));
    assertTrue(stats.containsKey("test"));
    assertTrue(stats.containsKey("refactor"));
    assertTrue(stats.containsKey("ci"));
    assertTrue(stats.containsKey("other"));
    assertTrue(stats.containsKey("totalFilesChanged"));
    assertTrue(stats.containsKey("totalInsertions"));
    assertTrue(stats.containsKey("totalDeletions"));
  }
}
