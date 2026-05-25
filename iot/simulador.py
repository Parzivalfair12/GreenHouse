"""IoT Sensor Simulator for GreenHouse.

Simulates temperature, humidity, light and CO2 sensors,
sends periodic readings to the GreenHouse backend API.

Usage:
    python simulador.py --interval 5 --anomaly-prob 0.15 --backend http://localhost:8080
"""

import argparse
import datetime
import json
import random
import time
import sys
import urllib.request
import urllib.error


SENSOR_TYPES = ["TEMPERATURE", "HUMIDITY", "LIGHT", "SOIL_MOISTURE"]

SENSOR_CONFIG = {
    "TEMPERATURE": {"min": 15, "max": 40, "normal_min": 18, "normal_max": 30, "unit": "C"},
    "HUMIDITY": {"min": 10, "max": 100, "normal_min": 40, "normal_max": 85, "unit": "%"},
    "LIGHT": {"min": 0, "max": 10000, "normal_min": 200, "normal_max": 8000, "unit": "lux"},
    "SOIL_MOISTURE": {"min": 0, "max": 100, "normal_min": 30, "normal_max": 80, "unit": "%"},
}


class SensorSimulator:
    def __init__(self, backend_url, anomaly_prob=0.1):
        self.backend = backend_url.rstrip("/")
        self.anomaly_prob = anomaly_prob
        self.values = {st: 25 for st in SENSOR_TYPES}
        self._init_sensors()

    def _init_sensors(self):
        for st in SENSOR_TYPES:
            cfg = SENSOR_CONFIG[st]
            mid = (cfg["normal_min"] + cfg["normal_max"]) / 2
            self.values[st] = mid + random.uniform(-5, 5)

    def _drift(self, sensor_type):
        cfg = SENSOR_CONFIG[sensor_type]
        current = self.values.get(sensor_type, cfg["normal_min"])
        drift = random.uniform(-2, 2)
        new_val = current + drift

        if random.random() < self.anomaly_prob:
            anomaly_delta = random.uniform(15, 25) * random.choice([-1, 1])
            new_val += anomaly_delta
            print(f"[WARNING] Anomalia generada en {sensor_type}: {new_val:.1f}")

        new_val = max(cfg["min"], min(cfg["max"], new_val))
        self.values[sensor_type] = new_val
        return round(new_val, 1)

    def _check_alert(self, sensor_code, sensor_type, value):
        cfg = SENSOR_CONFIG[sensor_type]
        if value > cfg["normal_max"] or value < cfg["normal_min"]:
            level = "WARNING" if abs(value - cfg["normal_max"]) < 10 else "CRITICAL"
            message = f"{sensor_type} fuera de rango: {value}{cfg['unit']} (limite {cfg['normal_min']}-{cfg['normal_max']})"
            print(f"[ALERT] [{level}] {message}")
            return level, message
        return None, None

    def send_reading(self, sensor_code, sensor_type, value):
        reading = {
            "sensorId": 1,
            "value": value,
            "recordedAt": datetime.datetime.now().isoformat()
        }
        try:
            data = json.dumps(reading).encode("utf-8")
            req = urllib.request.Request(
                f"{self.backend}/api/readings",
                data=data,
                headers={"Content-Type": "application/json"},
                method="POST"
            )
            with urllib.request.urlopen(req, timeout=5) as resp:
                result = json.loads(resp.read().decode())
                print(f"[INFO] Lectura enviada: {sensor_code}={value}{SENSOR_CONFIG[sensor_type]['unit']}")
                return True
        except urllib.error.HTTPError as e:
            print(f"[ERROR] HTTP {e.code} al enviar lectura: {e.reason}")
            return False
        except Exception as e:
            print(f"[ERROR] Red: {e}")
            return False

    def step(self):
        print(f"\n--- Ciclo {datetime.datetime.now().strftime('%H:%M:%S')} ---")
        for st in SENSOR_TYPES:
            code = f"{st[:4]}-001"
            value = self._drift(st)
            ok = self.send_reading(code, st, value)
            if ok:
                level, msg = self._check_alert(code, st, value)
                if level:
                    print(f"  -> Alerta activada")
        time.sleep(0.2)


def main():
    parser = argparse.ArgumentParser(description="GreenHouse IoT Sensor Simulator")
    parser.add_argument("--interval", type=int, default=5, help="Intervalo entre ciclos (segundos)")
    parser.add_argument("--anomaly-prob", type=float, default=0.1, help="Probabilidad de anomalia (0-1)")
    parser.add_argument("--backend", type=str, default="http://localhost:8080", help="URL del backend")
    args = parser.parse_args()

    print("=" * 50)
    print(" GREENHOUSE IOT SIMULATOR")
    print("=" * 50)
    print(f" Backend: {args.backend}")
    print(f" Intervalo: {args.interval}s")
    print(f" Probabilidad anomalia: {args.anomaly_prob}")
    print(f" Sensores: {', '.join(SENSOR_TYPES)}")
    print("=" * 50)

    sim = SensorSimulator(args.backend, args.anomaly_prob)

    try:
        while True:
            sim.step()
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\n[INFO] Simulacion detenida por el usuario")
        sys.exit(0)


if __name__ == "__main__":
    main()
