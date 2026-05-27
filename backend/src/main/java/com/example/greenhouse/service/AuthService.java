package com.example.greenhouse.service;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.UserRole;
import com.example.greenhouse.repository.AppUserRepository;
import com.example.greenhouse.web.dto.LoginRequest;
import com.example.greenhouse.web.dto.UserCreateRequest;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Authentication service backed by the app_user PostgreSQL table. */
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;

  public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public AppUser login(LoginRequest request) {
    return users.findByEmail(request.email())
        .map(existing -> authenticateExisting(existing, request))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  @Transactional
  public AppUser syncGoogleUser(OAuth2User principal) {
    String email = principal.getAttribute("email");
    if (email == null || email.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account has no email");
    }

    String name = principal.getAttribute("name");
    String fullName = name == null || name.isBlank() ? email : name;
    AppUser user = users.findByEmail(email)
        .map(existing -> updateGoogleUser(existing, fullName))
        .orElseGet(() -> createGoogleUser(email, fullName));
    log.info("Synced Google user: {}", email);
    return user;
  }

  @Transactional(readOnly = true)
  public Optional<AppUser> findByEmail(String email) {
    return users.findByEmail(email);
  }

  @Transactional
  public AppUser register(UserCreateRequest request) {
    users.findByEmail(request.email()).ifPresent(existing -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    });
    AppUser user = new AppUser();
    user.email = request.email();
    user.fullName = request.fullName();
    user.passwordHash = passwordEncoder.encode(request.password());
    user.provider = "email";
    user.role = request.role() != null ? request.role() : UserRole.VIEWER;
    AppUser saved = users.save(user);
    log.info("Registered user: {}", saved.email);
    return saved;
  }

  @Transactional
  public void verifyEmail(String email) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.verified = true;
    users.save(user);
    log.info("Verified email: {}", email);
  }

  @Transactional
  public void resetPassword(String email, String newPassword) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.passwordHash = passwordEncoder.encode(newPassword);
    users.save(user);
    log.info("Reset password for: {}", email);
  }

  private AppUser authenticateExisting(AppUser user, LoginRequest request) {
    if (!passwordEncoder.matches(request.password(), user.passwordHash)) {
      log.warn("Failed login attempt for: {}", request.email());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    log.info("Successful login: {}", request.email());
    return user;
  }

  private AppUser updateGoogleUser(AppUser user, String fullName) {
    user.fullName = fullName;
    user.provider = "google";
    if (user.role == null) {
      user.role = UserRole.OPERATOR;
    }
    return users.save(user);
  }

  private AppUser createGoogleUser(String email, String fullName) {
    AppUser user = new AppUser();
    user.email = email;
    user.fullName = fullName;
    user.passwordHash = passwordEncoder.encode("oauth2-google");
    user.provider = "google";
    user.role = UserRole.OPERATOR;
    return users.save(user);
  }
}
