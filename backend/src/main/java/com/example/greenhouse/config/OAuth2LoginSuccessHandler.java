package com.example.greenhouse.config;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.service.AuthService;
import jakarta.servlet.http.Cookie;
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

/** Stores authenticated Google users in PostgreSQL and returns via secure cookie. */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final String frontendUrl;

  public OAuth2LoginSuccessHandler(
      AuthService authService,
      JwtTokenProvider jwtTokenProvider,
      @Value("${app.frontend-url}") String frontendUrl) {
    this.authService = authService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.frontendUrl = frontendUrl;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {
    AppUser user = null;
    if (authentication.getPrincipal() instanceof OAuth2User principal) {
      user = authService.syncGoogleUser(principal);
    }
    if (user != null) {
      String token = jwtTokenProvider.generateToken(user);
      Cookie cookie = new Cookie("jwt", token);
      cookie.setHttpOnly(true);
      cookie.setSecure(false); // Set to true in production with HTTPS
      cookie.setPath("/");
      cookie.setMaxAge(86400); // 24 hours
      response.addCookie(cookie);
      response.sendRedirect(frontendUrl + "?oauth=google&status=success");
    } else {
      response.sendRedirect(frontendUrl + "?oauth=google&status=error&message=No+se+pudo+autenticar");
    }
  }

  public String failureUrl() {
    String message = URLEncoder.encode("No se pudo autenticar con Google", StandardCharsets.UTF_8);
    return frontendUrl + "?oauth=google&status=error&message=" + message;
  }
}
