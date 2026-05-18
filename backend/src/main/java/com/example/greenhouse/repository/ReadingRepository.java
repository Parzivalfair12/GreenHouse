package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Reading;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
  @EntityGraph(attributePaths = "sensor")
  List<Reading> findBySensorIdOrderByRecordedAtDesc(Long sensorId);
  @EntityGraph(attributePaths = "sensor")
  Optional<Reading> findFirstBySensorIdOrderByRecordedAtDesc(Long sensorId);
  @Override
  @EntityGraph(attributePaths = "sensor")
  List<Reading> findAll();
}
