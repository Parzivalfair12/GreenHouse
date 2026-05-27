package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloField {

  private String name;
  private String type;
  private boolean pk;
  private boolean fk;
  private boolean nullable;
  private boolean unique;
  private String column;
  private String validation;
  private String description;
  private String defaultValue;

  @JsonProperty("enum")
  private List<String> enumValues;

  @JsonProperty("references")
  private ModeloReference references;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public boolean isPk() { return pk; }
  public void setPk(boolean pk) { this.pk = pk; }

  public boolean isFk() { return fk; }
  public void setFk(boolean fk) { this.fk = fk; }

  public boolean isNullable() { return nullable; }
  public void setNullable(boolean nullable) { this.nullable = nullable; }

  public boolean isUnique() { return unique; }
  public void setUnique(boolean unique) { this.unique = unique; }

  public String getColumn() { return column; }
  public void setColumn(String column) { this.column = column; }

  public String getValidation() { return validation; }
  public void setValidation(String validation) { this.validation = validation; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getDefaultValue() { return defaultValue; }
  public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

  public List<String> getEnumValues() { return enumValues; }
  public void setEnumValues(List<String> enumValues) { this.enumValues = enumValues; }

  public ModeloReference getReferences() { return references; }
  public void setReferences(ModeloReference references) { this.references = references; }
}
