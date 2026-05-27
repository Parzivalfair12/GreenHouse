package com.example.greenhouse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

/**
 * Conditional OAuth2 client configuration.
 * Only loaded when app.oauth2.enabled=true.
 * Prevents startup crashes when GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET are not set.
 */
@Configuration
@ConditionalOnProperty(name = "app.oauth2.enabled", havingValue = "true")
public class OAuth2ClientConfig {
  private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConfig.class);

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(
      @Value("${GOOGLE_CLIENT_ID}") String clientId,
      @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
      @Value("${app.oauth2.redirect-uri:}") String redirectUriOverride) {

    // Hardcode exact redirect URI to eliminate mismatch with Google Cloud Console
    String redirectUri = "http://localhost:8080/login/oauth2/code/google";

    log.info("OAuth2 redirect URI hardcoded: {}", redirectUri);
    log.info("OAuth2 client ID starts with: {}...", clientId.length() > 8 ? clientId.substring(0, 8) : clientId);

    ClientRegistration google = ClientRegistration.withRegistrationId("google")
        .clientId(clientId)
        .clientSecret(clientSecret)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri(redirectUri)
        .scope("openid", "email", "profile")
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .tokenUri("https://www.googleapis.com/oauth2/v4/token")
        .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
        .userNameAttributeName(IdTokenClaimNames.SUB)
        .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
        .clientName("Google")
        .build();

    return new InMemoryClientRegistrationRepository(google);
  }
}
