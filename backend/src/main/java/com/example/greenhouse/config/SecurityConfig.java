package com.example.greenhouse.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** OAuth2 and endpoint authorization rules. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler successHandler) throws Exception {
    return http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .cors(Customizer.withDefaults())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            // Publicos — sin autenticacion
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()

            // Admin only
            .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/greenhouses").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/greenhouses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/greenhouses/**").hasRole("ADMIN")

            // Operator y Admin pueden resolver alertas
            .requestMatchers("/api/alerts/**/resolve").hasAnyRole("ADMIN", "OPERATOR")

            // Todos los autenticados pueden leer
            .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

            // El resto de /api/** requiere autenticacion minima
            .requestMatchers("/api/**").authenticated()

            // Cualquier otra peticion
            .anyRequest().authenticated())
        .oauth2Login(oauth -> oauth
            .successHandler(successHandler)
            .failureUrl(successHandler.failureUrl()))
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(frontendUrl));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept-Language"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
