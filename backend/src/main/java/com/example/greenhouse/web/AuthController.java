package com.example.greenhouse.web;

import com.example.greenhouse.config.JwtTokenProvider;
import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.service.AuthService;
import com.example.greenhouse.service.EmailService;
import com.example.greenhouse.web.dto.LoginRequest;
import com.example.greenhouse.web.dto.LoginResponse;
import com.example.greenhouse.web.dto.UserCreateRequest;
import com.example.greenhouse.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Authentication endpoints that persist demo users in PostgreSQL. */
@Tag(name = "Autenticacion", description = "Endpoints de login, registro y perfil de usuario")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;
  private final long expirationMs;
  private final MessageSource messages;
  private final boolean emailEnabled;

  public AuthController(AuthService service, JwtTokenProvider jwtTokenProvider,
      EmailService emailService,
      @Value("${app.jwt-expiration-ms}") long expirationMs,
      @Value("${greenhouse.email.enabled:false}") boolean emailEnabled,
      MessageSource messages) {
    this.service = service;
    this.jwtTokenProvider = jwtTokenProvider;
    this.emailService = emailService;
    this.expirationMs = expirationMs;
    this.emailEnabled = emailEnabled;
    this.messages = messages;
  }

  @Operation(summary = "Iniciar sesion", description = "Autentica con email y password, devuelve JWT")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado"),
      @ApiResponse(responseCode = "401", description = "Credenciales invalidas"),
      @ApiResponse(responseCode = "403", description = "Cuenta no verificada")
  })
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    AppUser user = service.login(request);
    String token = jwtTokenProvider.generateToken(user);
    return new LoginResponse(token, user.email, user.fullName,
        List.of("ROLE_" + user.role.name()), expirationMs / 1000, user.verified);
  }

  @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en la base de datos y envia correo de verificacion")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
      @ApiResponse(responseCode = "409", description = "El correo ya existe")
  })
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody UserCreateRequest request) {
    AppUser user = service.register(request);
    if (emailEnabled) {
      emailService.sendVerificationEmail(user.email, user.fullName, user.verificationToken);
      return ResponseEntity.ok(Map.of(
          "message", "Usuario registrado. Revisa tu correo para verificar tu cuenta.",
          "email", user.email));
    }
    // Fallback for academic/demo environments without SMTP configured
    return ResponseEntity.ok(Map.of(
        "message", "Usuario registrado (modo desarrollo: email no configurado). Verifica tu cuenta con el token.",
        "email", user.email,
        "token", user.verificationToken));
  }

  @Operation(summary = "Refrescar token", description = "Extiende la validez del JWT actual")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Nuevo token JWT generado"),
      @ApiResponse(responseCode = "401", description = "Token invalido o expirado")
  })
  @PostMapping("/refresh")
  @PreAuthorize("isAuthenticated()")
  public LoginResponse refresh(Authentication authentication) {
    String email = authentication.getName();
    AppUser user = service.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    String token = jwtTokenProvider.generateToken(user);
    return new LoginResponse(token, user.email, user.fullName,
        List.of("ROLE_" + user.role.name()), expirationMs / 1000, user.verified);
  }

  @Operation(summary = "Verificar email (GET)", description = "Marca el email como verificado usando un token desde un enlace")
  @GetMapping("/verify-email")
  public Map<String, String> verifyEmailGet(@RequestParam("token") String token) {
    service.verifyEmailWithToken(token);
    return Map.of("message", "Email verificado correctamente");
  }

  @Operation(summary = "Verificar email (POST)", description = "Marca el email como verificado usando un token JWT o secure token")
  @PostMapping("/verify")
  public Map<String, String> verifyEmailPost(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
    }
    // Try secure token first
    try {
      service.verifyEmailWithToken(token);
      return Map.of("message", "Email verificado correctamente");
    } catch (ResponseStatusException ex) {
      // Fallback to JWT token for backwards compatibility
      if (!jwtTokenProvider.validateToken(token)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
      }
      String email = jwtTokenProvider.getEmailFromToken(token);
      service.verifyEmail(email);
      return Map.of("message", "Email verificado correctamente");
    }
  }

  @Operation(summary = "Reenviar verificacion", description = "Genera un nuevo token de verificacion y lo envia por email")
  @PostMapping("/resend-verification")
  @PreAuthorize("isAuthenticated()")
  public Map<String, String> resendVerification(Authentication authentication) {
    String email = authentication.getName();
    AppUser user = service.regenerateVerificationToken(email);
    if (emailEnabled) {
      emailService.sendVerificationEmail(user.email, user.fullName, user.verificationToken);
      return Map.of("message", "Correo de verificacion enviado");
    }
    return Map.of("message", "Token de verificacion regenerado (modo desarrollo)", "token", user.verificationToken);
  }

  @Operation(summary = "Solicitar recuperacion de contrasena", description = "Genera un token de recuperacion y lo envia por email")
  @PostMapping("/forgot-password")
  public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    if (email == null || email.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
    }
    AppUser user = service.createPasswordResetToken(email);
    if (user == null) {
      // Do not reveal whether email exists
      return Map.of("message", "Si el correo existe, se ha enviado un enlace de recuperacion");
    }
    if (emailEnabled) {
      emailService.sendPasswordResetEmail(user.email, user.fullName, user.resetToken);
      return Map.of("message", "Si el correo existe, se ha enviado un enlace de recuperacion");
    }
    // Fallback for academic/demo environments
    return Map.of("message", "Token de recuperacion generado (modo desarrollo: email no configurado)", "token", user.resetToken);
  }

  @Operation(summary = "Restablecer contrasena", description = "Usa el token de recuperacion para cambiar la contrasena")
  @PostMapping("/reset-password")
  public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    String newPassword = body.get("password");
    if (token == null || newPassword == null || newPassword.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token y password requeridos");
    }
    if (newPassword.length() < 4) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena debe tener al menos 4 caracteres");
    }
    service.resetPasswordWithToken(token, newPassword);
    return Map.of("message", "Contrasena restablecida correctamente");
  }

  @Operation(summary = "Perfil actual", description = "Devuelve los datos del usuario autenticado (JWT u OAuth2)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Datos del usuario"),
      @ApiResponse(responseCode = "401", description = "No autenticado")
  })
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public UserResponse currentUser(Authentication authentication, Locale locale) {
    if (authentication == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, messages.getMessage("auth.unauthorized", null, locale));
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof OAuth2User oauth2User) {
      return UserResponse.from(service.syncGoogleUser(oauth2User));
    }
    if (principal instanceof String email) {
      return service.findByEmail(email)
          .map(UserResponse::from)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
              messages.getMessage("auth.user.not.found", null, locale)));
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
        messages.getMessage("auth.unsupported", null, locale));
  }
}
