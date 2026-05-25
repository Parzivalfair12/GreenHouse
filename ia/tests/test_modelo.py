import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from modelo import predict_next, detect_anomaly, get_risk_level
import pytest


def test_predict_next_returns_float():
    values = [25, 26, 27, 28, 29]
    result = predict_next(values, "temperature")
    assert isinstance(result, float)


def test_predict_next_with_insufficient_data():
    values = [25]
    result = predict_next(values, "temperature")
    assert isinstance(result, float)


def test_detect_anomaly_returns_bool():
    recent = [25, 26, 25, 27, 26, 25]
    assert detect_anomaly(50, recent) is True
    assert detect_anomaly(26, recent) is False


def test_get_risk_level_low():
    assert get_risk_level(25, 60) == "LOW"


def test_get_risk_level_medium():
    risk = get_risk_level(34, 20)
    assert risk == "MEDIUM"


def test_get_risk_level_high():
    risk = get_risk_level(38, 60)
    assert risk == "HIGH"


def test_empty_predict():
    result = predict_next([], "temperature")
    assert isinstance(result, float)


def test_detect_anomaly_insufficient_data():
    assert detect_anomaly(50, [25]) is False
