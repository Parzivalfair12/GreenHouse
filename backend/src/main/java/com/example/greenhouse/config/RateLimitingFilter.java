package com.example.greenhouse.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de rate limiting en memoria para endpoints de autenticación.
 *
 * Registra intentos fallidos por IP + identificador (email o ruta) y bloquea
 * después de 5 fallos en 15 minutos. Retorna HTTP 429 Too Many Requests con
 * header Retry-After.
 *
 * Rutas protegidas: /api/auth/login, /api/auth/refresh,
 * /api/auth/forgot-password, /api/auth/reset-password.
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

  private static final int MAX_ATTEMPTS = 5;
  private static final long BLOCK_DURATION_MS = 15 * 60 * 1000L; // 15 minutes
  private static final long WINDOW_MS = 15 * 60 * 1000L;

  private final Map<String, AttemptBucket> buckets = new ConcurrentHashMap<>();

  private static class AttemptBucket {
    int attempts;
    long firstAttemptMs;
    long blockedUntilMs;

    AttemptBucket(long now) {
      this.attempts = 1;
      this.firstAttemptMs = now;
      this.blockedUntilMs = 0;
    }
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String path = request.getRequestURI();
    if (!isProtectedPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIp(request);
    String identifier = extractIdentifier(request, path);
    String key = clientIp + ":" + identifier + ":" + path;

    long now = System.currentTimeMillis();

    synchronized (buckets) {
      AttemptBucket bucket = buckets.get(key);
      if (bucket != null) {
        // Clean expired window
        if (now - bucket.firstAttemptMs > WINDOW_MS && bucket.blockedUntilMs < now) {
          buckets.remove(key);
          bucket = null;
        }
      }

      if (bucket != null && bucket.blockedUntilMs > now) {
        long retryAfter = (bucket.blockedUntilMs - now) / 1000;
        log.warn("Rate limit triggered for IP={}, identifier={}, path={}", clientIp, identifier, path);
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Demasiados intentos. Intenta de nuevo en " + retryAfter + " segundos.\",\"retryAfter\":" + retryAfter + "}");
        return;
      }
    }

    // Wrap response to detect failed attempts
    StatusCaptureResponseWrapper wrapped = new StatusCaptureResponseWrapper(response);
    filterChain.doFilter(request, wrapped);

    // Count as failed if status indicates authentication failure
    if (isFailureStatus(wrapped.getStatus())) {
      synchronized (buckets) {
        AttemptBucket bucket = buckets.computeIfAbsent(key, k -> new AttemptBucket(now));
        // Reset window if expired
        if (now - bucket.firstAttemptMs > WINDOW_MS) {
          bucket.attempts = 1;
          bucket.firstAttemptMs = now;
        } else {
          bucket.attempts++;
        }
        if (bucket.attempts >= MAX_ATTEMPTS) {
          bucket.blockedUntilMs = now + BLOCK_DURATION_MS;
          log.warn("Blocked IP={}, identifier={}, path={} for {} minutes after {} failed attempts",
              clientIp, identifier, path, BLOCK_DURATION_MS / 60000, bucket.attempts);
        }
      }
    }
  }

  private boolean isProtectedPath(String path) {
    return path.equals("/api/auth/login")
        || path.equals("/api/auth/refresh")
        || path.equals("/api/auth/forgot-password")
        || path.equals("/api/auth/reset-password");
  }

  private String extractIdentifier(HttpServletRequest request, String path) {
    if (path.equals("/api/auth/login") || path.equals("/api/auth/forgot-password")) {
      String email = request.getParameter("email");
      if (email == null || email.isBlank()) {
        // Try to read from JSON body? Simpler: just use IP for body-based requests
        // We'll track by IP alone for these if body email can't be parsed easily
        email = "unknown";
      }
      return email.toLowerCase().trim();
    }
    return "_global_";
  }

  private String getClientIp(HttpServletRequest request) {
    String xf = request.getHeader("X-Forwarded-For");
    if (xf != null && !xf.isBlank()) {
      return xf.split(",")[0].trim();
    }
    String ri = request.getHeader("X-Real-Ip");
    if (ri != null && !ri.isBlank()) {
      return ri.trim();
    }
    return request.getRemoteAddr();
  }

  private boolean isFailureStatus(int status) {
    return status == 401 || status == 403 || status == 429;
  }

  /**
   * Simple wrapper to capture status code after filter chain completes.
   */
  private static class StatusCaptureResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
    private int status = 200;

    StatusCaptureResponseWrapper(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void setStatus(int sc) {
      this.status = sc;
      super.setStatus(sc);
    }

    @Override
    public void sendError(int sc) throws IOException {
      this.status = sc;
      super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
      this.status = sc;
      super.sendError(sc, msg);
    }

    @Override
    public int getStatus() {
      return status;
    }
  }
}
