package com.example.greenhouse.config;

import com.example.greenhouse.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** Stores authenticated Google users in PostgreSQL before returning to React. */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
  private final AuthService authService;
  private final String frontendUrl;

  public OAuth2LoginSuccessHandler(AuthService authService, @Value("${app.frontend-url}") String frontendUrl) {
    this.authService = authService;
    this.frontendUrl = frontendUrl;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (authentication.getPrincipal() instanceof OAuth2User principal) {
      authService.syncGoogleUser(principal);
    }
    String redirectUrl = frontendUrl + "?oauth=google&status=success";
    response.sendRedirect(redirectUrl);
  }

  public String failureUrl() {
    String message = URLEncoder.encode("No se pudo autenticar con Google", StandardCharsets.UTF_8);
    return frontendUrl + "?oauth=google&status=error&message=" + message;
  }
}
