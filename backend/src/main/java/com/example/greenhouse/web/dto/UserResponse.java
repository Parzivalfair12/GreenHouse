package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.UserRole;

/**
 * Projection of {@link com.example.greenhouse.domain.AppUser} for API responses.
 * Excludes sensitive fields (password hash, tokens).
 *
 * @since 2.1.0
 */
public record UserResponse(Long id, String email, String fullName, UserRole role, String provider, boolean verified) {
  public static UserResponse from(AppUser user) {
    return new UserResponse(user.id, user.email, user.fullName, user.role, user.provider, user.verified);
  }
}
