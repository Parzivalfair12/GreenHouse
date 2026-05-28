package com.example.greenhouse.config;

import com.example.greenhouse.web.dto.ApiErrorResponse;
import com.example.greenhouse.web.dto.ValidationErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.i18n.LocaleContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final MessageSource messages;

  public GlobalExceptionHandler(MessageSource messages) {
    this.messages = messages;
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
    log.warn("ResponseStatusException {}: {}", ex.getStatusCode().value(), ex.getReason());
    return buildResponse(ex.getStatusCode().value(), ex.getReason(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    log.warn("Validation failed for {}: {}", request.getDescription(false), fieldErrors);
    Locale locale = LocaleContextHolder.getLocale();
    var body = new ValidationErrorResponse(
        LocalDateTime.now().toString(),
        400,
        messages.getMessage("validation.error.title", null, "Validation Failed", locale),
        messages.getMessage("validation.error", null, "Validation error in the submitted fields", locale),
        request.getDescription(false).replace("uri=", ""),
        fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
    log.warn("IllegalArgumentException: {}", ex.getMessage());
    return buildResponse(400, ex.getMessage(), request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    log.warn("AccessDeniedException for {}: {}", request.getDescription(false), ex.getMessage());
    Locale locale = LocaleContextHolder.getLocale();
    return buildResponse(403, messages.getMessage("access.denied", null, locale), request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex, WebRequest request) {
    log.error("Unhandled exception for {}: {}", request.getDescription(false), ex.getMessage(), ex);
    Locale locale = LocaleContextHolder.getLocale();
    return buildResponse(500, messages.getMessage("internal.error", null, locale), request);
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(int status, String message, WebRequest request) {
    var body = new ApiErrorResponse(
        LocalDateTime.now().toString(),
        status,
        HttpStatus.valueOf(status).getReasonPhrase(),
        message,
        request.getDescription(false).replace("uri=", ""));
    return ResponseEntity.status(status).body(body);
  }
}
