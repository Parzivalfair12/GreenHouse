package com.example.greenhouse.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

  private final Environment env;

  @Value("${app.oauth2.enabled:false}")
  private String oauth2Enabled;

  @Value("${app.oauth2.redirect-uri:}")
  private String oauth2RedirectUri;

  @Value("${app.frontend-url:}")
  private String frontendUrl;

  @Value("${GOOGLE_CLIENT_ID:}")
  private String googleClientId;

  public DebugController(Environment env) {
    this.env = env;
  }

  @GetMapping("/api/debug/env")
  public Map<String, Object> debugEnv() {
    String activeProfile = String.join(", ", env.getActiveProfiles());
    String clientIdPreview = googleClientId.length() > 10
        ? googleClientId.substring(0, 10) + "..."
        : (googleClientId.isBlank() ? "(not set)" : googleClientId);

    return Map.of(
        "activeProfiles", activeProfile.length() > 0 ? activeProfile : "(default)",
        "oauth2.enabled", oauth2Enabled,
        "oauth2.redirect-uri", oauth2RedirectUri.isBlank() ? "(auto-derived from request)" : oauth2RedirectUri,
        "frontend-url", frontendUrl,
        "google.client-id", clientIdPreview,
        "google.client-id.length", googleClientId.length(),
        "app.jwt-secret.set", env.getProperty("app.jwt-secret", "").length() > 0,
        "spring.mail.host", env.getProperty("spring.mail.host", "(not set)"),
        "spring.profiles.active", env.getProperty("spring.profiles.active", "(not set)"),
        "note", "redirect_uri resolves from the incoming request URL when using {baseUrl} placeholder"
    );
  }
}
