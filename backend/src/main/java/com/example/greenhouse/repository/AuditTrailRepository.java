package com.example.greenhouse.repository;

import com.example.greenhouse.domain.AuditTrail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
  List<AuditTrail> findTop50ByOrderByCreatedAtDesc();
  List<AuditTrail> findByEventTypeOrderByCreatedAtDesc(String eventType);
  List<AuditTrail> findBySourceServiceOrderByCreatedAtDesc(String sourceService);
  long countByEventType(String eventType);
  long countByStatus(String status);
}
