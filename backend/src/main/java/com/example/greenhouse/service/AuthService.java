package com.example.greenhouse.service;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.UserRole;
import com.example.greenhouse.repository.AppUserRepository;
import com.example.greenhouse.web.dto.LoginRequest;
import com.example.greenhouse.web.dto.UserCreateRequest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Authentication service backed by the app_user PostgreSQL table. */
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final List<String> oauthAdminEmails;

  public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
      @Value("${app.oauth2.admin-emails:}") String adminEmails) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.oauthAdminEmails = adminEmails == null || adminEmails.isBlank()
        ? List.of()
        : List.of(adminEmails.toLowerCase().split("\\s*,\\s*"));
    log.info("OAuth2 admin emails: {}", this.oauthAdminEmails.isEmpty() ? "(none)" : this.oauthAdminEmails);
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
    user.verified = false;
    user.verificationToken = generateSecureToken();
    user.verificationTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
    AppUser saved = users.save(user);
    log.info("Registered user: {} with verification token", saved.email);
    return saved;
  }

  @Transactional
  public AppUser login(LoginRequest request) {
    log.info("Login attempt for: {}", request.email());
    AppUser user = users.findByEmail(request.email().toLowerCase())
        .orElseThrow(() -> {
          log.warn("User not found: {}", request.email());
          return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        });

    log.info("User found: {}, provider: {}, verified: {}, role: {}, hashLength: {}",
        user.email, user.provider, user.verified, user.role,
        user.passwordHash != null ? user.passwordHash.length() : 0);

    if (!user.verified && "email".equals(user.provider)) {
      log.warn("Account not verified for: {}", request.email());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no verificada");
    }

    boolean matches = passwordEncoder.matches(request.password(), user.passwordHash);
    log.info("Password match result for {}: {}, encoderClass: {}", request.email(), matches,
        passwordEncoder.getClass().getName());

    if (!matches) {
      log.warn("Password mismatch for: {}", request.email());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }
    log.info("Successful login: {}", request.email());
    return user;
  }

  @Transactional
  public AppUser verifyEmailWithToken(String token) {
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
    }

    AppUser user = users.findAll().stream()
        .filter(u -> token.equals(u.verificationToken))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado"));

    if (user.verificationTokenExpiry == null || Instant.now().isAfter(user.verificationTokenExpiry)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
    }

    user.verified = true;
    user.verificationToken = null;
    user.verificationTokenExpiry = null;
    users.save(user);
    log.info("Verified email via token for: {}", user.email);
    return user;
  }

  @Transactional
  public AppUser regenerateVerificationToken(String email) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.verificationToken = generateSecureToken();
    user.verificationTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
    users.save(user);
    log.info("Regenerated verification token for: {}", email);
    return user;
  }

  @Transactional
  public AppUser createPasswordResetToken(String email) {
    AppUser user = users.findByEmail(email).orElse(null);
    if (user == null) {
      return null;
    }
    user.resetToken = generateSecureToken();
    user.resetTokenExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
    users.save(user);
    log.info("Created password reset token for: {}", email);
    return user;
  }

  @Transactional
  public void resetPasswordWithToken(String token, String newPassword) {
    if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token y password requeridos");
    }
    AppUser user = users.findAll().stream()
        .filter(u -> token.equals(u.resetToken))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado"));

    if (user.resetTokenExpiry == null || Instant.now().isAfter(user.resetTokenExpiry)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
    }

    user.passwordHash = passwordEncoder.encode(newPassword);
    user.resetToken = null;
    user.resetTokenExpiry = null;
    users.save(user);
    log.info("Password reset successfully for: {}", user.email);
  }

  @Transactional
  public void verifyEmail(String email) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    user.verified = true;
    user.verificationToken = null;
    user.verificationTokenExpiry = null;
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

  private AppUser updateGoogleUser(AppUser user, String fullName) {
    user.fullName = fullName;
    user.provider = "google";
    user.verified = true;
    user.role = resolveOAuthRole(user.email);
    return users.save(user);
  }

  private AppUser createGoogleUser(String email, String fullName) {
    AppUser user = new AppUser();
    user.email = email;
    user.fullName = fullName;
    user.passwordHash = passwordEncoder.encode("oauth2-google");
    user.provider = "google";
    user.role = resolveOAuthRole(email);
    user.verified = true;
    return users.save(user);
  }

  private UserRole resolveOAuthRole(String email) {
    if (email != null && oauthAdminEmails.contains(email.toLowerCase())) {
      log.info("OAuth user {} matched admin list, granting ADMIN role", email);
      return UserRole.ADMIN;
    }
    return UserRole.OPERATOR;
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return BASE64_ENCODER.encodeToString(bytes);
  }
}
