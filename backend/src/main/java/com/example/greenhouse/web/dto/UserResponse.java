package com.example.greenhouse.web.dto;

import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.UserRole;

public record UserResponse(Long id, String email, String fullName, UserRole role, String provider, boolean verified) {
  public static UserResponse from(AppUser user) {
    return new UserResponse(user.id, user.email, user.fullName, user.role, user.provider, user.verified);
  }
}
