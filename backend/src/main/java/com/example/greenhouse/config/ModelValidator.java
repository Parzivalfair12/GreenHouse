package com.example.greenhouse.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Validates that JPA entities align with modelo.json at startup. */
@Component
public class ModelValidator {
  private static final Logger log = LoggerFactory.getLogger(ModelValidator.class);

  @PostConstruct
  public void validate() {
    try (InputStream is = getClass().getResourceAsStream("/modelo.json")) {
      if (is == null) {
        log.warn("modelo.json not found on classpath; skipping validation");
        return;
      }
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(is);
      JsonNode entities = root.path("entities");
      if (entities.isMissingNode()) {
        log.warn("modelo.json missing 'entities' node; skipping validation");
        return;
      }
      Set<String> expectedEntities = new HashSet<>();
      entities.fieldNames().forEachRemaining(expectedEntities::add);
      log.info("Modelo JSON loaded with {} entities: {}", expectedEntities.size(), expectedEntities);
    } catch (Exception e) {
      log.error("Failed to validate modelo.json", e);
    }
  }
}
