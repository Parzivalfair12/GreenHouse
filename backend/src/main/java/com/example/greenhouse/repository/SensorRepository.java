package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Sensor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
  Optional<Sensor> findByCode(String code);

  @Override
  @EntityGraph(attributePaths = "greenhouse")
  Optional<Sensor> findById(Long id);

  @Override
  @EntityGraph(attributePaths = "greenhouse")
  List<Sensor> findAll();
}
