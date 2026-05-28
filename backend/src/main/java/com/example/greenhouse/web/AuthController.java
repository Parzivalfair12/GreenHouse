package com.example.greenhouse.web;

import com.example.greenhouse.config.JwtTokenProvider;
import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.service.AuthService;
import com.example.greenhouse.service.EmailService;
import com.example.greenhouse.web.dto.LoginRequest;
import com.example.greenhouse.web.dto.LoginResponse;
import com.example.greenhouse.web.dto.UserCreateRequest;
import com.example.greenhouse.web.dto.UserProfileUpdateRequest;
import com.example.greenhouse.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador REST de autenticación. Maneja login email/password, registro de
 * usuarios, verificación de email, recuperación de contraseña e integración
 * OAuth2 con Google.
 *
 * Seguridad:
 * <ul>
 *   <li>Endpoints públicos: login, register, forgot-password, reset-password,
 *       verify-email.</li>
 *   <li>Endpoints protegidos: refresh, resend-verification, me (requieren JWT).</li>
 *   <li>Todas las respuestas de error se resuelven mediante {@link MessageSource}
 *       respetando el header Accept-Language.</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;
  private final long expirationMs;
  private final MessageSource messages;
  private final boolean emailEnabled;
  private final ClientRegistrationRepository clientRegistrationRepository;

  public AuthController(AuthService service, JwtTokenProvider jwtTokenProvider,
      EmailService emailService,
      @Value("${app.jwt-expiration-ms}") long expirationMs,
      @Value("${greenhouse.email.enabled:false}") boolean emailEnabled,
      MessageSource messages,
      @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
    this.service = service;
    this.jwtTokenProvider = jwtTokenProvider;
    this.emailService = emailService;
    this.expirationMs = expirationMs;
    this.emailEnabled = emailEnabled;
    this.messages = messages;
    this.clientRegistrationRepository = clientRegistrationRepository;
  }

  /**
   * Autentica al usuario con email y contraseña, retornando un token JWT.
   *
   * Reglas de negocio:
   * <ul>
   *   <li>La cuenta debe estar verificada (email/password).</li>
   *   <li>Usuarios OAuth2 no pueden autenticarse por password.</li>
   *   <li>El token incluye el rol del usuario para control de acceso.</li>
   * </ul>
   *
   * @param request credenciales del usuario
   * @return respuesta con token JWT, email, roles y expiración
   * @since 2.1.0
   */
  /**
   * Devuelve la configuracion de autenticacion disponible en el backend.
   *
   * Permite al frontend detectar dinamicamente si OAuth2 Google esta
   * configurado, evitando depender de variables de entorno en build time.
   */
  @Operation(summary = "Configuracion de auth", description = "Devuelve si OAuth2 esta disponible")
  @ApiResponse(responseCode = "200", description = "Configuracion obtenida",
      content = @Content(mediaType = "application/json",
          examples = @ExampleObject(value = "{\"oauthEnabled\":true}")))
  @GetMapping("/config")
  public Map<String, Boolean> authConfig() {
    return Map.of("oauthEnabled", clientRegistrationRepository != null);
  }

  @Operation(summary = "Iniciar sesion", description = "Autentica con email y password, devuelve JWT")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3j6T6l5C9qkL_rRc0XU\",\"email\":\"usuario@ejemplo.com\",\"fullName\":\"Juan Perez\",\"roles\":[\"ROLE_ADMIN\"],\"expiresIn\":86400,\"verified\":true}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (email o password vacios)"),
      @ApiResponse(responseCode = "401", description = "Credenciales invalidas"),
      @ApiResponse(responseCode = "403", description = "Cuenta no verificada"),
      @ApiResponse(responseCode = "429", description = "Demasiados intentos de login"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/login")
  public LoginResponse login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales del usuario", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"email\":\"usuario@ejemplo.com\",\"password\":\"MiPassword123\"}")))
      @Valid @RequestBody LoginRequest request) {
    AppUser user = service.login(request);
    String token = jwtTokenProvider.generateToken(user);
    return new LoginResponse(token, user.email, user.fullName,
        List.of("ROLE_" + user.role.name()), expirationMs / 1000, user.verified);
  }

  /**
   * Registra un nuevo usuario con email y contraseña.
   *
   * Si el servicio de email está habilitado, envía un correo de verificación.
   * En modo desarrollo, retorna el token directamente para pruebas.
   *
   * @param request datos del nuevo usuario
   * @param locale  idioma para los mensajes de respuesta
   * @return mensaje de confirmación y datos del usuario creado
   * @since 2.1.0
   */
  @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en la base de datos y envia correo de verificacion")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"message\":\"Usuario registrado exitosamente. Revisa tu correo para verificar tu cuenta.\",\"email\":\"nuevo@ejemplo.com\"}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "409", description = "El correo ya existe"),
      @ApiResponse(responseCode = "429", description = "Demasiados intentos"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/register")
  public ResponseEntity<?> register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del nuevo usuario", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"email\":\"nuevo@ejemplo.com\",\"fullName\":\"Maria Garcia\",\"password\":\"Segura123\",\"role\":\"VIEWER\"}")))
      @Valid @RequestBody UserCreateRequest request, Locale locale) {
    AppUser user = service.register(request);
    if (emailEnabled) {
      emailService.sendVerificationEmail(user.email, user.fullName, user.verificationToken);
      return ResponseEntity.ok(Map.of(
          "message", messages.getMessage("auth.register.success.email", null, locale),
          "email", user.email));
    }
    // Fallback for academic/demo environments without SMTP configured
    return ResponseEntity.ok(Map.of(
        "message", messages.getMessage("auth.register.success.dev", null, locale),
        "email", user.email,
        "token", user.verificationToken));
  }

  @Operation(summary = "Refrescar token", description = "Extiende la validez del JWT actual")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Nuevo token JWT generado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9.newtoken123\",\"email\":\"usuario@ejemplo.com\",\"fullName\":\"Juan Perez\",\"roles\":[\"ROLE_ADMIN\"],\"expiresIn\":86400,\"verified\":true}"))),
      @ApiResponse(responseCode = "401", description = "Token invalido o expirado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/refresh")
  @PreAuthorize("isAuthenticated()")
  public LoginResponse refresh(Authentication authentication, Locale locale) {
    String email = authentication.getName();
    AppUser user = service.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            messages.getMessage("auth.user.not.found", null, locale)));
    String token = jwtTokenProvider.generateToken(user);
    return new LoginResponse(token, user.email, user.fullName,
        List.of("ROLE_" + user.role.name()), expirationMs / 1000, user.verified);
  }

  @Operation(summary = "Verificar email (GET)", description = "Marca el email como verificado usando un token desde un enlace")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Email verificado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Token no proporcionado"),
      @ApiResponse(responseCode = "401", description = "Token invalido o expirado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping("/verify-email")
  public Map<String, String> verifyEmailGet(@Parameter(description = "Token de verificacion recibido por email", required = true, example = "abc123-def456-ghi789") @RequestParam("token") String token, Locale locale) {
    service.verifyEmailWithToken(token);
    return Map.of("message", messages.getMessage("auth.email.verified", null, locale));
  }

  @Operation(summary = "Verificar email (POST)", description = "Marca el email como verificado usando un token JWT o secure token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Email verificado exitosamente"),
      @ApiResponse(responseCode = "400", description = "Token no proporcionado"),
      @ApiResponse(responseCode = "401", description = "Token invalido o expirado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/verify")
  public Map<String, String> verifyEmailPost(@RequestBody Map<String, String> body, Locale locale) {
    String token = body.get("token");
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          messages.getMessage("auth.token.required", null, locale));
    }
    // Try secure token first
    try {
      service.verifyEmailWithToken(token);
      return Map.of("message", messages.getMessage("auth.email.verified", null, locale));
    } catch (ResponseStatusException ex) {
      // Fallback to JWT token for backwards compatibility
      if (!jwtTokenProvider.validateToken(token)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            messages.getMessage("auth.token.invalid", null, locale));
      }
      String email = jwtTokenProvider.getEmailFromToken(token);
      service.verifyEmail(email);
      return Map.of("message", messages.getMessage("auth.email.verified", null, locale));
    }
  }

  @Operation(summary = "Reenviar verificacion", description = "Genera un nuevo token de verificacion y lo envia por email")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Token de verificacion reenviado"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/resend-verification")
  @PreAuthorize("isAuthenticated()")
  public Map<String, String> resendVerification(Authentication authentication, Locale locale) {
    String email = authentication.getName();
    AppUser user = service.regenerateVerificationToken(email);
    if (emailEnabled) {
      emailService.sendVerificationEmail(user.email, user.fullName, user.verificationToken);
      return Map.of("message", messages.getMessage("auth.verification.sent", null, locale));
    }
    return Map.of("message", messages.getMessage("auth.verification.regenerated.dev", null, locale),
        "token", user.verificationToken);
  }

  @Operation(summary = "Solicitar recuperacion de contrasena", description = "Genera un token de recuperacion y lo envia por email")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Correo de recuperacion enviado (si el email existe)"),
      @ApiResponse(responseCode = "400", description = "Email no proporcionado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/forgot-password")
  public Map<String, String> forgotPassword(@RequestBody Map<String, String> body, Locale locale) {
    String email = body.get("email");
    if (email == null || email.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          messages.getMessage("auth.email.required", null, locale));
    }
    AppUser user = service.createPasswordResetToken(email);
    String sentMessage = messages.getMessage("auth.password.reset.sent", null, locale);
    if (user == null) {
      // Do not reveal whether email exists
      return Map.of("message", sentMessage);
    }
    if (emailEnabled) {
      emailService.sendPasswordResetEmail(user.email, user.fullName, user.resetToken);
      return Map.of("message", sentMessage);
    }
    // Fallback for academic/demo environments
    return Map.of("message", messages.getMessage("auth.password.reset.dev", null, locale),
        "token", user.resetToken);
  }

  @Operation(summary = "Restablecer contrasena", description = "Usa el token de recuperacion para cambiar la contrasena")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Contrasena restablecida exitosamente"),
      @ApiResponse(responseCode = "400", description = "Token o contrasena invalidos"),
      @ApiResponse(responseCode = "401", description = "Token invalido o expirado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping("/reset-password")
  public Map<String, String> resetPassword(@RequestBody Map<String, String> body, Locale locale) {
    String token = body.get("token");
    String newPassword = body.get("password");
    if (token == null || newPassword == null || newPassword.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          messages.getMessage("auth.token.password.required", null, locale));
    }
    if (newPassword.length() < 4) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          messages.getMessage("validation.password.required", null, locale));
    }
    service.resetPasswordWithToken(token, newPassword);
    return Map.of("message", messages.getMessage("auth.password.reset.success", null, locale));
  }

  @Operation(summary = "Perfil actual", description = "Devuelve los datos del usuario autenticado (JWT u OAuth2)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Datos del usuario",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":1,\"email\":\"usuario@ejemplo.com\",\"fullName\":\"Juan Perez\",\"role\":\"ADMIN\",\"provider\":\"email\",\"verified\":true}"))),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
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

  @Operation(summary = "Actualizar perfil", description = "Actualiza el nombre y email del usuario autenticado")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Perfil actualizado",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":1,\"email\":\"nuevo@ejemplo.com\",\"fullName\":\"Juan Perez Actualizado\",\"role\":\"ADMIN\",\"provider\":\"email\",\"verified\":true}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "409", description = "El correo ya existe"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PatchMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public UserResponse updateProfile(Authentication authentication,
      @Valid @RequestBody UserProfileUpdateRequest request, Locale locale) {
    String email = authentication.getName();
    AppUser user = service.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            messages.getMessage("auth.user.not.found", null, locale)));
    if (!user.email.equalsIgnoreCase(request.email())) {
      service.findByEmail(request.email()).ifPresent(existing -> {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            messages.getMessage("user.email.exists", null, locale));
      });
    }
    user.email = request.email();
    user.fullName = request.fullName();
    return UserResponse.from(service.update(user));
  }
}
