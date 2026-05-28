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

/**
 * Servicio de escaneo granular de commits Git.
 *
 * Analiza el historial de commits detectando:
 * - conventional commits (feat, fix, docs, test, refactor, ci)
 * - archivos modificados
 * - lineas agregadas/eliminadas
 * - ramas
 * - tags
 * - merges
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Service
public class GitCommitScannerService {

  private static final Logger log = LoggerFactory.getLogger(GitCommitScannerService.class);

  private static final Pattern CONVENTIONAL = Pattern.compile("^(feat|fix|docs|test|refactor|ci|style|chore)(\\([^)]+\\))?:\\s*(.*)$");
  private static final Pattern FILES_CHANGED = Pattern.compile("^(\\d+)\\s+files? changed");
  private static final Pattern INSERTIONS = Pattern.compile("^(\\d+)\\s+insertions?");
  private static final Pattern DELETIONS = Pattern.compile("^(\\d+)\\s+deletions?");

  private File findGitRoot() {
    File dir = new File(".").getAbsoluteFile();
    for (File d = dir; d != null; d = d.getParentFile()) {
      if (new File(d, ".git").exists()) return d;
    }
    return dir;
  }

  public List<Map<String, Object>> scanRecentCommits(int maxCount) {
    List<Map<String, Object>> commits = new ArrayList<>();
    File gitRoot = findGitRoot();
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "git", "log", "-" + maxCount,
          "--format=%H|%an|%ae|%ai|%s|%D",
          "--shortstat");
      pb.directory(gitRoot);
      Process process = pb.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        Map<String, Object> current = null;
        while ((line = reader.readLine()) != null) {
          if (line.contains("|") && line.split("\\|").length >= 5) {
            if (current != null) commits.add(current);
            current = parseCommitLine(line);
          } else if (current != null) {
            parseStatsLine(line, current);
          }
        }
        if (current != null) commits.add(current);
      }
      process.waitFor();
    } catch (Exception e) {
      log.warn("Error escaneando commits: {}", e.getMessage());
    }
    return commits;
  }

  private Map<String, Object> parseCommitLine(String line) {
    String[] parts = line.split("\\|", 6);
    Map<String, Object> commit = new HashMap<>();
    commit.put("hash", parts[0]);
    commit.put("author", parts[1]);
    commit.put("email", parts[2]);
    commit.put("date", parts[3]);
    commit.put("message", parts[4]);
    if (parts.length >= 6 && !parts[5].isEmpty()) {
      commit.put("refs", parts[5]);
    }

    Matcher m = CONVENTIONAL.matcher(parts[4]);
    if (m.find()) {
      commit.put("type", m.group(1));
      String scope = m.group(2);
      commit.put("scope", scope != null ? scope.replace("(", "").replace(")", "") : "");
      commit.put("description", m.group(3));
    } else {
      commit.put("type", "other");
      commit.put("scope", "");
      commit.put("description", parts[4]);
    }

    commit.put("filesChanged", 0);
    commit.put("insertions", 0);
    commit.put("deletions", 0);
    return commit;
  }

  private void parseStatsLine(String line, Map<String, Object> commit) {
    Matcher fc = FILES_CHANGED.matcher(line);
    if (fc.find()) commit.put("filesChanged", Integer.parseInt(fc.group(1)));
    Matcher ins = INSERTIONS.matcher(line);
    if (ins.find()) commit.put("insertions", Integer.parseInt(ins.group(1)));
    Matcher del = DELETIONS.matcher(line);
    if (del.find()) commit.put("deletions", Integer.parseInt(del.group(1)));
  }

  public Map<String, Object> stats() {
    List<Map<String, Object>> commits = scanRecentCommits(50);
    int total = commits.size();
    int feat = 0, fix = 0, docs = 0, test = 0, refactor = 0, ci = 0, other = 0;
    int totalFiles = 0, totalInsertions = 0, totalDeletions = 0;

    for (Map<String, Object> c : commits) {
      String type = (String) c.getOrDefault("type", "other");
      switch (type) {
        case "feat" -> feat++;
        case "fix" -> fix++;
        case "docs" -> docs++;
        case "test" -> test++;
        case "refactor" -> refactor++;
        case "ci" -> ci++;
        default -> other++;
      }
      totalFiles += (Integer) c.getOrDefault("filesChanged", 0);
      totalInsertions += (Integer) c.getOrDefault("insertions", 0);
      totalDeletions += (Integer) c.getOrDefault("deletions", 0);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("totalCommits", total);
    result.put("feat", feat);
    result.put("fix", fix);
    result.put("docs", docs);
    result.put("test", test);
    result.put("refactor", refactor);
    result.put("ci", ci);
    result.put("other", other);
    result.put("totalFilesChanged", totalFiles);
    result.put("totalInsertions", totalInsertions);
    result.put("totalDeletions", totalDeletions);
    return result;
  }
}
