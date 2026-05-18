package com.example.greenhouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(AlertService.class)
@ActiveProfiles("test")
class AlertServiceTest {
  @Autowired
  AlertRepository repository;

  @Autowired
  AlertService service;

  @Test
  void resolveMarksAlertAsResolved() {
    Alert alert = new Alert();
    alert.message = "Temperatura alta";
    Alert saved = repository.save(alert);

    service.resolve(saved.id);

    assertThat(repository.findById(saved.id)).get().extracting("resolved").isEqualTo(true);
  }
}
