package com.example.greenhouse.web;

import com.example.greenhouse.config.JwtTokenProvider;
import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.service.AuthService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Authentication endpoints that persist demo users in PostgreSQL. */
@Tag(name = "Autenticacion", description = "Endpoints de login, registro y perfil de usuario")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  private final JwtTokenProvider jwtTokenProvider;
  private final long expirationMs;
  private final MessageSource messages;

  public AuthController(AuthService service, JwtTokenProvider jwtTokenProvider,
      @Value("${app.jwt-expiration-ms}") long expirationMs,
      MessageSource messages) {
    this.service = service;
    this.jwtTokenProvider = jwtTokenProvider;
    this.expirationMs = expirationMs;
    this.messages = messages;
  }

  @Operation(summary = "Iniciar sesion", description = "Autentica con email y password, devuelve JWT")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado"),
      @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
  })
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    AppUser user = service.login(request);
    String token = jwtTokenProvider.generateToken(user);
    return new LoginResponse(token, user.email, user.fullName,
        List.of("ROLE_" + user.role.name()), expirationMs / 1000);
  }

  @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en la base de datos")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
      @ApiResponse(responseCode = "409", description = "El correo ya existe")
  })
  @PostMapping("/register")
  public UserResponse register(@Valid @RequestBody UserCreateRequest request) {
    return UserResponse.from(service.register(request));
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
