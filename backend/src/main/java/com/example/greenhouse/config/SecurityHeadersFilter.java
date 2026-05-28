package com.example.greenhouse.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que agrega headers de seguridad a todas las respuestas HTTP.
 *
 * Headers incluidos:
 * <ul>
 *   <li>X-Content-Type-Options: nosniff — previene MIME sniffing</li>
 *   <li>X-Frame-Options: DENY — previene clickjacking</li>
 *   <li>Referrer-Policy — control de información de referencia</li>
 *   <li>Content-Security-Policy — restricción de recursos (scripts, estilos,
 *       conexiones, frames)</li>
 *   <li>Permissions-Policy — deshabilita APIs sensibles (geolocalización,
 *       micrófono, cámara)</li>
 * </ul>
 *
 * Se ejecuta con la máxima precedencia en la cadena de filtros.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SecurityHeadersFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    response.setHeader("X-XSS-Protection", "0"); // Modern browsers use CSP instead
    response.setHeader("Content-Security-Policy",
        "default-src 'self'; " +
        "script-src 'self'; " +
        "style-src 'self' 'unsafe-inline'; " +
        "img-src 'self' data:; " +
        "font-src 'self'; " +
        "connect-src 'self'; " +
        "frame-ancestors 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self'");
    response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

    filterChain.doFilter(request, response);
  }
}
