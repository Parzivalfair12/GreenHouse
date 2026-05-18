package com.example.greenhouse.web;

import com.example.greenhouse.service.GreenhouseService;
import com.example.greenhouse.web.dto.CropRequest;
import com.example.greenhouse.web.dto.GreenhouseRequest;
import com.example.greenhouse.web.dto.GreenhouseResponse;
import com.example.greenhouse.web.dto.IrrigationRequest;
import com.example.greenhouse.web.dto.SensorRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

/** REST endpoints for greenhouse management. */
@RestController
@RequestMapping("/api/greenhouses")
public class GreenhouseController {
  private final GreenhouseService service;

  public GreenhouseController(GreenhouseService service) {
    this.service = service;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<GreenhouseResponse> findAll() {
    return service.findAll().stream().map(GreenhouseResponse::from).toList();
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public GreenhouseResponse findById(@PathVariable long id) {
    return GreenhouseResponse.from(service.findById(id));
  }

  @PostMapping
  @Transactional
  @ResponseStatus(HttpStatus.CREATED)
  public GreenhouseResponse create(@Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.create(request));
  }

  @PutMapping("/{id}")
  @Transactional
  public GreenhouseResponse update(@PathVariable long id, @Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.update(id, request));
  }

  @PostMapping("/{id}/crops")
  @Transactional
  public GreenhouseResponse addCrop(@PathVariable long id, @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.addCrop(id, request));
  }

  @PutMapping("/{id}/crops/{cropId}")
  @Transactional
  public GreenhouseResponse updateCrop(
      @PathVariable long id,
      @PathVariable long cropId,
      @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.updateCrop(id, cropId, request));
  }

  @PostMapping("/{id}/sensors")
  @Transactional
  public GreenhouseResponse addSensor(@PathVariable long id, @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.addSensor(id, request));
  }

  @PutMapping("/{id}/sensors/{sensorId}")
  @Transactional
  public GreenhouseResponse updateSensor(
      @PathVariable long id,
      @PathVariable long sensorId,
      @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.updateSensor(id, sensorId, request));
  }

  @PostMapping("/{id}/irrigation-events")
  @Transactional
  public GreenhouseResponse addIrrigation(@PathVariable long id, @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.addIrrigation(id, request));
  }

  @PutMapping("/{id}/irrigation-events/{eventId}")
  @Transactional
  public GreenhouseResponse updateIrrigation(
      @PathVariable long id,
      @PathVariable long eventId,
      @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.updateIrrigation(id, eventId, request));
  }
}
