package com.example.greenhouse.web.dto;

import java.util.List;

public record LoginResponse(String token, String email, String fullName, List<String> roles, long expiresIn, boolean verified) {}
