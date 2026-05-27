package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Greenhouse;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreenhouseRepository extends JpaRepository<Greenhouse, Long> {
  @Override
  @EntityGraph(attributePaths = {"crops", "sensors", "irrigationEvents", "zones", "actuators", "rules"})
  List<Greenhouse> findAll();

  @EntityGraph(attributePaths = {"crops", "sensors", "irrigationEvents", "zones", "actuators", "rules"})
  java.util.Optional<Greenhouse> findById(Long id);

  long countByActiveTrue();
}
