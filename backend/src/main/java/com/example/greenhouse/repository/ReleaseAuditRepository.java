package com.example.greenhouse.repository;

import com.example.greenhouse.domain.ReleaseAudit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseAuditRepository extends JpaRepository<ReleaseAudit, Long> {
  List<ReleaseAudit> findTop10ByOrderByReleasedAtDesc();
  Optional<ReleaseAudit> findByTagName(String tagName);
}
