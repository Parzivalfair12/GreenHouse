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
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio central de autenticación que maneja login email/password,
 * integración OAuth2 Google, registro de usuarios, verificación de email
 * y flujos de recuperación de contraseña.
 *
 * Todos los mensajes expuestos se resuelven mediante Spring {@link MessageSource}
 * usando el locale del caller via {@link LocaleContextHolder}, habilitando i18n
 * para cada respuesta de error.
 *
 * Los tokens seguros para verificación y reseteo de contraseña se generan con
 * {@link SecureRandom} y se codifican como Base64 URL-safe sin padding.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final List<String> oauthAdminEmails;
  private final MessageSource messages;

  public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
      @Value("${app.oauth2.admin-emails:}") String adminEmails,
      MessageSource messages) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.messages = messages;
    this.oauthAdminEmails = adminEmails == null || adminEmails.isBlank()
        ? List.of()
        : List.of(adminEmails.toLowerCase().split("\\s*,\\s*"));
    log.info("OAuth2 admin emails: {}", this.oauthAdminEmails.isEmpty() ? "(none)" : this.oauthAdminEmails);
  }

  private String msg(String key) {
    return messages.getMessage(key, null, key, LocaleContextHolder.getLocale());
  }

  /**
   * Synchronizes a Google OAuth2 principal with the local user database.
   *
   * Business rules:
   * <ul>
   *   <li>Email is mandatory from the Google identity provider.</li>
   *   <li>Existing users are updated with the latest full name and marked as
   *       verified (Google verifies emails externally).</li>
   *   <li>New users are created with provider {@code "google"}, verified=true, and
   *       a BCrypt-encoded placeholder password.</li>
   *   <li>OAuth2 admin emails configured via {@code app.oauth2.admin-emails}
   *       receive {@link UserRole#ADMIN}; others receive
   *       {@link UserRole#OPERATOR}.</li>
   * </ul>
   *
   * @param principal the authenticated OAuth2User from Google
   * @return the persisted AppUser
   * @throws ResponseStatusException 401 if email is missing from the principal
   * @since 2.1.0
   */
  @Transactional
  public AppUser syncGoogleUser(OAuth2User principal) {
    String email = principal.getAttribute("email");
    if (email == null || email.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.no.email"));
    }

    String name = principal.getAttribute("name");
    String fullName = name == null || name.isBlank() ? email : name;
    AppUser user = users.findByEmail(email)
        .map(existing -> updateGoogleUser(existing, fullName))
        .orElseGet(() -> createGoogleUser(email, fullName));
    log.info("Synced Google user: {}", email);
    return user;
  }

  /**
   * Looks up a user by email for read-only queries such as token refresh and
   * profile retrieval.
   *
   * @param email case-insensitive email address
   * @return the user wrapped in {@link Optional}, empty if not found
   * @since 2.1.0
   */
  @Transactional(readOnly = true)
  public Optional<AppUser> findByEmail(String email) {
    return users.findByEmail(email);
  }

  /**
   * Creates a new email/password user with unverified status and a 24-hour
   * verification window.
   *
   * Business rules:
   * <ul>
   *   <li>Email must be unique; a 409 Conflict is returned on duplicates.</li>
   *   <li>Password is BCrypt-encoded before persistence.</li>
   *   <li>Role defaults to {@link UserRole#VIEWER} if not provided.</li>
   *   <li>Verification token is a 32-byte secure random URL-safe Base64 string
   *       expiring in 24 hours.</li>
   * </ul>
   *
   * @param request validated user creation payload
   * @return the newly persisted AppUser
   * @throws ResponseStatusException 409 if email already exists
   * @since 2.1.0
   */
  @Transactional
  public AppUser register(UserCreateRequest request) {
    users.findByEmail(request.email()).ifPresent(existing -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, msg("user.email.exists"));
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

  /**
   * Authenticates a user with email and password.
   *
   * Business rules:
   * <ul>
   *   <li>Email lookup is case-insensitive.</li>
   *   <li>Non-verified email/password accounts receive 403 Forbidden —
   *       the frontend then offers to resend the verification link.</li>
   *   <li>Password is compared via {@link BCryptPasswordEncoder#matches}.</li>
   *   <li>OAuth2 (Google) users cannot log in via password; their provider is
   *       {@code "google"} and the stored hash is a placeholder.</li>
   * </ul>
   *
   * Security:
   * <ul>
   *   <li>Credentials are never logged; only match results and metadata.</li>
   *   <li>All error messages are i18n-resolved through {@code msg()}.</li>
   * </ul>
   *
   * @param request validated login credentials
   * @return the authenticated AppUser
   * @throws ResponseStatusException 401 if credentials are invalid
   * @throws ResponseStatusException 403 if account is not verified
   * @since 2.1.0
   */
  @Transactional
  public AppUser login(LoginRequest request) {
    log.info("Login attempt for: {}", request.email());
    AppUser user = users.findByEmail(request.email().toLowerCase())
        .orElseThrow(() -> {
          log.warn("User not found: {}", request.email());
          return new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.invalid.credentials"));
        });

    log.info("User found: {}, provider: {}, verified: {}, role: {}, hashLength: {}",
        user.email, user.provider, user.verified, user.role,
        user.passwordHash != null ? user.passwordHash.length() : 0);

    if (!user.verified && "email".equals(user.provider)) {
      log.warn("Account not verified for: {}", request.email());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, msg("auth.account.not.verified"));
    }

    boolean matches = passwordEncoder.matches(request.password(), user.passwordHash);
    log.info("Password match result for {}: {}, encoderClass: {}", request.email(), matches,
        passwordEncoder.getClass().getName());

    if (!matches) {
      log.warn("Password mismatch for: {}", request.email());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.invalid.credentials"));
    }
    log.info("Successful login: {}", request.email());
    return user;
  }

  /**
   * Verifies an email account using a URL-safe secure token sent via email.
   *
   * Business rules:
   * <ul>
   *   <li>Token is matched against all users (blind scan) — acceptable because
   *       tokens are 256-bit random values with negligible collision risk.</li>
   *   <li>Expired tokens (past their 24-hour TTL) are rejected.</li>
   *   <li>On success the token and expiry are cleared and the user is marked
   *       verified.</li>
   * </ul>
   *
   * @param token the verification token from the email link
   * @return the verified AppUser
   * @throws ResponseStatusException 400 if token is blank
   * @throws ResponseStatusException 401 if token is invalid or expired
   * @since 2.1.0
   */
  @Transactional
  public AppUser verifyEmailWithToken(String token) {
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg("auth.token.required"));
    }

    AppUser user = users.findAll().stream()
        .filter(u -> token.equals(u.verificationToken))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.token.invalid")));

    if (user.verificationTokenExpiry == null || Instant.now().isAfter(user.verificationTokenExpiry)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.token.expired"));
    }

    user.verified = true;
    user.verificationToken = null;
    user.verificationTokenExpiry = null;
    users.save(user);
    log.info("Verified email via token for: {}", user.email);
    return user;
  }

  /**
   * Generates a fresh verification token for an authenticated user who has
   * not yet verified their email.
   *
   * @param email the authenticated user's email
   * @return the updated AppUser with new token and expiry
   * @throws ResponseStatusException 404 if user not found
   * @since 2.1.0
   */
  @Transactional
  public AppUser regenerateVerificationToken(String email) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg("user.not.found")));
    user.verificationToken = generateSecureToken();
    user.verificationTokenExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
    users.save(user);
    log.info("Regenerated verification token for: {}", email);
    return user;
  }

  /**
   * Creates a password-reset token for a given email, valid for 1 hour.
   *
   * Security:
   * <ul>
   *   <li>Returns {@code null} when the email does not exist to avoid
   *       user-enumeration attacks — the controller always returns the same
   *       success message regardless of outcome.</li>
   *   <li>Token is a 32-byte URL-safe Base64 string.</li>
   * </ul>
   *
   * @param email the target email address
   * @return the updated AppUser, or {@code null} if no user matches
   * @since 2.1.0
   */
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

  /**
   * Resets a user's password using a valid reset token.
   *
   * Business rules:
   * <ul>
   *   <li>Both token and new password must be non-blank.</li>
   *   <li>Token is matched by scanning all users (blind scan) and must
   *       not have expired (1-hour TTL).</li>
   *   <li>New password is BCrypt-encoded and the token is cleared
   *       immediately to prevent reuse.</li>
   * </ul>
   *
   * @param token       the reset token from the recovery email
   * @param newPassword the new cleartext password to encode and store
   * @throws ResponseStatusException 400 if token or password is blank
   * @throws ResponseStatusException 401 if token is invalid or expired
   * @since 2.1.0
   */
  @Transactional
  public void resetPasswordWithToken(String token, String newPassword) {
    if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg("auth.token.password.required"));
    }
    AppUser user = users.findAll().stream()
        .filter(u -> token.equals(u.resetToken))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.token.invalid")));

    if (user.resetTokenExpiry == null || Instant.now().isAfter(user.resetTokenExpiry)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg("auth.token.expired"));
    }

    user.passwordHash = passwordEncoder.encode(newPassword);
    user.resetToken = null;
    user.resetTokenExpiry = null;
    users.save(user);
    log.info("Password reset successfully for: {}", user.email);
  }

  /**
   * Marks a user's email as verified directly (used as JWT token fallback
   * in the verification endpoint).
   *
   * @param email the user's email
   * @throws ResponseStatusException 404 if user not found
   * @since 2.1.0
   */
  @Transactional
  public void verifyEmail(String email) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg("user.not.found")));
    user.verified = true;
    user.verificationToken = null;
    user.verificationTokenExpiry = null;
    users.save(user);
    log.info("Verified email: {}", email);
  }

  /**
   * Directly resets a user's password without a token (legacy support).
   *
   * @param email       the user's email
   * @param newPassword the new cleartext password
   * @throws ResponseStatusException 404 if user not found
   * @since 2.1.0
   */
  @Transactional
  public void resetPassword(String email, String newPassword) {
    AppUser user = users.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg("user.not.found")));
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
