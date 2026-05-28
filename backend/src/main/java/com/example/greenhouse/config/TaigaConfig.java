package com.example.greenhouse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Configuration
public class TaigaConfig {

  private static final Logger log = LoggerFactory.getLogger(TaigaConfig.class);

  private final String apiUrl;
  private final String token;
  private final Integer projectId;
  private final int connectTimeout;
  private final int readTimeout;
  private RestTemplate restTemplate;

  public TaigaConfig(
      @Value("${app.taiga.api-url:https://api.taiga.io/api/v1}") String apiUrl,
      @Value("${app.taiga.token:}") String token,
      @Value("${app.taiga.project-id:}") Integer projectId,
      @Value("${app.taiga.connect-timeout-ms:5000}") int connectTimeout,
      @Value("${app.taiga.read-timeout-ms:10000}") int readTimeout) {
    this.apiUrl = apiUrl;
    this.token = token;
    this.projectId = projectId;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
  }

  @PostConstruct
  void init() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofMillis(connectTimeout).toMillis());
    factory.setReadTimeout((int) Duration.ofMillis(readTimeout).toMillis());
    this.restTemplate = new RestTemplate(factory);
    if (token == null || token.isBlank()) {
      log.warn("Taiga token no configurado — el servicio funcionara en modo degradado");
    } else {
      log.info("Taiga configurado: url={}, projectId={}", apiUrl, projectId);
    }
  }

  public String getApiUrl() { return apiUrl; }
  public String getToken() { return token; }
  public Integer getProjectId() { return projectId; }
  public RestTemplate getRestTemplate() { return restTemplate; }

  public boolean isEnabled() {
    return token != null && !token.isBlank() && projectId != null;
  }
}
