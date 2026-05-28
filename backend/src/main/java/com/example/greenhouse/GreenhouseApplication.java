package com.example.greenhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point for the greenhouse management API. */
@SpringBootApplication
@EnableScheduling
public class GreenhouseApplication {
  public static void main(String[] args) {
    SpringApplication.run(GreenhouseApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onReady(ApplicationReadyEvent event) {
    Environment env = event.getApplicationContext().getEnvironment();
    String port = env.getProperty("server.port", "8080");
    String activeProfiles = String.join(", ", env.getActiveProfiles());
    String frontendUrl = env.getProperty("app.frontend-url", "http://localhost:5173");
    log.info("============================================================");
    log.info("GreenHouse API iniciado");
    log.info("Puerto: {}", port);
    log.info("Perfiles activos: [{}]", activeProfiles.isBlank() ? "default" : activeProfiles);
    log.info("Frontend URL: {}", frontendUrl);
    log.info("Health: http://localhost:{}/api/health", port);
    log.info("Actuator: http://localhost:{}/actuator", port);
    log.info("Swagger: http://localhost:{}/swagger-ui.html", port);
    log.info("============================================================");
  }
}
