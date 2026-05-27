package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloDefinition {

  private String project;
  private String version;
  private String database;
  private String basePackage;

  @JsonProperty("entities")
  private List<ModeloEntity> entities;

  @JsonProperty("enums")
  private List<ModeloEnum> enums;

  @JsonProperty("relationships")
  private List<ModeloRelationship> relationships;

  public String getProject() { return project; }
  public void setProject(String project) { this.project = project; }

  public String getVersion() { return version; }
  public void setVersion(String version) { this.version = version; }

  public String getDatabase() { return database; }
  public void setDatabase(String database) { this.database = database; }

  public String getBasePackage() { return basePackage; }
  public void setBasePackage(String basePackage) { this.basePackage = basePackage; }

  public List<ModeloEntity> getEntities() { return entities; }
  public void setEntities(List<ModeloEntity> entities) { this.entities = entities; }

  public List<ModeloEnum> getEnums() { return enums; }
  public void setEnums(List<ModeloEnum> enums) { this.enums = enums; }

  public List<ModeloRelationship> getRelationships() { return relationships; }
  public void setRelationships(List<ModeloRelationship> relationships) { this.relationships = relationships; }
}
