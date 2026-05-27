package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloRelationship {

  private String from;
  private String to;
  private String type;
  private String fk;
  private String cardinality;

  public String getFrom() { return from; }
  public void setFrom(String from) { this.from = from; }

  public String getTo() { return to; }
  public void setTo(String to) { this.to = to; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public String getFk() { return fk; }
  public void setFk(String fk) { this.fk = fk; }

  public String getCardinality() { return cardinality; }
  public void setCardinality(String cardinality) { this.cardinality = cardinality; }
}
