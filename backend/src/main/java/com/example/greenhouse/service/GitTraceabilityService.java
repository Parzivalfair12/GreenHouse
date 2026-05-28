package com.example.greenhouse.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitTraceabilityService {

  private static final Logger log = LoggerFactory.getLogger(GitTraceabilityService.class);
  private static final Pattern US_PATTERN = Pattern.compile("US#(\\d+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern CONVENTIONAL_COMMIT = Pattern.compile("^(\\w+)(\\([^)]+\\))?:\\s*(.*)$");

  private File findGitRoot() {
    File dir = new File(".").getAbsoluteFile();
    for (File d = dir; d != null; d = d.getParentFile()) {
      if (new File(d, ".git").exists()) return d;
    }
    return dir;
  }

  public List<Map<String, Object>> getCommitHistory(int maxCount) {
    List<Map<String, Object>> commits = new ArrayList<>();
    File gitRoot = findGitRoot();
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "git", "log", "--oneline", "--decorate=short",
          "-" + maxCount, "--format=%H|%an|%ae|%ai|%s|%D");
      pb.directory(gitRoot);
      Process process = pb.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split("\\|", 6);
          if (parts.length >= 5) {
            List<String> usRefs = extractUserStoryRefs(parts[4]);
            Map<String, String> parsed = parseCommitMessage(parts[4]);
            Map<String, Object> commit = new HashMap<>();
            commit.put("hash", parts[0]);
            commit.put("author", parts[1]);
            commit.put("email", parts[2]);
            commit.put("date", parts[3]);
            commit.put("message", parts[4]);
            commit.put("type", parsed.getOrDefault("type", "other"));
            commit.put("scope", parsed.getOrDefault("scope", ""));
            commit.put("description", parsed.getOrDefault("description", parts[4]));
            commit.put("userStories", usRefs);
            if (parts.length >= 6 && !parts[5].isEmpty()) {
              commit.put("refs", parts[5]);
            }
            commits.add(commit);
          }
        }
      }
      process.waitFor();
    } catch (Exception e) {
      log.warn("No se pudo leer el historial git desde {}: {}", gitRoot, e.getMessage());
    }
    return commits;
  }

  public List<Map<String, Object>> getFullHistory() {
    return getCommitHistory(50);
  }

  public List<String> extractUserStoryRefs(String message) {
    List<String> refs = new ArrayList<>();
    Matcher matcher = US_PATTERN.matcher(message);
    while (matcher.find()) {
      refs.add("US#" + matcher.group(1));
    }
    return refs;
  }

  public Map<String, String> parseCommitMessage(String message) {
    Map<String, String> result = new HashMap<>();
    if (message == null) {
      result.put("type", "other"); result.put("scope", ""); result.put("description", "");
      return result;
    }
    Matcher matcher = CONVENTIONAL_COMMIT.matcher(message);
    if (matcher.find()) {
      result.put("type", matcher.group(1));
      String scope = matcher.group(2);
      result.put("scope", scope != null ? scope.replace("(", "").replace(")", "") : "");
      result.put("description", matcher.group(3));
    } else {
      result.put("type", "other");
      result.put("scope", "");
      result.put("description", message);
    }
    return result;
  }

  public List<Map<String, Object>> buildTraceabilityMatrix() {
    List<Map<String, Object>> matrix = new ArrayList<>();
    List<Map<String, Object>> commits = getFullHistory();

    for (Map<String, Object> commit : commits) {
      @SuppressWarnings("unchecked")
      List<String> stories = (List<String>) commit.getOrDefault("userStories", List.of());
      if (stories.isEmpty()) {
        Map<String, Object> row = new HashMap<>();
        row.put("commit", commit.get("hash"));
        row.put("message", commit.get("message"));
        row.put("type", commit.get("type"));
        row.put("userStories", "N/A");
        row.put("author", commit.get("author"));
        row.put("date", commit.get("date"));
        matrix.add(row);
      } else {
        for (String story : stories) {
          Map<String, Object> row = new HashMap<>();
          row.put("commit", commit.get("hash"));
          row.put("message", commit.get("message"));
          row.put("type", commit.get("type"));
          row.put("userStories", story);
          row.put("author", commit.get("author"));
          row.put("date", commit.get("date"));
          matrix.add(row);
        }
      }
    }
    return matrix;
  }

  public Map<String, Object> stats() {
    List<Map<String, Object>> commits = getFullHistory();
    int total = commits.size();
    int withUS = 0;
    int withoutUS = 0;
    Map<String, Integer> typeCount = new HashMap<>();
    for (Map<String, Object> c : commits) {
      @SuppressWarnings("unchecked")
      List<String> stories = (List<String>) c.getOrDefault("userStories", List.of());
      if (stories.isEmpty()) withoutUS++;
      else withUS++;
      String type = (String) c.getOrDefault("type", "other");
      typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
    }
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalCommits", total);
    stats.put("withUserStories", withUS);
    stats.put("withoutUserStories", withoutUS);
    stats.put("types", typeCount);
    return stats;
  }
}
