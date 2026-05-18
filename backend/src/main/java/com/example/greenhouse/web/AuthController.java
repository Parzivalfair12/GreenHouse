package com.example.greenhouse.web;

import com.example.greenhouse.service.AuthService;
import com.example.greenhouse.web.dto.LoginRequest;
import com.example.greenhouse.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Authentication endpoints that persist demo users in PostgreSQL. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public UserResponse login(@Valid @RequestBody LoginRequest request) {
    return UserResponse.from(service.login(request));
  }

  @GetMapping("/me")
  public UserResponse currentUser(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No OAuth2 session");
    }
    return UserResponse.from(service.syncGoogleUser(principal));
  }
}
