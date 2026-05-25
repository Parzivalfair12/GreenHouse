package com.example.greenhouse.config;

import com.example.greenhouse.web.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = extractToken(request);

    if (StringUtils.hasText(token)) {
      try {
        if (!jwtTokenProvider.validateToken(token)) {
          SecurityContextHolder.clearContext();
          filterChain.doFilter(request, response);
          return;
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        var authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

      } catch (ExpiredJwtException e) {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var error = new ApiErrorResponse(
            LocalDateTime.now().toString(),
            401,
            "Unauthorized",
            "Token expirado. Por favor inicie sesion nuevamente.",
            request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), error);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
