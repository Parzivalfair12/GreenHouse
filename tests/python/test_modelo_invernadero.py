import json
import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[2]
MODEL_PATH = ROOT / "docs" / "modelo-invernadero.json"


class GreenhouseModelTest(unittest.TestCase):
    def setUp(self):
        self.model = json.loads(MODEL_PATH.read_text(encoding="utf-8"))

    def test_model_contains_required_sections(self):
        self.assertIn("greenhouses", self.model)
        self.assertIn("alerts", self.model)
        self.assertIn("users", self.model)

    def test_greenhouse_has_nested_domain_data(self):
        greenhouse = self.model["greenhouses"][0]
        self.assertGreater(len(greenhouse["crops"]), 0)
        self.assertGreater(len(greenhouse["sensors"]), 0)
        self.assertGreater(len(greenhouse["irrigationEvents"]), 0)

    def test_sensor_thresholds_are_valid(self):
        sensor = self.model["greenhouses"][0]["sensors"][0]
        self.assertLess(sensor["minThreshold"], sensor["maxThreshold"])

    def test_greenhouse_has_required_fields(self):
        for gh in self.model["greenhouses"]:
            self.assertIn("id", gh)
            self.assertIn("name", gh)
            self.assertIn("location", gh)
            self.assertIn("areaSquareMeters", gh)

    def test_alert_has_required_fields(self):
        for alert in self.model["alerts"]:
            self.assertIn("id", alert)
            self.assertIn("severity", alert)
            self.assertIn("message", alert)
            self.assertIn("resolved", alert)

    def test_user_has_required_fields(self):
        for user in self.model["users"]:
            self.assertIn("id", user)
            self.assertIn("email", user)
            self.assertIn("fullName", user)
            self.assertIn("role", user)

    def test_sensor_has_valid_type(self):
        valid_types = {"TEMPERATURE", "HUMIDITY", "SOIL_MOISTURE", "LIGHT"}
        for gh in self.model["greenhouses"]:
            for sensor in gh.get("sensors", []):
                self.assertIn(sensor["type"], valid_types)

    def test_crop_has_status(self):
        valid_statuses = {"GERMINATING", "GROWING", "HARVESTED", "LOST"}
        for gh in self.model["greenhouses"]:
            for crop in gh.get("crops", []):
                self.assertIn(crop["status"], valid_statuses)

    def test_irrigation_has_mode(self):
        valid_modes = {"MANUAL", "AUTOMATIC"}
        for gh in self.model["greenhouses"]:
            for event in gh.get("irrigationEvents", []):
                self.assertIn(event["mode"], valid_modes)

    def test_area_is_positive(self):
        for gh in self.model["greenhouses"]:
            self.assertGreater(gh["areaSquareMeters"], 0)


if __name__ == "__main__":
    unittest.main()
