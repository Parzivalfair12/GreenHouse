package com.example.greenhouse.web;

import com.example.greenhouse.service.GreenhouseService;
import com.example.greenhouse.web.dto.CropRequest;
import com.example.greenhouse.web.dto.GreenhouseRequest;
import com.example.greenhouse.web.dto.GreenhouseResponse;
import com.example.greenhouse.web.dto.IrrigationRequest;
import com.example.greenhouse.web.dto.SensorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Invernaderos", description = "Gestion de invernaderos, cultivos, sensores y riegos")
@RestController
@RequestMapping("/api/greenhouses")
public class GreenhouseController {
  private final GreenhouseService service;

  public GreenhouseController(GreenhouseService service) {
    this.service = service;
  }

  @Operation(summary = "Listar invernaderos", description = "Devuelve todos los invernaderos activos")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de invernaderos")})
  @GetMapping
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public List<GreenhouseResponse> findAll() {
    return service.findAll().stream().map(GreenhouseResponse::from).toList();
  }

  @Operation(summary = "Obtener invernadero", description = "Devuelve un invernadero por su ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Invernadero encontrado"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado")
  })
  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  @PreAuthorize("isAuthenticated()")
  public GreenhouseResponse findById(@PathVariable long id) {
    return GreenhouseResponse.from(service.findById(id));
  }

  @Operation(summary = "Crear invernadero", description = "Crea un nuevo invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Invernadero creado"),
      @ApiResponse(responseCode = "403", description = "No autorizado")
  })
  @PostMapping
  @Transactional
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse create(@Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.create(request));
  }

  @Operation(summary = "Actualizar invernadero", description = "Modifica un invernadero existente (requiere ADMIN)")
  @PutMapping("/{id}")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse update(@PathVariable long id, @Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.update(id, request));
  }

  @PostMapping("/{id}/crops")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addCrop(@PathVariable long id, @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.addCrop(id, request));
  }

  @PutMapping("/{id}/crops/{cropId}")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateCrop(
      @PathVariable long id,
      @PathVariable long cropId,
      @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.updateCrop(id, cropId, request));
  }

  @PostMapping("/{id}/sensors")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addSensor(@PathVariable long id, @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.addSensor(id, request));
  }

  @PutMapping("/{id}/sensors/{sensorId}")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateSensor(
      @PathVariable long id,
      @PathVariable long sensorId,
      @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.updateSensor(id, sensorId, request));
  }

  @PostMapping("/{id}/irrigation-events")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addIrrigation(@PathVariable long id, @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.addIrrigation(id, request));
  }

  @PutMapping("/{id}/irrigation-events/{eventId}")
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateIrrigation(
      @PathVariable long id,
      @PathVariable long eventId,
      @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.updateIrrigation(id, eventId, request));
  }
}
