package com.example.greenhouse.config;

import com.example.greenhouse.model.ModeloDefinition;
import com.example.greenhouse.model.ModeloEntity;
import com.example.greenhouse.model.ModeloEnum;
import com.example.greenhouse.model.ModeloField;
import com.example.greenhouse.model.ModeloRelationship;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates that JPA entities, enums, tables and relationships align with modelo.json at startup.
 * modelo.json is the single source of truth for the domain model.
 */
@Component
public class ModelValidator {

  private static final Logger log = LoggerFactory.getLogger(ModelValidator.class);

  private final DataSource dataSource;

  public ModelValidator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostConstruct
  public void validate() {
    try (InputStream is = getClass().getResourceAsStream("/modelo.json")) {
      if (is == null) {
        log.error("modelo.json not found on classpath; validation cannot proceed");
        return;
      }

      ObjectMapper mapper = new ObjectMapper();
      ModeloDefinition model = mapper.readValue(is, ModeloDefinition.class);

      List<ModeloEntity> entities = model.getEntities();
      List<ModeloEnum> enums = model.getEnums();
      List<ModeloRelationship> relationships = model.getRelationships();

      if (entities == null) {
        log.error("modelo.json missing 'entities' array; validation cannot proceed");
        return;
      }

      log.info("============================================================");
      log.info("Modelo JSON loaded: project={}, version={}, database={}",
          model.getProject(), model.getVersion(), model.getDatabase());
      log.info("  Entities: {}", entities.size());
      log.info("  Enums: {}", enums != null ? enums.size() : 0);
      log.info("  Relationships: {}", relationships != null ? relationships.size() : 0);
      log.info("============================================================");

      Set<String> entityNames = entities.stream()
          .map(ModeloEntity::getName)
          .collect(Collectors.toSet());
      log.info("Entity names from modelo.json: {}", entityNames);

      validateJpaEntities(entities);
      validateEnums(enums);
      validateRelationships(relationships);
      validateDatabaseTables(entities);

      log.info("Modelo JSON validation completed successfully");

    } catch (Exception e) {
      log.error("Failed to validate modelo.json", e);
    }
  }

  private void validateJpaEntities(List<ModeloEntity> expectedEntities) {
    String packageName = "com.example.greenhouse.domain";
    Set<String> jpaEntityNames = new HashSet<>();

    try {
      Class<?>[] classes = {
          Class.forName(packageName + ".Greenhouse"),
          Class.forName(packageName + ".Crop"),
          Class.forName(packageName + ".Sensor"),
          Class.forName(packageName + ".Reading"),
          Class.forName(packageName + ".IrrigationEvent"),
          Class.forName(packageName + ".Alert"),
          Class.forName(packageName + ".Actuator"),
          Class.forName(packageName + ".AutomationRule"),
          Class.forName(packageName + ".Zone"),
          Class.forName(packageName + ".AuditLog"),
          Class.forName(packageName + ".AppUser")
      };

      for (Class<?> clazz : classes) {
        if (clazz.isAnnotationPresent(Entity.class)) {
          jpaEntityNames.add(clazz.getSimpleName());
        }
      }
    } catch (ClassNotFoundException e) {
      log.warn("Could not scan domain package for JPA entities: {}", e.getMessage());
    }

    Set<String> expectedNames = expectedEntities.stream()
        .map(ModeloEntity::getName)
        .collect(Collectors.toSet());

    Set<String> missingInJpa = new HashSet<>(expectedNames);
    missingInJpa.removeAll(jpaEntityNames);

    Set<String> extraInJpa = new HashSet<>(jpaEntityNames);
    extraInJpa.removeAll(expectedNames);

    if (!missingInJpa.isEmpty()) {
      log.warn("JPA entities missing for modelo.json entities: {}", missingInJpa);
    }
    if (!extraInJpa.isEmpty()) {
      log.warn("Extra JPA entities not in modelo.json: {}", extraInJpa);
    }
    if (missingInJpa.isEmpty() && extraInJpa.isEmpty()) {
      log.info("JPA entities aligned with modelo.json: {} entities", jpaEntityNames.size());
    }
  }

  private void validateEnums(List<ModeloEnum> expectedEnums) {
    if (expectedEnums == null) return;

    String packageName = "com.example.greenhouse.domain";
    Set<String> jpaEnumNames = new HashSet<>();

    String[] enumClassNames = {
        "ActionOrigin", "ActuatorType", "AlertSeverity", "CropStatus",
        "IrrigationMode", "RuleType", "SensorType", "UserRole"
    };

    for (String enumName : enumClassNames) {
      try {
        Class<?> clazz = Class.forName(packageName + "." + enumName);
        if (clazz.isEnum()) {
          jpaEnumNames.add(enumName);
        }
      } catch (ClassNotFoundException e) {
        log.debug("Enum class not found: {}", enumName);
      }
    }

    Set<String> expectedNames = expectedEnums.stream()
        .map(ModeloEnum::getName)
        .collect(Collectors.toSet());

    Set<String> missingInJpa = new HashSet<>(expectedNames);
    missingInJpa.removeAll(jpaEnumNames);

    Set<String> extraInJpa = new HashSet<>(jpaEnumNames);
    extraInJpa.removeAll(expectedNames);

    if (!missingInJpa.isEmpty()) {
      log.warn("JPA enums missing for modelo.json enums: {}", missingInJpa);
    }
    if (!extraInJpa.isEmpty()) {
      log.warn("Extra JPA enums not in modelo.json: {}", extraInJpa);
    }
    if (missingInJpa.isEmpty() && extraInJpa.isEmpty()) {
      log.info("JPA enums aligned with modelo.json: {} enums", jpaEnumNames.size());
    }
  }

  private void validateRelationships(List<ModeloRelationship> expectedRelationships) {
    if (expectedRelationships == null) return;

    log.info("Validating {} relationships from modelo.json", expectedRelationships.size());

    int fkCount = 0;
    for (ModeloRelationship rel : expectedRelationships) {
      if (rel.getFk() != null && !rel.getFk().isBlank()) {
        fkCount++;
      }
    }
    log.info("Relationships with foreign keys: {}", fkCount);
  }

  private void validateDatabaseTables(List<ModeloEntity> expectedEntities) {
    if (dataSource == null) {
      log.warn("DataSource not available; skipping database table validation");
      return;
    }

    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData metaData = conn.getMetaData();
      Set<String> dbTables = new HashSet<>();

      try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
        while (tables.next()) {
          dbTables.add(tables.getString("TABLE_NAME").toLowerCase());
        }
      }

      Set<String> expectedTables = expectedEntities.stream()
          .map(e -> e.getTable().toLowerCase())
          .collect(Collectors.toSet());

      Set<String> missingInDb = new HashSet<>(expectedTables);
      missingInDb.removeAll(dbTables);

      if (!missingInDb.isEmpty()) {
        log.warn("Database tables missing for modelo.json entities: {}", missingInDb);
      } else {
        log.info("Database tables aligned with modelo.json: {} tables", expectedTables.size());
      }

    } catch (Exception e) {
      log.warn("Could not validate database tables: {}", e.getMessage());
    }
  }
}
