package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloEntity {

  private String name;
  private String table;
  private String description;
  private boolean audit;

  @JsonProperty("softDelete")
  private boolean softDelete;

  @JsonProperty("timestamps")
  private boolean timestamps;

  @JsonProperty("fields")
  private List<ModeloField> fields;

  @JsonProperty("indexes")
  private List<ModeloIndex> indexes;

  @JsonProperty("permissions")
  private List<String> permissions;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getTable() { return table; }
  public void setTable(String table) { this.table = table; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public boolean isAudit() { return audit; }
  public void setAudit(boolean audit) { this.audit = audit; }

  public boolean isSoftDelete() { return softDelete; }
  public void setSoftDelete(boolean softDelete) { this.softDelete = softDelete; }

  public boolean isTimestamps() { return timestamps; }
  public void setTimestamps(boolean timestamps) { this.timestamps = timestamps; }

  public List<ModeloField> getFields() { return fields; }
  public void setFields(List<ModeloField> fields) { this.fields = fields; }

  public List<ModeloIndex> getIndexes() { return indexes; }
  public void setIndexes(List<ModeloIndex> indexes) { this.indexes = indexes; }

  public List<String> getPermissions() { return permissions; }
  public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
