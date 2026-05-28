package com.example.greenhouse.repository;

import com.example.greenhouse.domain.PipelineJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineJobRepository extends JpaRepository<PipelineJob, Long> {
  List<PipelineJob> findByGithubRunId(Long githubRunId);
}
