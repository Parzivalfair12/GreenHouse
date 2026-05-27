package com.example.greenhouse.repository;

import com.example.greenhouse.domain.IrrigationEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IrrigationEventRepository extends JpaRepository<IrrigationEvent, Long> {
  List<IrrigationEvent> findByGreenhouseIdOrderByStartedAtDesc(Long greenhouseId);
}
