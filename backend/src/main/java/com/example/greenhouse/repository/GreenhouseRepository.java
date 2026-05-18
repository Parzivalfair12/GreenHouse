package com.example.greenhouse.repository;

import com.example.greenhouse.domain.Greenhouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GreenhouseRepository extends JpaRepository<Greenhouse, Long> {}
