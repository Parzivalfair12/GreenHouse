package com.example.greenhouse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModeloEnum {

  private String name;
  private List<String> values;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public List<String> getValues() { return values; }
  public void setValues(List<String> values) { this.values = values; }
}
