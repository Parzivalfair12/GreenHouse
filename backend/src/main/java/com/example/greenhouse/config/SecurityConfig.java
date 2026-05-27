package com.example.greenhouse.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final RateLimitingFilter rateLimitingFilter;
  private final SecurityHeadersFilter securityHeadersFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
      RateLimitingFilter rateLimitingFilter,
      SecurityHeadersFilter securityHeadersFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.rateLimitingFilter = rateLimitingFilter;
    this.securityHeadersFilter = securityHeadersFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      OAuth2LoginSuccessHandler successHandler,
      @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) throws Exception {
    http
        // Rate limiting and security headers run before everything else
        .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        // CSRF disabled for /api/** because this is a stateless REST API using JWT Bearer tokens.
        // The JWT is sent in Authorization header for programmatic access (login, register, CRUD).
        // CSRF remains active for non-API endpoints (e.g., OAuth2 callback paths).
        // H2 console is excluded only for local development convenience.
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .cors(Customizer.withDefaults())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/forgot-password").permitAll()
            .requestMatchers("/api/auth/reset-password").permitAll()
            .requestMatchers("/api/auth/verify-email").permitAll()
            .requestMatchers("/api/auth/verify").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/debug/**").permitAll()
            .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()

            .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/greenhouses").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/greenhouses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/greenhouses/**").hasRole("ADMIN")

            // Operator and Admin can resolve alerts
            .requestMatchers("/api/alerts/*/resolve").hasAnyRole("ADMIN", "OPERATOR")

            // Authenticated read access
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

            // All other API requires authentication
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated());

    // OAuth2 login is only enabled when ClientRegistrationRepository exists
    // (created by OAuth2ClientConfig when app.oauth2.enabled=true)
    if (clientRegistrationRepository != null) {
      http.oauth2Login(oauth -> oauth
          .successHandler(successHandler)
          .failureUrl(successHandler.failureUrl()));
    }

    http.logout(logout -> logout.logoutSuccessUrl("/"));
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
    CorsConfiguration configuration = new CorsConfiguration();
    var origins = new java.util.ArrayList<>(List.of("http://localhost:5173", "http://localhost:3000"));
    if (frontendUrl != null && !frontendUrl.isBlank() && !origins.contains(frontendUrl)) {
      origins.add(frontendUrl);
    }
    configuration.setAllowedOriginPatterns(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
