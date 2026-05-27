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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business operations for greenhouse records. */
@Service
public class GreenhouseService {
  private static final Logger log = LoggerFactory.getLogger(GreenhouseService.class);

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
    Greenhouse saved = repository.save(greenhouse);
    log.info("Created greenhouse: {}", saved.name);
    return saved;
  }

  @Transactional
  public Greenhouse update(long id, GreenhouseRequest request) {
    Greenhouse greenhouse = findById(id);
    greenhouse.name = request.name();
    greenhouse.location = request.location();
    greenhouse.areaSquareMeters = request.areaSquareMeters();
    greenhouse.active = request.active();
    log.info("Updated greenhouse: {}", greenhouse.name);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public void delete(long id) {
    Greenhouse greenhouse = findById(id);
    repository.delete(greenhouse);
    log.info("Deleted greenhouse: {}", id);
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
    log.info("Added crop {} to greenhouse {}", crop.name, greenhouseId);
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
    log.info("Updated crop {} in greenhouse {}", cropId, greenhouseId);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public void deleteCrop(long greenhouseId, long cropId) {
    Greenhouse greenhouse = findById(greenhouseId);
    Crop crop = greenhouse.crops.stream()
        .filter(item -> item.id == cropId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Crop not found"));
    greenhouse.crops.remove(crop);
    log.info("Deleted crop {} from greenhouse {}", cropId, greenhouseId);
    repository.saveAndFlush(greenhouse);
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
    log.info("Added sensor {} to greenhouse {}", sensor.code, greenhouseId);
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
    log.info("Updated sensor {} in greenhouse {}", sensorId, greenhouseId);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public void deleteSensor(long greenhouseId, long sensorId) {
    Greenhouse greenhouse = findById(greenhouseId);
    Sensor sensor = greenhouse.sensors.stream()
        .filter(item -> item.id == sensorId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Sensor not found"));
    greenhouse.sensors.remove(sensor);
    log.info("Deleted sensor {} from greenhouse {}", sensorId, greenhouseId);
    repository.saveAndFlush(greenhouse);
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
    log.info("Added irrigation event to greenhouse {}", greenhouseId);
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
    log.info("Updated irrigation event {} in greenhouse {}", eventId, greenhouseId);
    return repository.saveAndFlush(greenhouse);
  }

  @Transactional
  public void deleteIrrigation(long greenhouseId, long eventId) {
    Greenhouse greenhouse = findById(greenhouseId);
    IrrigationEvent event = greenhouse.irrigationEvents.stream()
        .filter(item -> item.id == eventId)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Irrigation event not found"));
    greenhouse.irrigationEvents.remove(event);
    log.info("Deleted irrigation event {} from greenhouse {}", eventId, greenhouseId);
    repository.saveAndFlush(greenhouse);
  }
}
