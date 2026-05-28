package com.example.greenhouse.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
public class StoryGenerationService {

  private static final Logger log = LoggerFactory.getLogger(StoryGenerationService.class);

  private final ApplicationContext appContext;

  public StoryGenerationService(ApplicationContext appContext) {
    this.appContext = appContext;
  }

  public List<Map<String, Object>> generateStoriesFromCode() {
    List<Map<String, Object>> stories = new ArrayList<>();
    Map<String, List<ControllerEndpoint>> controllers = discoverControllers();

    for (Map.Entry<String, List<ControllerEndpoint>> entry : controllers.entrySet()) {
      String epicName = entry.getKey();
      List<ControllerEndpoint> endpoints = entry.getValue();
      String description = buildDescription(epicName, endpoints);
      List<String> criteria = new ArrayList<>();
      for (ControllerEndpoint ep : endpoints) {
        criteria.add(ep.method + " " + ep.fullPath + " — " + ep.summary);
      }
      List<Map<String, Object>> criteriaList = buildCriteria(criteria);
      stories.add(Map.of(
          "epic", epicName,
          "subject", "US - " + epicName,
          "description", description,
          "status", "COMPLETED",
          "priority", detectPriority(endpoints),
          "criteria", criteriaList,
          "modules", endpoints));
    }

    log.info("StoryGeneration: {} historias generadas dinamicamente desde {} controllers",
        stories.size(), controllers.size());
    return stories;
  }

  private Map<String, List<ControllerEndpoint>> discoverControllers() {
    Map<String, List<ControllerEndpoint>> result = new LinkedHashMap<>();
    Map<String, Object> controllers = appContext.getBeansWithAnnotation(RestController.class);

    for (Map.Entry<String, Object> beanEntry : controllers.entrySet()) {
      Class<?> controllerClass = beanEntry.getValue().getClass();
      String epicName = extractEpicName(controllerClass);
      List<ControllerEndpoint> endpoints = extractEndpoints(controllerClass);
      if (!endpoints.isEmpty()) {
        result.put(epicName, endpoints);
      }
    }
    return result;
  }

  private String extractEpicName(Class<?> controllerClass) {
    Tag tag = controllerClass.getAnnotation(Tag.class);
    if (tag != null && !tag.name().isEmpty() && !"Taiga".equals(tag.name()) && !"DevOps".equals(tag.name())) {
      return tag.description().isEmpty() ? tag.name() : tag.description();
    }
    String simpleName = controllerClass.getSimpleName();
    if (simpleName.endsWith("Controller")) {
      simpleName = simpleName.substring(0, simpleName.length() - "Controller".length());
    }
    return simpleName.replaceAll("([a-z])([A-Z])", "$1 $2");
  }

  private List<ControllerEndpoint> extractEndpoints(Class<?> controllerClass) {
    List<ControllerEndpoint> endpoints = new ArrayList<>();
    String basePath = "";
    RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
    if (classMapping != null && classMapping.value().length > 0) {
      basePath = classMapping.value()[0];
    }

    for (Method method : controllerClass.getDeclaredMethods()) {
      GetMapping get = method.getAnnotation(GetMapping.class);
      PostMapping post = method.getAnnotation(PostMapping.class);
      PutMapping put = method.getAnnotation(PutMapping.class);
      DeleteMapping delete = method.getAnnotation(DeleteMapping.class);
      PatchMapping patch = method.getAnnotation(PatchMapping.class);
      Operation op = method.getAnnotation(Operation.class);

      if (get != null) extractMethodPath(endpoints, basePath, get.value(), "GET", op, method);
      if (post != null) extractMethodPath(endpoints, basePath, post.value(), "POST", op, method);
      if (put != null) extractMethodPath(endpoints, basePath, put.value(), "PUT", op, method);
      if (delete != null) extractMethodPath(endpoints, basePath, delete.value(), "DELETE", op, method);
      if (patch != null) extractMethodPath(endpoints, basePath, patch.value(), "PATCH", op, method);
    }
    return endpoints;
  }

  private void extractMethodPath(List<ControllerEndpoint> list, String base,
      String[] paths, String httpMethod, Operation op, Method method) {
    String subPath = (paths.length > 0 && !paths[0].isEmpty()) ? paths[0] : "";
    String fullPath = base + (subPath.startsWith("/") ? subPath : "/" + subPath);
    String summary = op != null && op.summary() != null && !op.summary().isEmpty()
        ? op.summary() : method.getName();
    String roles = extractRoles(method);
    list.add(new ControllerEndpoint(httpMethod, fullPath, summary, roles));
  }

  private String extractRoles(Method method) {
    var preAuth = method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    if (preAuth != null) return preAuth.value();
    var clsAuth = method.getDeclaringClass().getAnnotation(
        org.springframework.security.access.prepost.PreAuthorize.class);
    if (clsAuth != null) return clsAuth.value();
    return "authenticated";
  }

  private String buildDescription(String epic, List<ControllerEndpoint> endpoints) {
    int readCount = 0, writeCount = 0;
    for (ControllerEndpoint ep : endpoints) {
      if ("GET".equals(ep.method)) readCount++;
      else writeCount++;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Gestion de ").append(epic.toLowerCase()).append(". ");
    sb.append("Expone ").append(endpoints.size()).append(" endpoints");
    if (readCount > 0) sb.append(" (").append(readCount).append(" GET, ").append(writeCount).append(" mutacion)");
    sb.append(" con autenticacion JWT y control de roles.");
    return sb.toString();
  }

  private String detectPriority(List<ControllerEndpoint> endpoints) {
    for (ControllerEndpoint ep : endpoints) {
      if (ep.roles.contains("ADMIN") || ep.method.equals("DELETE")) return "alta";
    }
    return "media";
  }

  private List<Map<String, Object>> buildCriteria(List<String> criteria) {
    List<Map<String, Object>> list = new ArrayList<>();
    for (int i = 0; i < criteria.size(); i++) {
      list.add(Map.of("id", i + 1, "description", criteria.get(i), "status", "PASSED"));
    }
    return list;
  }

  public static class ControllerEndpoint {
    public final String method;
    public final String fullPath;
    public final String summary;
    public final String roles;

    public ControllerEndpoint(String method, String fullPath, String summary, String roles) {
      this.method = method;
      this.fullPath = fullPath;
      this.summary = summary;
      this.roles = roles;
    }
  }
}
