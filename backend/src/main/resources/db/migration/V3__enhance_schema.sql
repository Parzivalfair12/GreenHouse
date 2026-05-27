-- Enhance AutomationRule with foreign keys
ALTER TABLE automation_rule
  ADD COLUMN IF NOT EXISTS sensor_id BIGINT REFERENCES sensor(id),
  ADD COLUMN IF NOT EXISTS actuator_id BIGINT REFERENCES actuator(id);

-- Enhance IrrigationEvent with foreign keys
ALTER TABLE irrigation_event
  ADD COLUMN IF NOT EXISTS actuator_id BIGINT REFERENCES actuator(id),
  ADD COLUMN IF NOT EXISTS zone_id BIGINT REFERENCES greenhouse(id),
  ADD COLUMN IF NOT EXISTS rule_id BIGINT REFERENCES automation_rule(id);

-- Enhance AuditLog with foreign keys
ALTER TABLE audit_log
  ADD COLUMN IF NOT EXISTS greenhouse_id BIGINT REFERENCES greenhouse(id),
  ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES app_user(id);
