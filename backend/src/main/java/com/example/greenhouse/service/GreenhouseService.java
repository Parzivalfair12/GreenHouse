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

/**
 * Servicio de operaciones de negocio para el ciclo de vida de invernaderos,
 * incluyendo CRUD de invernaderos, cultivos, sensores y eventos de riego.
 *
 * Las operaciones son transaccionales — las mutaciones en invernaderos
 * cascadan a entidades hijas (cultivos, sensores, riegos) mediante
 * JPA {@code CascadeType} y {@code orphanRemoval}.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class GreenhouseService {
  private static final Logger log = LoggerFactory.getLogger(GreenhouseService.class);

  private final GreenhouseRepository repository;

  public GreenhouseService(GreenhouseRepository repository) {
    this.repository = repository;
  }

  /**
   * Lists all greenhouses with their child collections (crops, sensors,
   * irrigation events) eagerly loaded via JPA fetch joins.
   *
   * @return all greenhouses, empty list if none exist
   * @since 2.1.0
   */
  @Transactional(readOnly = true)
  public List<Greenhouse> findAll() {
    return repository.findAll();
  }

  /**
   * Retrieves a single greenhouse by primary key.
   *
   * @param id greenhouse ID
   * @return the found greenhouse
   * @throws IllegalArgumentException if no greenhouse exists with the given id
   * @since 2.1.0
   */
  @Transactional(readOnly = true)
  public Greenhouse findById(long id) {
    return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Greenhouse not found"));
  }

  /**
   * Creates a new greenhouse from the validated request DTO.
   *
   * @param request greenhouse creation payload with name, location, area and active status
   * @return the persisted greenhouse
   * @since 2.1.0
   */
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

  /**
   * Updates an existing greenhouse's mutable fields.
   *
   * @param id      greenhouse ID
   * @param request update payload
   * @return the updated greenhouse after flush
   * @throws IllegalArgumentException if greenhouse not found
   * @since 2.1.0
   */
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

  /**
   * Deletes a greenhouse and all its associated data.
   *
   * @param id greenhouse ID to delete
   * @throws IllegalArgumentException if greenhouse not found
   * @since 2.1.0
   */
  @Transactional
  public void delete(long id) {
    Greenhouse greenhouse = findById(id);
    repository.delete(greenhouse);
    log.info("Deleted greenhouse: {}", id);
  }

  /**
   * Adds a crop to a greenhouse.
   *
   * @param greenhouseId parent greenhouse ID
   * @param request      crop data (name, variety, planting dates)
   * @return the updated greenhouse
   * @since 2.1.0
   */
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

  /**
   * Updates an existing crop's data.
   *
   * @since 2.1.0
   */
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

  /**
   * Removes a crop from a greenhouse.
   *
   * @since 2.1.0
   */
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

  /**
   * Registers a new sensor in a greenhouse with type, unit and threshold limits.
   *
   * @since 2.1.0
   */
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

  /**
   * Updates an existing sensor's metadata.
   *
   * @since 2.1.0
   */
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

  /**
   * Removes a sensor from a greenhouse.
   *
   * @since 2.1.0
   */
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

  /**
   * Records a manual irrigation event for a greenhouse.
   *
   * The event is timestamped at creation time. Mode defaults to
   * {@link IrrigationMode#MANUAL} if not specified.
   *
   * @since 2.1.0
   */
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

  /**
   * Updates an existing irrigation event's parameters.
   *
   * @since 2.1.0
   */
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

  /**
   * Removes an irrigation event from a greenhouse.
   *
   * @since 2.1.0
   */
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
