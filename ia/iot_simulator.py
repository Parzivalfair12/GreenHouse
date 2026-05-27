#!/usr/bin/env python3
"""IoT Sensor Simulator for GreenHouse Manager.

Generates realistic sensor readings and POSTs them to the Spring Boot backend.
Can be run standalone or controlled via the backend SimulatorController.

Usage:
    python iot_simulator.py --interval 5 --duration 300
"""

import argparse
import random
import time
import requests
from datetime import datetime

API_BASE = "http://localhost:8080"
AUTH_TOKEN = None


def get_sensors():
    """Fetch all sensors from the backend."""
    headers = {}
    if AUTH_TOKEN:
        headers["Authorization"] = f"Bearer {AUTH_TOKEN}"
    resp = requests.get(f"{API_BASE}/api/sensors", headers=headers, timeout=10)
    resp.raise_for_status()
    return resp.json()


def generate_reading(sensor):
    """Generate a realistic reading based on sensor type."""
    sensor_type = sensor.get("type", "TEMPERATURE")
    min_t = sensor.get("minThreshold", 0)
    max_t = sensor.get("maxThreshold", 100)
    base = {
        "TEMPERATURE": 22.0 + random.gauss(0, 3),
        "HUMIDITY": 55.0 + random.gauss(0, 10),
        "SOIL_MOISTURE": 35.0 + random.gauss(0, 8),
        "LIGHT": 500.0 + random.gauss(0, 150),
    }.get(sensor_type, random.uniform(min_t, max_t))

    # Clamp to thresholds
    if min_t is not None and base < min_t:
        base = min_t + random.random() * 2
    if max_t is not None and base > max_t:
        base = max_t - random.random() * 2

    # 5% chance of anomaly
    if random.random() < 0.05:
        base = base * 1.4 if random.random() < 0.5 else base * 0.6

    return round(base, 2)


def post_reading(sensor, value):
    """POST a reading to the backend."""
    headers = {"Content-Type": "application/json"}
    if AUTH_TOKEN:
        headers["Authorization"] = f"Bearer {AUTH_TOKEN}"
    payload = {
        "sensorId": sensor["id"],
        "value": value,
        "recordedAt": datetime.now().isoformat()
    }
    resp = requests.post(f"{API_BASE}/api/readings", json=payload, headers=headers, timeout=10)
    resp.raise_for_status()
    return resp.json()


def run_simulation(interval_seconds, max_iterations=None):
    """Run the simulation loop."""
    print(f"[IoT Simulator] Starting with interval={interval_seconds}s")
    iteration = 0
    while max_iterations is None or iteration < max_iterations:
        iteration += 1
        try:
            sensors = get_sensors()
            if not sensors:
                print("[IoT Simulator] No sensors found. Waiting...")
                time.sleep(interval_seconds)
                continue
            for sensor in sensors:
                value = generate_reading(sensor)
                result = post_reading(sensor, value)
                print(f"[IoT Simulator] {sensor['code']} ({sensor['type']}): {value} {sensor.get('unit','')} -> reading #{result.get('id')}")
            time.sleep(interval_seconds)
        except requests.exceptions.ConnectionError:
            print(f"[IoT Simulator] Backend unavailable. Retrying in {interval_seconds}s...")
            time.sleep(interval_seconds)
        except Exception as e:
            print(f"[IoT Simulator] Error: {e}")
            time.sleep(interval_seconds)
    print("[IoT Simulator] Finished.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="GreenHouse IoT Sensor Simulator")
    parser.add_argument("--interval", type=int, default=5, help="Seconds between reading batches")
    parser.add_argument("--duration", type=int, default=0, help="Total duration in seconds (0=unlimited)")
    parser.add_argument("--token", type=str, default=None, help="JWT auth token")
    args = parser.parse_args()

    if args.token:
        AUTH_TOKEN = args.token

    max_iter = args.duration // args.interval if args.duration > 0 else None
    run_simulation(args.interval, max_iter)
