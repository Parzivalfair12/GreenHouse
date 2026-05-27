-- Add missing foreign key columns that were added to V3 after it was already applied
-- This migration compensates for the schema drift caused by modifying V3 post-application

ALTER TABLE automation_rule
  ADD COLUMN IF NOT EXISTS sensor_id BIGINT REFERENCES sensor(id),
  ADD COLUMN IF NOT EXISTS actuator_id BIGINT REFERENCES actuator(id);

ALTER TABLE irrigation_event
  ADD COLUMN IF NOT EXISTS actuator_id BIGINT REFERENCES actuator(id),
  ADD COLUMN IF NOT EXISTS zone_id BIGINT REFERENCES zone(id),
  ADD COLUMN IF NOT EXISTS rule_id BIGINT REFERENCES automation_rule(id);

ALTER TABLE audit_log
  ADD COLUMN IF NOT EXISTS greenhouse_id BIGINT REFERENCES greenhouse(id),
  ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES app_user(id);
