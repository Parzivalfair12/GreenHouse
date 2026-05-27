package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Crop;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
  List<Crop> findByGreenhouseIdOrderByIdDesc(Long greenhouseId);
}
