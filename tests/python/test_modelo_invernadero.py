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


if __name__ == "__main__":
    unittest.main()
