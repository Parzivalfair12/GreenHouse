package com.example.greenhouse.config;

import com.example.greenhouse.domain.Alert;
import com.example.greenhouse.domain.AlertSeverity;
import com.example.greenhouse.domain.AppUser;
import com.example.greenhouse.domain.Actuator;
import com.example.greenhouse.domain.ActuatorType;
import com.example.greenhouse.domain.AutomationRule;
import com.example.greenhouse.domain.Crop;
import com.example.greenhouse.domain.Greenhouse;
import com.example.greenhouse.domain.RuleType;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.domain.UserRole;
import com.example.greenhouse.domain.Zone;
import com.example.greenhouse.repository.AppUserRepository;
import com.example.greenhouse.repository.GreenhouseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Inserts demo data that matches the documented JSON model. */
@Configuration
public class DataSeeder {
  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
  @Value("${greenhouse.seeder.admin-password:admin1234}")
  private String adminPassword;

  @Bean
  CommandLineRunner seedGreenhouseData(
      GreenhouseRepository repository,
      AppUserRepository users,
      PasswordEncoder passwordEncoder) {
    return args -> {
      AppUser admin = users.findByEmail("admin@greenhouse.local").orElseGet(() -> {
        AppUser u = new AppUser();
        u.email = "admin@greenhouse.local";
        u.fullName = "Administrador";
        u.passwordHash = passwordEncoder.encode(adminPassword);
        u.provider = "email";
        u.role = UserRole.ADMIN;
        u.verified = true;
        return u;
      });
      // Ensure existing admin has correct credentials and verified flag
      if (!passwordEncoder.matches(adminPassword, admin.passwordHash)) {
        admin.passwordHash = passwordEncoder.encode(adminPassword);
        log.info("Updated admin password hash");
      }
      if (!admin.verified) {
        admin.verified = true;
        log.info("Marked existing admin as verified");
      }
      if (!"ADMIN".equals(admin.role.name())) {
        admin.role = UserRole.ADMIN;
        log.info("Restored admin role for: {}", admin.email);
      }
      users.save(admin);
      log.info("Admin user ensured: {}, role: {}, verified: {}", admin.email, admin.role, admin.verified);

      if (repository.count() > 0) {
        return;
      }

      Greenhouse greenhouse = new Greenhouse();
      greenhouse.name = "Invernadero Norte";
      greenhouse.location = "Campus principal";
      greenhouse.areaSquareMeters = BigDecimal.valueOf(120.5);

      Crop crop = new Crop();
      crop.name = "Tomate chonto";
      crop.variety = "Santa Clara";
      crop.plantedAt = LocalDate.of(2026, 5, 1);
      crop.expectedHarvestAt = LocalDate.of(2026, 8, 15);
      crop.greenhouse = greenhouse;
      greenhouse.crops.add(crop);

      Sensor sensor = new Sensor();
      sensor.code = "TEMP-001";
      sensor.type = SensorType.TEMPERATURE;
      sensor.unit = "C";
      sensor.minThreshold = BigDecimal.valueOf(18);
      sensor.maxThreshold = BigDecimal.valueOf(30);
      sensor.greenhouse = greenhouse;
      greenhouse.sensors.add(sensor);

      Zone zone = new Zone();
      zone.name = "Zona germinacion";
      zone.description = "Bandejas iniciales de cultivo";
      zone.greenhouse = greenhouse;
      greenhouse.zones.add(zone);

      Actuator actuator = new Actuator();
      actuator.name = "Riego simulado norte";
      actuator.type = ActuatorType.IRRIGATION;
      actuator.greenhouse = greenhouse;
      greenhouse.actuators.add(actuator);

      AutomationRule rule = new AutomationRule();
      rule.name = "Humedad baja activa riego";
      rule.type = RuleType.LOW_HUMIDITY_IRRIGATION;
      rule.threshold = BigDecimal.valueOf(45);
      rule.greenhouse = greenhouse;
      greenhouse.rules.add(rule);

      Alert alert = new Alert();
      alert.severity = AlertSeverity.WARNING;
      alert.message = "Temperatura por encima del umbral";
      alert.sensor = sensor;
      sensor.alerts.add(alert);
      repository.save(greenhouse);
    };
  }
}
