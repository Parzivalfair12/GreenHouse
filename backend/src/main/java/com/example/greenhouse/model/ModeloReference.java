package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloReference {

  private String entity;
  private String field;
  private String cardinality;

  public String getEntity() { return entity; }
  public void setEntity(String entity) { this.entity = entity; }

  public String getField() { return field; }
  public void setField(String field) { this.field = field; }

  public String getCardinality() { return cardinality; }
  public void setCardinality(String cardinality) { this.cardinality = cardinality; }
}
