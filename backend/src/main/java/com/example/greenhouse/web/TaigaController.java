package com.example.greenhouse.web;

import com.example.greenhouse.service.GitTraceabilityService;
import com.example.greenhouse.service.StoryGenerationService;
import com.example.greenhouse.service.TaigaService;
import com.example.greenhouse.web.dto.TaigaCommentRequest;
import com.example.greenhouse.web.dto.TaigaStatusRequest;
import com.example.greenhouse.web.dto.TaigaSyncResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Taiga", description = "Integracion real con Taiga — historias de usuario, epics, tasks y trazabilidad")
@RestController
@RequestMapping("/api/taiga")
public class TaigaController {

  private final TaigaService taigaService;
  private final StoryGenerationService storyGen;
  private final GitTraceabilityService gitTrace;

  public TaigaController(TaigaService taigaService, StoryGenerationService storyGen, GitTraceabilityService gitTrace) {
    this.taigaService = taigaService;
    this.storyGen = storyGen;
    this.gitTrace = gitTrace;
  }

  @Operation(summary = "Listar historias desde Taiga", description = "Obtiene las historias de usuario reales desde la API de Taiga.io")
  @GetMapping("/stories")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> listStories() {
    return taigaService.listStories();
  }

  @Operation(summary = "Listar epics desde Taiga", description = "Obtiene los epics del proyecto desde Taiga.io")
  @GetMapping("/epics")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> listEpics() {
    return taigaService.listEpics();
  }

  @Operation(summary = "Obtener historia por ID", description = "Devuelve una historia de usuario especifica desde Taiga")
  @GetMapping("/stories/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, Object>> getStory(@PathVariable int id) {
    Map<String, Object> story = taigaService.getStory(id);
    if (story == null || story.isEmpty()) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(story);
  }

  @Operation(summary = "Listar tareas de una historia", description = "Obtiene las tareas asociadas a una historia de usuario")
  @GetMapping("/stories/{id}/tasks")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> listTasks(@PathVariable int id) {
    return taigaService.listTasks(id);
  }

  @Operation(summary = "Actualizar estado de historia", description = "Cambia el estado de una historia en Taiga (new, in_progress, ready_for_test, done)")
  @PatchMapping("/stories/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable int id, @RequestBody TaigaStatusRequest request) {
    Map<String, Object> result = taigaService.updateStoryStatus(id, request);
    if (result == null || result.isEmpty()) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "Agregar comentario a historia", description = "Agrega un comentario a una historia de usuario en Taiga")
  @PostMapping("/stories/{id}/comment")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> addComment(@PathVariable int id, @RequestBody TaigaCommentRequest request) {
    Map<String, Object> result = taigaService.addComment(id, request);
    if (result == null || result.isEmpty()) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "Resumen del proyecto Taiga", description = "Estadisticas de completitud del proyecto desde Taiga.io")
  @GetMapping("/summary")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> summary() {
    return taigaService.summary();
  }

  @Operation(summary = "Historias generadas desde codigo", description = "Genera automaticamente historias de usuario analizando los controllers reales del proyecto via ApplicationContext")
  @GetMapping("/generated-stories")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> generatedStories() {
    return storyGen.generateStoriesFromCode();
  }

  @Operation(summary = "Sincronizar historias con Taiga", description = "Sincroniza las historias generadas desde el codigo con Taiga.io (crea/actualiza via API REST real)")
  @PostMapping("/sync")
  @PreAuthorize("hasRole('ADMIN')")
  public TaigaSyncResponse syncStories() {
    return taigaService.syncStories(storyGen.generateStoriesFromCode());
  }

  @Operation(summary = "Matriz de trazabilidad", description = "Matriz de trazabilidad entre commits e historias de usuario")
  @GetMapping("/traceability")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> traceability() {
    return gitTrace.buildTraceabilityMatrix();
  }

  @Operation(summary = "Historial de commits", description = "Historial de commits del proyecto con deteccion de referencias a historias (US#)")
  @GetMapping("/commits")
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> commits() {
    return gitTrace.getFullHistory();
  }

  @Operation(summary = "Estadisticas de trazabilidad", description = "Estadisticas de commits con/sin historias de usuario asociadas")
  @GetMapping("/traceability/stats")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> traceabilityStats() {
    return gitTrace.stats();
  }
}
