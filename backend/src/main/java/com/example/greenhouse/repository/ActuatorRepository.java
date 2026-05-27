package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Actuator;
import com.example.greenhouse.domain.ActuatorType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActuatorRepository extends JpaRepository<Actuator, Long> {
  @Override
  @EntityGraph(attributePaths = "greenhouse")
  List<Actuator> findAll();
  List<Actuator> findByGreenhouseIdOrderByIdDesc(Long greenhouseId);
  Optional<Actuator> findFirstByGreenhouseIdAndTypeAndActiveTrue(Long greenhouseId, ActuatorType type);
  long countByEnabledTrue();
}
