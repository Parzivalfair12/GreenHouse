package com.example.greenhouse.config;

import com.example.greenhouse.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtTokenProvider(
      @Value("${app.jwt-secret}") String secret,
      @Value("${app.jwt-expiration-ms}") long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(AppUser user) {
    Date now = new Date();
    return Jwts.builder()
        .subject(user.email)
        .claim("roles", List.of("ROLE_" + user.role.name()))
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(secretKey)
        .compact();
  }

  public String getEmailFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
    return parseClaims(token).get("roles", List.class);
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      throw e;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
