package com.example.greenhouse.web.dto;

import java.util.List;

/**
 * JWT authentication response returned after successful login or token refresh.
 *
 * @param token     signed JWT access token
 * @param email     authenticated user email
 * @param fullName  user display name
 * @param roles     granted Spring Security roles (e.g. {@code ROLE_ADMIN})
 * @param expiresIn token TTL in seconds
 * @param verified  whether the email has been verified
 * @since 2.1.0
 */
public record LoginResponse(String token, String email, String fullName, List<String> roles, long expiresIn, boolean verified) {}

