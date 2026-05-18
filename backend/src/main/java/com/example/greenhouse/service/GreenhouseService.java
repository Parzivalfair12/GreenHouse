package com.example.greenhouse.service;

import com.example.greenhouse.domain.Crop;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.IrrigationEvent;
import com.example.greenhouse.domain.IrrigationMode;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.repository.GreenhouseRepository;
import com.example.greenhouse.web.dto.CropRequest;
import com.example.greenhouse.web.dto.GreenhouseRequest;
import com.example.greenhouse.web.dto.IrrigationRequest;
import com.example.greenhouse.web.dto.SensorRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business operations for greenhouse records. */
@Service
public class GreenhouseService {
  private final GreenhouseRepository repository;

  public GreenhouseService(GreenhouseRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public List<Greenhouse> findAll() {
    return repository.findAll();
  }

  @Transactional(readOnly = true)
  public Greenhouse findById(long id) {
    return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Greenhouse not found"));
  }

  @Transactional
  public Greenhouse create(GreenhouseRequest request) {
    Greenhouse greenhouse = new Greenhouse();
    greenhouse.name = request.name();
    greenhouse.location = request.location();
    greenhouse.areaSquareMeters = request.areaSquareMeters();
    greenhouse.active = request.active();
    return repository.save(greenhouse);
  }

  @Transactional
  public Greenhouse update(long id, GreenhouseRequest request) {
    Greenhouse greenhouse = findById(id);
    greenhouse.name = request.name();
    greenhouse.location = request.location();
    greenhouse.areaSquareMeters = request.areaSquareMeters();
    greenhouse.active = request.active();
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse addCrop(long greenhouseId, CropRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    Crop crop = new Crop();
    crop.name = request.name();
    crop.variety = request.variety();
    crop.plantedAt = request.plantedAt();
    crop.expectedHarvestAt = request.expectedHarvestAt();
    crop.greenhouse = greenhouse;
    greenhouse.crops.add(crop);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse updateCrop(long greenhouseId, long cropId, CropRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    Crop crop = greenhouse.crops.stream()
        .filter(item -> item.id == cropId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Crop not found"));
    crop.name = request.name();
    crop.variety = request.variety();
    crop.plantedAt = request.plantedAt();
    crop.expectedHarvestAt = request.expectedHarvestAt();
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse addSensor(long greenhouseId, SensorRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    Sensor sensor = new Sensor();
    sensor.code = request.code();
    sensor.type = request.type();
    sensor.unit = request.unit();
    sensor.minThreshold = request.minThreshold();
    sensor.maxThreshold = request.maxThreshold();
    sensor.greenhouse = greenhouse;
    greenhouse.sensors.add(sensor);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse updateSensor(long greenhouseId, long sensorId, SensorRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    Sensor sensor = greenhouse.sensors.stream()
        .filter(item -> item.id == sensorId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Sensor not found"));
    sensor.code = request.code();
    sensor.type = request.type();
    sensor.unit = request.unit();
    sensor.minThreshold = request.minThreshold();
    sensor.maxThreshold = request.maxThreshold();
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse addIrrigation(long greenhouseId, IrrigationRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    IrrigationEvent event = new IrrigationEvent();
    event.startedAt = LocalDateTime.now();
    event.durationMinutes = request.durationMinutes();
    event.waterLiters = request.waterLiters();
    event.mode = request.mode() == null ? IrrigationMode.MANUAL : request.mode();
    event.greenhouse = greenhouse;
    greenhouse.irrigationEvents.add(event);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public Greenhouse updateIrrigation(long greenhouseId, long eventId, IrrigationRequest request) {
    Greenhouse greenhouse = findById(greenhouseId);
    IrrigationEvent event = greenhouse.irrigationEvents.stream()
        .filter(item -> item.id == eventId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Irrigation event not found"));
    event.durationMinutes = request.durationMinutes();
    event.waterLiters = request.waterLiters();
    event.mode = request.mode() == null ? IrrigationMode.MANUAL : request.mode();
    return repository.saveAndFlush(greenhouse);
  }
}
