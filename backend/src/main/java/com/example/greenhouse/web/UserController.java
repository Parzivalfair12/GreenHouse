package com.example.greenhouse.web;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.repository.AppUserRepository;
import com.example.greenhouse.web.dto.UserCreateRequest;
import com.example.greenhouse.web.dto.UserResponse;
import com.example.greenhouse.web.dto.UserRoleUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador REST para administración de usuarios y roles.
 *
 * Seguridad:
 * <ul>
 *   <li>Todos los endpoints requieren rol ADMIN.</li>
 *   <li>Las contraseñas se almacenan usando BCrypt.</li>
 *   <li>Los mensajes de error se resuelven por MessageSource (i18n).</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Tag(name = "Usuarios", description = "Administracion de usuarios y roles (solo ADMIN)")
@RestController
@RequestMapping("/api/users")
public class UserController {
  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final MessageSource messages;

  public UserController(AppUserRepository users, PasswordEncoder passwordEncoder, MessageSource messages) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.messages = messages;
  }

  @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> list() {
    return users.findAll().stream()
        .map(UserResponse::from)
        .toList();
  }

  @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario con rol especifico (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":2,\"email\":\"operador@ejemplo.com\",\"fullName\":\"Operador Sistema\",\"role\":\"OPERATOR\",\"provider\":\"email\",\"verified\":true}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (datos incorrectos)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "409", description = "El correo ya existe"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del nuevo usuario", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"email\":\"operador@ejemplo.com\",\"fullName\":\"Operador Sistema\",\"password\":\"Pass1234\",\"role\":\"OPERATOR\"}")))
      @Valid @RequestBody UserCreateRequest request, Locale locale) {
    users.findByEmail(request.email()).ifPresent(existing -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, messages.getMessage("user.email.exists", null, locale));
    });

    AppUser user = new AppUser();
    user.email = request.email();
    user.fullName = request.fullName();
    user.passwordHash = passwordEncoder.encode(request.password());
    user.provider = "email";
    user.role = request.role();
    return UserResponse.from(users.save(user));
  }

  @Operation(summary = "Actualizar rol de usuario", description = "Cambia el rol de un usuario existente (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente",
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"id\":1,\"email\":\"usuario@ejemplo.com\",\"fullName\":\"Juan Perez\",\"role\":\"ADMIN\",\"provider\":\"email\",\"verified\":true}"))),
      @ApiResponse(responseCode = "400", description = "Solicitud invalida (rol incorrecto)"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @PatchMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse updateRole(
      @Parameter(description = "ID del usuario", required = true, example = "1") @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nuevo rol del usuario", required = true,
          content = @Content(mediaType = "application/json",
              examples = @ExampleObject(value = "{\"role\":\"ADMIN\"}")))
      @Valid @RequestBody UserRoleUpdateRequest request,
      Locale locale) {
    AppUser user = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            messages.getMessage("user.not.found", null, locale)));
    user.role = request.role();
    return UserResponse.from(users.save(user));
  }

  @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema (requiere ADMIN)")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
      @ApiResponse(responseCode = "401", description = "No autenticado"),
      @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
      @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(
      @Parameter(description = "ID del usuario", required = true, example = "1") @PathVariable Long id,
      Locale locale) {
    AppUser user = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            messages.getMessage("user.not.found", null, locale)));
    users.delete(user);
  }
}
