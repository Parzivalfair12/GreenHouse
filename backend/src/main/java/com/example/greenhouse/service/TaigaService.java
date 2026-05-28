package com.example.greenhouse.service;

import com.example.greenhouse.config.TaigaConfig;
import com.example.greenhouse.web.dto.TaigaCommentRequest;
import com.example.greenhouse.web.dto.TaigaStatusRequest;
import com.example.greenhouse.web.dto.TaigaSyncResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TaigaService {

  private static final Logger log = LoggerFactory.getLogger(TaigaService.class);

  private final TaigaConfig config;
  private final RestTemplate rest;

  public TaigaService(TaigaConfig config) {
    this.config = config;
    this.rest = config.getRestTemplate();
  }

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    if (config.getToken() != null && !config.getToken().isBlank()) {
      headers.setBearerAuth(config.getToken());
    }
    return headers;
  }

  public boolean isEnabled() {
    return config.isEnabled();
  }

  public List<Map<String, Object>> listStories() {
    if (!config.isEnabled()) return Collections.emptyList();
    try {
      String url = config.getApiUrl() + "/userstories?project=" + config.getProjectId();
      ResponseEntity<List<Map<String, Object>>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      log.info("Taiga: {} historias obtenidas de API real", response.getBody().size());
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al obtener historias de Taiga: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> listEpics() {
    if (!config.isEnabled()) return Collections.emptyList();
    try {
      String url = config.getApiUrl() + "/epics?project=" + config.getProjectId();
      ResponseEntity<List<Map<String, Object>>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al obtener epics de Taiga: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public List<Map<String, Object>> listTasks(int storyId) {
    if (!config.isEnabled()) return Collections.emptyList();
    try {
      String url = config.getApiUrl() + "/tasks?user_story=" + storyId;
      ResponseEntity<List<Map<String, Object>>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al obtener tasks de Taiga: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public Map<String, Object> getStory(int storyId) {
    if (!config.isEnabled()) return null;
    try {
      String url = config.getApiUrl() + "/userstories/" + storyId;
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
          new ParameterizedTypeReference<Map<String, Object>>() {});
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al obtener historia {} de Taiga: {}", storyId, e.getMessage());
      return null;
    }
  }

  public Map<String, Object> createStory(Map<String, Object> storyData) {
    if (!config.isEnabled()) return null;
    try {
      String url = config.getApiUrl() + "/userstories";
      HttpHeaders headers = authHeaders();
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(storyData, headers);
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.POST, entity,
          new ParameterizedTypeReference<Map<String, Object>>() {});
      log.info("Historia creada en Taiga: ref={}, id={}",
          response.getBody().get("ref"), response.getBody().get("id"));
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al crear historia en Taiga: {}", e.getMessage());
      return null;
    }
  }

  public Map<String, Object> updateStoryStatus(int storyId, TaigaStatusRequest request) {
    if (!config.isEnabled()) return null;
    try {
      Map<String, Object> story = getStory(storyId);
      if (story == null) return null;
      Integer version = (Integer) story.get("version");
      if (version == null) version = 1;
      Map<String, Object> body = new HashMap<>();
      body.put("version", version);
      body.put("status", request.status());
      String url = config.getApiUrl() + "/userstories/" + storyId;
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.PATCH, new HttpEntity<>(body, authHeaders()),
          new ParameterizedTypeReference<Map<String, Object>>() {});
      log.info("Historia {} actualizada a estado={} (version={})", storyId, request.status(), version);
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al actualizar estado de historia {}: {}", storyId, e.getMessage());
      return null;
    }
  }

  public Map<String, Object> addComment(int storyId, TaigaCommentRequest request) {
    if (!config.isEnabled()) return null;
    try {
      Map<String, Object> story = getStory(storyId);
      if (story == null) return null;
      Integer version = (Integer) story.get("version");
      if (version == null) version = 1;
      Map<String, Object> body = new HashMap<>();
      body.put("version", version);
      body.put("comment", request.comment());
      String url = config.getApiUrl() + "/userstories/" + storyId;
      ResponseEntity<Map<String, Object>> response = rest.exchange(
          url, HttpMethod.PATCH, new HttpEntity<>(body, authHeaders()),
          new ParameterizedTypeReference<Map<String, Object>>() {});
      log.info("Comentario agregado a historia {} (version={})", storyId, version);
      return response.getBody();
    } catch (Exception e) {
      log.error("Error al agregar comentario a historia {}: {}", storyId, e.getMessage());
      return null;
    }
  }

  public Map<String, Object> summary() {
    if (!config.isEnabled()) {
      return Map.of(
          "totalStories", 0, "completedStories", 0,
          "completionPercent", 0, "taigaEnabled", false,
          "project", "GreenHouse Manager");
    }
    try {
      List<Map<String, Object>> stories = listStories();
      long total = stories.size();
      long completed = stories.stream()
          .filter(s -> {
            Object statusObj = s.get("status");
            if (statusObj instanceof Map) {
              String name = (String) ((Map<?, ?>) statusObj).get("name");
              return "done".equalsIgnoreCase(name) || "closed".equalsIgnoreCase(name);
            }
            if (statusObj instanceof Integer) {
              return (Integer) statusObj == 4;
            }
            return "done".equalsIgnoreCase(String.valueOf(statusObj)) || "closed".equalsIgnoreCase(String.valueOf(statusObj));
          })
          .count();
      return Map.of(
          "totalStories", total,
          "completedStories", completed,
          "completionPercent", total > 0 ? (completed * 100 / total) : 0,
          "project", "GreenHouse Manager",
          "taigaEnabled", true);
    } catch (Exception e) {
      log.error("Error al obtener resumen de Taiga: {}", e.getMessage());
      return Map.of(
          "totalStories", 0, "completedStories", 0,
          "completionPercent", 0, "taigaEnabled", true,
          "project", "GreenHouse Manager", "error", e.getMessage());
    }
  }

  public TaigaSyncResponse syncStories(List<Map<String, Object>> generatedStories) {
    if (!config.isEnabled()) {
      return new TaigaSyncResponse(false, "Taiga no configurado — configure TAIGA_TOKEN y TAIGA_PROJECT_ID",
          0, 0, generatedStories.size(),
          List.of("TAIGA_DISABLED"));
    }
    int created = 0;
    int updated = 0;
    int skipped = 0;
    List<String> errors = new ArrayList<>();

    List<Map<String, Object>> existingStories = listStories();

    for (Map<String, Object> story : generatedStories) {
      try {
        String subject = String.valueOf(story.getOrDefault("subject", ""));
        if (subject.isBlank()) { skipped++; continue; }
        Map<String, Object> existing = findExistingBySubject(existingStories, subject);
        if (existing != null) {
          int existingId = Integer.parseInt(String.valueOf(existing.get("id")));
          Integer version = (Integer) existing.get("version");
          Map<String, Object> updateBody = new HashMap<>();
          updateBody.put("version", version != null ? version : 1);
          updateBody.put("subject", story.get("subject"));
          updateBody.put("description", story.get("description"));
          String url = config.getApiUrl() + "/userstories/" + existingId;
          rest.exchange(url, HttpMethod.PATCH, new HttpEntity<>(updateBody, authHeaders()),
              new ParameterizedTypeReference<Map<String, Object>>() {});
          updated++;
          log.info("Historia actualizada: id={}, subject={}", existingId, subject);
        } else {
          Map<String, Object> createBody = new HashMap<>();
          createBody.put("project", config.getProjectId());
          createBody.put("subject", story.get("subject"));
          createBody.put("description", story.get("description"));
          createBody.put("status", story.getOrDefault("status", "new"));
          if (story.containsKey("epic") && story.get("epic") != null) {
            createBody.put("epic_name", story.get("epic"));
          }
          Map<String, Object> result = createStory(createBody);
          if (result != null) {
            created++;
          } else {
            errors.add("Fallo al crear: " + subject);
            skipped++;
          }
        }
      } catch (Exception e) {
        errors.add("Error en '" + story.get("subject") + "': " + e.getMessage());
        skipped++;
      }
    }
    log.info("Sync real completada: creadas={}, actualizadas={}, omitidas={}, errores={}",
        created, updated, skipped, errors.size());
    return new TaigaSyncResponse(true,
        "Sincronizacion real completada: " + created + " creadas, " + updated + " actualizadas, "
            + skipped + " omitidas",
        created, updated, skipped, errors);
  }

  private Map<String, Object> findExistingBySubject(List<Map<String, Object>> stories, String subject) {
    for (Map<String, Object> s : stories) {
      if (subject.equals(s.get("subject"))) return s;
    }
    return null;
  }
}
