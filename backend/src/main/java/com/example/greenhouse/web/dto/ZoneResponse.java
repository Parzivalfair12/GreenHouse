package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.Zone;

public record ZoneResponse(Long id, String name, String description, boolean active, Long greenhouseId, String greenhouseName) {
  public static ZoneResponse from(Zone zone) {
    return new ZoneResponse(zone.id, zone.name, zone.description, zone.active, zone.greenhouse.id, zone.greenhouse.name);
  }
}
