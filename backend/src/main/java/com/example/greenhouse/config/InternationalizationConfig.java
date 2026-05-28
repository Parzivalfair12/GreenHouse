package com.example.greenhouse.config;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Configuración de internacionalización (i18n) del sistema.
 *
 * Define:
 * <ul>
 *   <li>{@link ResourceBundleMessageSource} con archivos {@code messages.properties}
 *       codificados en UTF-8 y fallback al locale del sistema deshabilitado.</li>
 *   <li>{@link AcceptHeaderLocaleResolver} que resuelve el locale desde el
 *       header HTTP {@code Accept-Language} con español como default.</li>
 * </ul>
 *
 * Flujo:
 * <ol>
 *   <li>Frontend envía header {@code Accept-Language: es|en} en cada request.</li>
 *   <li>{@code AcceptHeaderLocaleResolver} extrae el locale.</li>
 *   <li>Controllers reciben el parámetro {@code Locale locale} automáticamente.</li>
 *   <li>{@code MessageSource.getMessage()} retorna el texto en el idioma
 *       solicitado o el default (español).</li>
 * </ol>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Configuration
public class InternationalizationConfig {
  @Bean
  MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("messages");
    source.setDefaultEncoding("UTF-8");
    source.setFallbackToSystemLocale(false);
    return source;
  }

  @Bean
  LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(Locale.forLanguageTag("es"));
    return resolver;
  }
}
