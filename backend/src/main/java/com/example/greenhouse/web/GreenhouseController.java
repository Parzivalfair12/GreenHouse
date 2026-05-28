package com.example.greenhouse.web;

import com.example.greenhouse.service.GreenhouseService;
import com.example.greenhouse.web.dto.CropRequest;
import com.example.greenhouse.web.dto.GreenhouseRequest;
import com.example.greenhouse.web.dto.GreenhouseResponse;
import com.example.greenhouse.web.dto.IrrigationRequest;
import com.example.greenhouse.web.dto.SensorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión completa de invernaderos, incluyendo
 * cultivos, sensores y eventos de riego asociados.
 *
 * Seguridad:
 * <ul>
 *   <li>Lectura: cualquier usuario autenticado.</li>
 *   <li>Escritura: solo ADMIN (crear, actualizar, eliminar invernaderos).</li>
 *   <li>Operaciones sobre hijos (cultivos, sensores, riegos): ADMIN.</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Tag(name = "Invernaderos", description = "Gestion de invernaderos, cultivos, sensores y riegos")
@RestController
@RequestMapping("/api/greenhouses")
public class GreenhouseController {
  private final GreenhouseService service;

  public GreenhouseController(GreenhouseService service) {
    this.service = service;
  }

  @Operation(summary = "Listar invernaderos", description = "Devuelve todos los invernaderos activos")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de invernaderos"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<GreenhouseResponse> findAll() {
    return service.findAll().stream().map(GreenhouseResponse::from).toList();
  }

  @Operation(summary = "Obtener invernadero", description = "Devuelve un invernadero por su ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Invernadero encontrado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Invernadero Central\",\"location\":\"Zona Norte\",\"areaSquareMeters\":500.00,\"active\":true,\"cropCount\":2,\"sensorCount\":4,\"crops\":[{\"id\":1,\"name\":\"Tomate\",\"variety\":\"Cherry\",\"status\":\"GROWING\",\"plantedAt\":\"2026-01-15\",\"expectedHarvestAt\":\"2026-04-15\"}],\"sensors\":[{\"id\":1,\"code\":\"TEMP-001\",\"type\":\"TEMPERATURE\",\"unit\":\"°C\",\"minThreshold\":10.0,\"maxThreshold\":35.0}],\"irrigationEvents\":[{\"id\":1,\"durationMinutes\":30,\"waterLiters\":150.00,\"mode\":\"AUTOMATIC\"}]}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public GreenhouseResponse findById(@Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id) {
    return GreenhouseResponse.from(service.findById(id));
  }

  @Operation(summary = "Crear invernadero", description = "Crea un nuevo invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Invernadero creado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":2,\"name\":\"Invernadero Nuevo\",\"location\":\"Zona Sur\",\"areaSquareMeters\":350.00,\"active\":true,\"cropCount\":0,\"sensorCount\":0,\"crops\":[],\"sensors\":[],\"irrigationEvents\":[]}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del invernadero", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"name\":\"Invernadero Nuevo\",\"location\":\"Zona Sur\",\"areaSquareMeters\":350.00,\"active\":true}")))
      @Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.create(request));
  }

  @Operation(summary = "Actualizar invernadero", description = "Modifica un invernadero existente (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Invernadero actualizado"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse update(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del invernadero", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"name\":\"Invernadero Modificado\",\"location\":\"Zona Este\",\"areaSquareMeters\":400.00,\"active\":false}")))
      @Valid @RequestBody GreenhouseRequest request) {
    return GreenhouseResponse.from(service.update(id, request));
  }

  @Operation(summary = "Eliminar invernadero", description = "Elimina un invernadero y todos sus datos asociados (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Invernadero eliminado (sin contenido)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id) {
    service.delete(id);
  }

  @Operation(summary = "Agregar cultivo", description = "Agrega un nuevo cultivo a un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cultivo agregado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/{id}/crops")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addCrop(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del cultivo", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"name\":\"Tomate\",\"variety\":\"Cherry\",\"plantedAt\":\"2026-03-01\",\"expectedHarvestAt\":\"2026-06-01\"}")))
      @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.addCrop(id, request));
  }

  @Operation(summary = "Actualizar cultivo", description = "Modifica un cultivo existente en un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Cultivo actualizado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o cultivo no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PutMapping("/{id}/crops/{cropId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateCrop(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del cultivo", required = true, example = "1") @PathVariable long cropId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del cultivo", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"name\":\"Tomate\",\"variety\":\"Roma\",\"plantedAt\":\"2026-03-01\",\"expectedHarvestAt\":\"2026-06-15\"}")))
      @Valid @RequestBody CropRequest request) {
    return GreenhouseResponse.from(service.updateCrop(id, cropId, request));
  }

  @Operation(summary = "Eliminar cultivo", description = "Elimina un cultivo de un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Cultivo eliminado (sin contenido)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o cultivo no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @DeleteMapping("/{id}/crops/{cropId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteCrop(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del cultivo", required = true, example = "1") @PathVariable long cropId) {
    service.deleteCrop(id, cropId);
  }

  @Operation(summary = "Agregar sensor", description = "Agrega un nuevo sensor a un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sensor agregado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/{id}/sensors")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addSensor(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del sensor", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"code\":\"HUM-002\",\"type\":\"HUMIDITY\",\"unit\":\"%\",\"minThreshold\":30.0,\"maxThreshold\":80.0,\"greenhouseId\":1}")))
      @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.addSensor(id, request));
  }

  @Operation(summary = "Actualizar sensor", description = "Modifica un sensor existente en un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Sensor actualizado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o sensor no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PutMapping("/{id}/sensors/{sensorId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateSensor(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del sensor", required = true, example = "1") @PathVariable long sensorId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del sensor", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"code\":\"HUM-002\",\"type\":\"HUMIDITY\",\"unit\":\"%\",\"minThreshold\":25.0,\"maxThreshold\":85.0,\"greenhouseId\":1}")))
      @Valid @RequestBody SensorRequest request) {
    return GreenhouseResponse.from(service.updateSensor(id, sensorId, request));
  }

  @Operation(summary = "Eliminar sensor", description = "Elimina un sensor de un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Sensor eliminado (sin contenido)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o sensor no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @DeleteMapping("/{id}/sensors/{sensorId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteSensor(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del sensor", required = true, example = "1") @PathVariable long sensorId) {
    service.deleteSensor(id, sensorId);
  }

  @Operation(summary = "Agregar evento de riego", description = "Registra un nuevo evento de riego en un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Evento de riego agregado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/{id}/irrigation-events")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse addIrrigation(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del evento de riego", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"durationMinutes\":30,\"waterLiters\":150.00,\"mode\":\"MANUAL\"}")))
      @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.addIrrigation(id, request));
  }

  @Operation(summary = "Actualizar evento de riego", description = "Modifica un evento de riego existente (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Evento de riego actualizado"),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o evento no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PutMapping("/{id}/irrigation-events/{eventId}")
  @PreAuthorize("hasRole('ADMIN')")
  public GreenhouseResponse updateIrrigation(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del evento de riego", required = true, example = "1") @PathVariable long eventId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del evento de riego", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"durationMinutes\":45,\"waterLiters\":200.00,\"mode\":\"AUTOMATIC\"}")))
      @Valid @RequestBody IrrigationRequest request) {
    return GreenhouseResponse.from(service.updateIrrigation(id, eventId, request));
  }

  @Operation(summary = "Eliminar evento de riego", description = "Elimina un evento de riego de un invernadero (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Evento de riego eliminado (sin contenido)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Invernadero o evento no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @DeleteMapping("/{id}/irrigation-events/{eventId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteIrrigation(
      @Parameter(description = "ID del invernadero", required = true, example = "1") @PathVariable long id,
      @Parameter(description = "ID del evento de riego", required = true, example = "1") @PathVariable long eventId) {
    service.deleteIrrigation(id, eventId);
  }
}
