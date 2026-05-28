package com.example.greenhouse.service;

import com.example.greenhouse.domain.Reading;
import com.example.greenhouse.domain.Sensor;
import com.example.greenhouse.domain.SensorType;
import com.example.greenhouse.repository.ReadingRepository;
import com.example.greenhouse.repository.SensorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Motor de simulación IoT que genera lecturas inteligentes de sensores cada 5 segundos.
 * Los valores evolucionan gradualmente (suben, bajan, se estabilizan) en lugar de
 * saltos aleatorios puros. Ocasionalmente se generan lecturas anómalas para
 * disparar alertas y reglas de automatización.
 *
 * Reglas de negocio:
 * <ul>
 *   <li>Cada sensor mantiene un estado interno de simulación con tendencia y
 *       ruido gaussiano para generar valores realistas.</li>
 *   <li>5% de las lecturas son anómalas (fuera de rango) para probar el
 *       motor de reglas y alertas.</li>
 *   <li>Las tendencias cambian gradualmente cada 5 ticks para simular
 *       ciclos ambientales naturales.</li>
 *   <li>Límites suaves (soft bounce) evitan valores infinitos.</li>
 * </ul>
 *
 * @author GreenHouse Team
 * @version 2.1.0
 * @since 2.1.0
 */
@Service
public class SimulationService {

  private static final Logger log = LoggerFactory.getLogger(SimulationService.class);
  private static final long INTERVAL_MS = 5000;

  private final SensorRepository sensors;
  private final ReadingRepository readings;
  private final RuleEngineService ruleEngine;

  private final Random random = new Random();
  private final Map<Long, SensorState> sensorStates = new ConcurrentHashMap<>();

  private volatile boolean running = false;

  public SimulationService(SensorRepository sensors, ReadingRepository readings,
      RuleEngineService ruleEngine) {
    this.sensors = sensors;
    this.readings = readings;
    this.ruleEngine = ruleEngine;
  }

  /**
   * Indica si el motor de simulación se encuentra actualmente en ejecución.
   *
   * @return true si el motor está activo generando lecturas
   * @since 2.1.0
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Inicia el motor de simulación. Las lecturas comienzan a generarse en el
   * próximo ciclo programado (@link Scheduled}.
   *
   * @since 2.1.0
   */
  public void start() {
    running = true;
    log.info("IoT Simulation engine STARTED (interval={}ms)", INTERVAL_MS);
  }

  /**
   * Detiene el motor de simulación de forma segura.
   *
   * @since 2.1.0
   */
  public void stop() {
    running = false;
    log.info("IoT Simulation engine STOPPED");
  }

  @Scheduled(fixedRate = 5000)
  public void tick() {
    if (!running) {
      return;
    }
    try {
      List<Sensor> allSensors = sensors.findAll();
      if (allSensors.isEmpty()) {
        return;
      }
      for (Sensor sensor : allSensors) {
        generateAndSaveReading(sensor);
      }
    } catch (Exception e) {
      log.error("Simulation tick error", e);
    }
  }

  @Transactional
  public void generateAndSaveReading(Sensor sensor) {
    SensorState state = sensorStates.computeIfAbsent(sensor.id, k -> new SensorState(sensor));

    BigDecimal value = state.nextValue();
    Reading reading = new Reading();
    reading.sensor = sensor;
    reading.value = value;
    reading.recordedAt = LocalDateTime.now();
    readings.save(reading);

    log.debug("Simulated reading: sensor={} type={} value={} {}",
        sensor.code, sensor.type, value, sensor.unit);

    // Re-attach sensor for lazy fields needed by rule engine
    Sensor attached = sensors.findById(sensor.id).orElse(sensor);
    ruleEngine.evaluateReading(attached, reading);
  }

  /**
   * Internal state for gradual sensor value simulation.
   */
  private class SensorState {
    private final SensorType type;
    private final BigDecimal min;
    private final BigDecimal max;
    private double current;
    private double trend; // positive = rising, negative = falling
    private int stableTicks = 0;

    SensorState(Sensor sensor) {
      this.type = sensor.type;
      this.min = sensor.minThreshold != null ? sensor.minThreshold : defaultMin(type);
      this.max = sensor.maxThreshold != null ? sensor.maxThreshold : defaultMax(type);
      this.current = (min.doubleValue() + max.doubleValue()) / 2.0;
      this.trend = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
    }

    BigDecimal nextValue() {
      // Occasionally inject anomaly (5% chance)
      if (random.nextDouble() < 0.05) {
        double anomaly = random.nextBoolean()
            ? max.doubleValue() * 1.15   // above max
            : min.doubleValue() * 0.85;  // below min
        current = anomaly;
        trend = 0;
        stableTicks = 0;
        return BigDecimal.valueOf(current).setScale(2, RoundingMode.HALF_UP);
      }

      // Change trend direction gradually
      if (stableTicks > 5) {
        trend += (random.nextDouble() - 0.5) * 0.8;
        trend = Math.max(-2.0, Math.min(2.0, trend));
        stableTicks = 0;
      }

      // Apply trend with small noise
      double noise = (random.nextDouble() - 0.5) * 0.3;
      current += trend * 0.15 + noise;

      // Bounds with soft bounce
      double range = max.doubleValue() - min.doubleValue();
      double lower = min.doubleValue() - range * 0.05;
      double upper = max.doubleValue() + range * 0.05;

      if (current < lower) {
        current = lower + random.nextDouble() * 0.5;
        trend = Math.abs(trend) * 0.5 + 0.2;
      }
      if (current > upper) {
        current = upper - random.nextDouble() * 0.5;
        trend = -Math.abs(trend) * 0.5 - 0.2;
      }

      stableTicks++;
      return BigDecimal.valueOf(current).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultMin(SensorType t) {
      return switch (t) {
        case TEMPERATURE -> BigDecimal.valueOf(18);
        case HUMIDITY -> BigDecimal.valueOf(40);
        case SOIL_MOISTURE -> BigDecimal.valueOf(15);
        case LIGHT -> BigDecimal.valueOf(100);
      };
    }

    private BigDecimal defaultMax(SensorType t) {
      return switch (t) {
        case TEMPERATURE -> BigDecimal.valueOf(32);
        case HUMIDITY -> BigDecimal.valueOf(80);
        case SOIL_MOISTURE -> BigDecimal.valueOf(65);
        case LIGHT -> BigDecimal.valueOf(900);
      };
    }
  }
}
