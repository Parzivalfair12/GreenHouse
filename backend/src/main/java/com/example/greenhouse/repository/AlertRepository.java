package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
  @EntityGraph(attributePaths = "sensor")
  List<Alert> findByResolvedFalseOrderByCreatedAtDesc();
}
