package com.example.greenhouse.config;

import com.example.greenhouse.service.GitHubActionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduler de sincronizacion automatica con GitHub Actions.
 * Ejecuta sync cada 5 minutos de forma automatica.
 *
 * @author GreenHouse Team
 * @version 2.2.0
 * @since 2.2.0
 */
@Configuration
@EnableScheduling
public class PipelineSyncScheduler {

  private static final Logger log = LoggerFactory.getLogger(PipelineSyncScheduler.class);

  private final GitHubActionsService githubService;

  public PipelineSyncScheduler(GitHubActionsService githubService) {
    this.githubService = githubService;
  }

  @Scheduled(fixedRate = 300_000) // 5 minutos
  public void syncPipelines() {
    if (!githubService.isEnabled()) {
      log.debug("GitHub Actions no configurado — omitiendo sync automatico");
      return;
    }
    log.info("Iniciando sync automatico de GitHub Actions...");
    var result = githubService.syncWorkflowRuns();
    log.info("Sync automatico completado: {}", result.get("message"));
  }
}
