import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from iot_simulator import generate_reading, post_reading


def test_generate_reading_returns_number():
    sensor = {
        "id": 1,
        "type": "TEMPERATURE",
        "code": "TEMP-TEST",
        "unit": "C",
        "minThreshold": 18,
        "maxThreshold": 32
    }
    value = generate_reading(sensor)
    assert isinstance(value, float)


def test_generate_reading_within_thresholds():
    sensor = {
        "id": 1,
        "type": "TEMPERATURE",
        "code": "TEMP-TEST",
        "unit": "C",
        "minThreshold": 18,
        "maxThreshold": 32
    }
    # Test many times to statistically validate (allow anomalies)
    for _ in range(50):
        value = generate_reading(sensor)
        assert value >= 10.0  # allow anomaly below
        assert value <= 45.0  # allow anomaly above


def test_generate_reading_humidity():
    sensor = {
        "id": 2,
        "type": "HUMIDITY",
        "code": "HUM-TEST",
        "unit": "%",
        "minThreshold": 40,
        "maxThreshold": 80
    }
    value = generate_reading(sensor)
    assert isinstance(value, float)
    assert value >= 35.0
    assert value <= 85.0


def test_generate_reading_soil_moisture():
    sensor = {
        "id": 3,
        "type": "SOIL_MOISTURE",
        "code": "SOIL-TEST",
        "unit": "%",
        "minThreshold": 15,
        "maxThreshold": 65
    }
    value = generate_reading(sensor)
    assert isinstance(value, float)
    assert value >= 10.0
    assert value <= 70.0


def test_generate_reading_light():
    sensor = {
        "id": 4,
        "type": "LIGHT",
        "code": "LIGHT-TEST",
        "unit": "lux",
        "minThreshold": 100,
        "maxThreshold": 900
    }
    value = generate_reading(sensor)
    assert isinstance(value, float)
    assert value >= 80.0
    assert value <= 1000.0


def test_generate_reading_with_default_thresholds():
    sensor = {
        "id": 5,
        "type": "TEMPERATURE",
        "code": "TEMP-DEF",
        "unit": "C"
        # no min/max thresholds
    }
    value = generate_reading(sensor)
    assert isinstance(value, float)


def test_generate_reading_structure():
    sensor = {
        "id": 1,
        "type": "TEMPERATURE",
        "code": "TEMP-TEST",
        "unit": "C",
        "minThreshold": 18,
        "maxThreshold": 32
    }
    value = generate_reading(sensor)
    # Value should have at most 2 decimal places
    assert round(value, 2) == value
