package com.example.greenhouse.repository;

import com.example.greenhouse.domain.WorkflowExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
  List<WorkflowExecution> findTop30ByOrderBySyncedAtDesc();
  Optional<WorkflowExecution> findByGithubRunId(Long githubRunId);
  long countByConclusion(String conclusion);
}
