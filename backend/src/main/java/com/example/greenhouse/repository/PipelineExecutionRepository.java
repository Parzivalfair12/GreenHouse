package com.example.greenhouse.repository;

import com.example.greenhouse.domain.PipelineExecution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineExecutionRepository extends JpaRepository<PipelineExecution, Long> {
  List<PipelineExecution> findTop50ByOrderByCreatedAtDesc();
  List<PipelineExecution> findByStatusOrderByCreatedAtDesc(String status);
  long countByStatus(String status);
}
