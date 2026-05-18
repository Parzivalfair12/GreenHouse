package com.example.greenhouse.web;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.repository.AppUserRepository;
import com.example.greenhouse.web.dto.UserCreateRequest;
import com.example.greenhouse.web.dto.UserResponse;
import com.example.greenhouse.web.dto.UserRoleUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** User and role administration backed by PostgreSQL. */
@RestController
@RequestMapping("/api/users")
public class UserController {
  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;

  public UserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public List<UserResponse> list() {
    return users.findAll().stream()
        .map(UserResponse::from)
        .toList();
  }

  @PostMapping
  public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
    users.findByEmail(request.email()).ifPresent(existing -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    });

    AppUser user = new AppUser();
    user.email = request.email();
    user.fullName = request.fullName();
    user.passwordHash = passwordEncoder.encode(request.password());
    user.provider = "email";
    user.role = request.role();
    return UserResponse.from(users.save(user));
  }

  @PatchMapping("/{id}/role")
  public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleUpdateRequest request) {
    AppUser user = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    user.role = request.role();
    return UserResponse.from(users.save(user));
  }
}
