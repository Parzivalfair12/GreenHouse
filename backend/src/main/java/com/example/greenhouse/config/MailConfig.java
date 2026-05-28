package com.example.greenhouse.config;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuracion condicional de JavaMailSender.
 *
 * Solo crea el bean cuando greenhouse.email.enabled=true y SMTP_HOST esta definido.
 * Si no se cumple, la aplicacion continua sin servicio de correo y loguea un warning.
 */
@Configuration
public class MailConfig {

  private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

  @Bean
  @ConditionalOnExpression(
      "'${greenhouse.email.enabled:false}'.equals('true') && " +
          "!'${spring.mail.host:}'.isBlank()"
  )
  public JavaMailSender javaMailSender(
      @Value("${spring.mail.host}") String host,
      @Value("${spring.mail.port:587}") int port,
      @Value("${spring.mail.username:}") String username,
      @Value("${spring.mail.password:}") String password,
      @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean auth,
      @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean starttls) {

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    if (!username.isBlank()) {
      mailSender.setUsername(username);
    }
    if (!password.isBlank()) {
      mailSender.setPassword(password);
    }

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", String.valueOf(auth));
    props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
    props.put("mail.debug", "false");

    log.info("JavaMailSender configured: host={}, port={}, auth={}, tls={}", host, port, auth, starttls);
    return mailSender;
  }
}
