CREATE TABLE greenhouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    area_square_meters DECIMAL(19,2) NOT NULL CHECK (area_square_meters > 0),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE crop (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    variety VARCHAR(255),
    planted_at DATE,
    expected_harvest_at DATE,
    status VARCHAR(32) DEFAULT 'GERMINATING',
    greenhouse_id BIGINT NOT NULL REFERENCES greenhouse(id)
);

CREATE TABLE sensor (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    unit VARCHAR(255) NOT NULL,
    min_threshold DECIMAL(19,2),
    max_threshold DECIMAL(19,2),
    greenhouse_id BIGINT REFERENCES greenhouse(id),
    zone_id BIGINT,
    CONSTRAINT uk_sensor_code UNIQUE (code)
);

CREATE TABLE reading (
    id BIGSERIAL PRIMARY KEY,
    reading_value DECIMAL(19,2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    sensor_id BIGINT NOT NULL REFERENCES sensor(id)
);

CREATE TABLE irrigation_event (
    id BIGSERIAL PRIMARY KEY,
    started_at TIMESTAMP,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    water_liters DECIMAL(19,2) NOT NULL CHECK (water_liters > 0),
    mode VARCHAR(32) DEFAULT 'AUTOMATIC',
    greenhouse_id BIGINT NOT NULL REFERENCES greenhouse(id)
);

CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(32) DEFAULT 'INFO',
    message VARCHAR(255) NOT NULL,
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sensor_id BIGINT REFERENCES sensor(id)
);

CREATE TABLE actuator (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) DEFAULT 'IRRIGATION',
    enabled BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    greenhouse_id BIGINT REFERENCES greenhouse(id)
);

CREATE TABLE automation_rule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) DEFAULT 'LOW_HUMIDITY_IRRIGATION',
    threshold DECIMAL(19,2),
    enabled BOOLEAN DEFAULT TRUE,
    greenhouse_id BIGINT REFERENCES greenhouse(id)
);

CREATE TABLE zone (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    greenhouse_id BIGINT REFERENCES greenhouse(id)
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    detail VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    origin VARCHAR(32) DEFAULT 'MANUAL'
);

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    provider VARCHAR(255) DEFAULT 'email',
    role VARCHAR(32) DEFAULT 'VIEWER',
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

ALTER TABLE sensor ADD CONSTRAINT fk_sensor_zone FOREIGN KEY (zone_id) REFERENCES zone(id);
