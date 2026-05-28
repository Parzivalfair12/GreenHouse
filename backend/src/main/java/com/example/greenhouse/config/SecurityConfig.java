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

/**
 * Configuración central de seguridad HTTP del sistema.
 *
 * Define:
 * <ul>
 *   <li>Cadena de filtros de seguridad con JWT, rate limiting y headers de seguridad.</li>
 *   <li>Políticas de autorización por ruta y rol (RBAC).</li>
 *   <li>Configuración CORS para el frontend.</li>
 *   <li>Integración OAuth2 Google cuando está habilitado.</li>
 *   <li>BCrypt como codificador de contraseñas.</li>
 * </ul>
 *
 * Orden de filtros:
 * <ol>
 *   <li>SecurityHeadersFilter (headers de seguridad)</li>
 *   <li>RateLimitingFilter (límite de intentos)</li>
 *   <li>JwtAuthenticationFilter (autenticación JWT)</li>
 *   <li>UsernamePasswordAuthenticationFilter (autenticación por formulario)</li>
 * </ol>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
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

  /**
   * Construye la cadena de filtros de seguridad HTTP.
   *
   * Configura:
   * <ul>
   *   <li>CSRF deshabilitado para /api/** (REST stateless con JWT).</li>
   *   <li>Headers de seguridad ejecutados antes que cualquier filtro de auth.</li>
   *   <li>Rate limiting antes de autenticación para protección contra brute-force.</li>
   *   <li>JWT filter después de rate limiting.</li>
   *   <li>Rutas públicas: login, register, forgot-password, health, Swagger.</li>
   *   <li>Rutas por rol: POST/DELETE /api/users → ADMIN, etc.</li>
   *   <li>OAuth2 login habilitado solo si ClientRegistrationRepository existe.</li>
   * </ul>
   *
   * @param http                          builder de seguridad HTTP
   * @param successHandler                manejador de éxito OAuth2
   * @param clientRegistrationRepository  repositorio OAuth2 (null si deshabilitado)
   * @return cadena de filtros construida
   * @throws Exception si ocurre un error en la configuración
   */
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      OAuth2LoginSuccessHandler successHandler,
      @Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) throws Exception {
    http
        .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .cors(Customizer.withDefaults())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/forgot-password").permitAll()
            .requestMatchers("/api/auth/reset-password").permitAll()
            .requestMatchers("/api/auth/verify-email").permitAll()
            .requestMatchers("/api/auth/verify").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/api/debug/**").hasRole("ADMIN")
            .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/greenhouses").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/greenhouses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/greenhouses/**").hasRole("ADMIN")
            .requestMatchers("/api/alerts/*/resolve").hasAnyRole("ADMIN", "OPERATOR")
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated());

    if (clientRegistrationRepository != null) {
      http.oauth2Login(oauth -> oauth
          .successHandler(successHandler)
          .failureUrl(successHandler.failureUrl()));
    }

    http.logout(logout -> logout.logoutSuccessUrl("/"));
    return http.build();
  }

  /**
   * Codificador de contraseñas BCrypt para almacenamiento seguro.
   *
   * @return instancia de {@link BCryptPasswordEncoder}
   */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configuración CORS que permite peticiones desde el frontend.
   *
   * Orígenes permitidos:
   * <ul>
   *   <li>{@code http://localhost:5173} (desarrollo Vite)</li>
   *   <li>{@code http://localhost:3000} (alternativa desarrollo)</li>
   *   <li>{@code app.frontend-url} de configuración (producción)</li>
   * </ul>
   *
   * Headers permitidos: Authorization, Content-Type, Accept-Language.
   *
   * @param frontendUrl URL del frontend desde configuración
   * @return fuente de configuración CORS
   */
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
