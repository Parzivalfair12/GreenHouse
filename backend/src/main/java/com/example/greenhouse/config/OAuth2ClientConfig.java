package com.example.greenhouse.config;

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

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(
      @Value("${GOOGLE_CLIENT_ID}") String clientId,
      @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret,
      @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {

    String baseUrl = frontendUrl.replaceAll("/+$", "");
    String redirectUri = baseUrl + "/login/oauth2/code/google";

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
