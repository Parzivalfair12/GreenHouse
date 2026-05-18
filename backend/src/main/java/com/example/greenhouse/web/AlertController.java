package com.example.greenhouse.web;

import com.example.greenhouse.service.AlertService;
import com.example.greenhouse.web.dto.AlertResponse;
import com.example.greenhouse.web.dto.MessageResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for greenhouse alerts. */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
  private final AlertService service;
  private final MessageSource messages;

  public AlertController(AlertService service, MessageSource messages) {
    this.service = service;
    this.messages = messages;
  }

  @GetMapping("/open")
  public List<AlertResponse> findOpenAlerts() {
    return service.findOpenAlerts().stream().map(AlertResponse::from).toList();
  }

  @PatchMapping("/{id}/resolve")
  public MessageResponse resolve(@PathVariable long id, Locale locale) {
    service.resolve(id);
    return new MessageResponse(messages.getMessage("alert.resolved", null, locale));
  }
}
