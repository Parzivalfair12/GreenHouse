package com.example.greenhouse.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

class StoryGenerationServiceTest {

  @Test
  void generateStoriesFromCode_returnsStories() {
    ApplicationContext ctx = mock(ApplicationContext.class);
    when(ctx.getBeansWithAnnotation(RestController.class)).thenReturn(Map.of());

    StoryGenerationService service = new StoryGenerationService(ctx);
    List<Map<String, Object>> stories = service.generateStoriesFromCode();

    assertNotNull(stories);
    assertTrue(stories.isEmpty()); // sin controllers no hay historias
  }

  @Test
  void controllerEndpoint_parsesPath() {
    StoryGenerationService.ControllerEndpoint ep =
        new StoryGenerationService.ControllerEndpoint("GET", "/api/test", "Test endpoint", "authenticated");
    assertEquals("GET", ep.method);
    assertEquals("/api/test", ep.fullPath);
    assertEquals("Test endpoint", ep.summary);
    assertEquals("authenticated", ep.roles);
  }
}
