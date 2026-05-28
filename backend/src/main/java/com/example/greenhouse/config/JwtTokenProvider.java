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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Proveedor central de tokens JWT para el sistema de autenticación.
 *
 * Responsabilidades:
 * <ul>
 *   <li>Generar tokens JWT firmados con HMAC-SHA256 usando una clave secreta
 *       configurable mediante {@code app.jwt-secret}.</li>
 *   <li>Validar la integridad y expiración de tokens entrantes.</li>
 *   <li>Extraer el email del usuario y los roles desde el payload del token.</li>
 * </ul>
 *
 * Seguridad:
 * <ul>
 *   <li>La clave secreta debe tener al menos 32 caracteres.</li>
 *   <li>Los tokens incluyen: email (subject), roles, verified, issuedAt, expiration.</li>
 *   <li>Expiración configurable mediante {@code app.jwt-expiration-ms} (default 24h).</li>
 *   <li>Firma HMAC-SHA256 usando la librería jjwt (io.jsonwebtoken).</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Component
public class JwtTokenProvider {
  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtTokenProvider(
      @Value("${app.jwt-secret}") String secret,
      @Value("${app.jwt-expiration-ms}") long expirationMs) {
    if (secret == null || secret.isBlank() || secret.length() < 32) {
      throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
    }
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  /**
   * Genera un token JWT firmado para el usuario autenticado.
   *
   * El payload incluye:
   * <ul>
   *   <li>subject: email del usuario</li>
   *   <li>roles: lista de roles Spring Security (e.g. {@code ROLE_ADMIN})</li>
   *   <li>verified: estado de verificación de email</li>
   *   <li>exp: timestamp de expiración</li>
   * </ul>
   *
   * @param user usuario autenticado con email, rol y verified
   * @return token JWT compacto firmado
   * @since 2.1.0
   */
  public String generateToken(AppUser user) {
    Date now = new Date();
    return Jwts.builder()
        .subject(user.email)
        .claim("roles", List.of("ROLE_" + user.role.name()))
        .claim("verified", user.verified)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(secretKey)
        .compact();
  }

  /**
   * Extrae el email del usuario desde un token JWT válido.
   *
   * @param token token JWT compacto
   * @return email del sujeto
   * @throws JwtException si el token es inválido o está expirado
   * @since 2.1.0
   */
  public String getEmailFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  /**
   * Extrae la lista de roles Spring Security desde el payload del token.
   *
   * @param token token JWT compacto
   * @return lista de roles (e.g. {@code ["ROLE_ADMIN", "ROLE_OPERATOR"]})
   * @since 2.1.0
   */
  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
    return parseClaims(token).get("roles", List.class);
  }

  /**
   * Verifica si un token JWT ha expirado.
   *
   * @param token token JWT compacto
   * @return {@code true} si el token está expirado o es inválido
   * @since 2.1.0
   */
  public boolean isTokenExpired(String token) {
    try {
      Date expiration = parseClaims(token).getExpiration();
      return expiration.before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return true;
    }
  }

  /**
   * Valida la integridad y expiración de un token JWT.
   *
   * @param token token JWT compacto
   * @return {@code true} si el token es válido
   * @throws ExpiredJwtException si el token está expirado (el caller debe
   *         manejar el refresh)
   * @since 2.1.0
   */
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.debug("JWT expired: {}", e.getMessage());
      throw e;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Parsea y verifica la firma de un token JWT.
   *
   * @param token token JWT compacto
   * @return claims verificados del token
   */
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
