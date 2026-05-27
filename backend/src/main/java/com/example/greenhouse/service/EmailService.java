package com.example.greenhouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Service for sending transactional emails with HTML templates. */
@Service
public class EmailService {
  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:noreply@greenhouse.local}")
  private String fromAddress;

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void send(String to, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
    log.info("Sent plain text email to: {} subject: {}", to, subject);
  }

  public void sendHtml(String to, String subject, String htmlBody) {
    try {
      var mimeMessage = mailSender.createMimeMessage();
      var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setFrom(fromAddress);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);
      mailSender.send(mimeMessage);
      log.info("Sent HTML email to: {} subject: {}", to, subject);
    } catch (Exception e) {
      log.error("Failed to send HTML email to: {} subject: {}", to, subject, e);
      // Fallback to plain text so the operation doesn't completely fail
      send(to, subject, stripHtml(htmlBody));
    }
  }

  public void sendVerificationEmail(String to, String fullName, String token) {
    String verifyUrl = frontendUrl.replaceAll("/+$", "") + "/verify-email?token=" + token;
    String html = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"><title>Verificacion de cuenta</title></head>
        <body style="font-family:Arial,sans-serif; color:#333; max-width:600px; margin:0 auto;">
          <h2 style="color:#2d6a4f;">Hola %s,</h2>
          <p>Gracias por registrarte en <strong>GreenHouse Manager</strong>.</p>
          <p>Para activar tu cuenta, haz clic en el siguiente boton:</p>
          <p style="text-align:center; margin:24px 0;">
            <a href="%s" style="background:#2d6a4f; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
              Verificar mi cuenta
            </a>
          </p>
          <p>O copia y pega este enlace en tu navegador:</p>
          <p style="word-break:break-all; background:#f5f5f5; padding:10px; border-radius:4px;">%s</p>
          <p style="color:#888; font-size:12px;">Este enlace expira en 24 horas. Si no solicitaste esta cuenta, ignora este mensaje.</p>
        </body>
        </html>
        """.formatted(escapeHtml(fullName), verifyUrl, verifyUrl);
    sendHtml(to, "Verifica tu cuenta GreenHouse", html);
  }

  public void sendPasswordResetEmail(String to, String fullName, String token) {
    String resetUrl = frontendUrl.replaceAll("/+$", "") + "/reset-password?token=" + token;
    String html = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"><title>Recuperacion de contrasena</title></head>
        <body style="font-family:Arial,sans-serif; color:#333; max-width:600px; margin:0 auto;">
          <h2 style="color:#2d6a4f;">Hola %s,</h2>
          <p>Recibimos una solicitud para restablecer la contrasena de tu cuenta en <strong>GreenHouse Manager</strong>.</p>
          <p>Haz clic en el siguiente boton para crear una nueva contrasena:</p>
          <p style="text-align:center; margin:24px 0;">
            <a href="%s" style="background:#2d6a4f; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
              Restablecer contrasena
            </a>
          </p>
          <p>O copia y pega este enlace en tu navegador:</p>
          <p style="word-break:break-all; background:#f5f5f5; padding:10px; border-radius:4px;">%s</p>
          <p style="color:#888; font-size:12px;">Este enlace expira en 1 hora. Si no solicitaste esto, ignora este mensaje.</p>
        </body>
        </html>
        """.formatted(escapeHtml(fullName), resetUrl, resetUrl);
    sendHtml(to, "Recuperacion de contrasena GreenHouse", html);
  }

  private static String stripHtml(String html) {
    return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
  }

  private static String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
