package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Zone;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
  List<Zone> findByGreenhouseIdOrderByIdDesc(Long greenhouseId);
}
