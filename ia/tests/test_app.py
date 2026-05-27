import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from app import app


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def test_health_returns_service_info(client):
    resp = client.get("/ia/health")
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["service"] == "greenhouse-ia"
    assert data["status"] == "UP"


def test_predict_returns_json(client):
    resp = client.post("/ia/predict", json={
        "temperature": [28, 29, 30],
        "humidity": [60, 61, 62]
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert "predictedTemperature" in data
    assert "predictedHumidity" in data
    assert "riskLevel" in data
    assert "anomalies" in data


def test_predict_without_body_returns_error(client):
    resp = client.post("/ia/predict", content_type="application/json")
    assert resp.status_code in (400, 415)


def test_recommend_returns_action(client):
    resp = client.post("/ia/recommend", json={
        "predictedTemperature": 32,
        "predictedHumidity": 50,
        "riskLevel": "MEDIUM"
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["action"] == "PREVENTIVE_ACTION"
    assert "recommendation" in data


def test_recommend_high_risk_returns_urgent(client):
    resp = client.post("/ia/recommend", json={
        "predictedTemperature": 38,
        "predictedHumidity": 20,
        "riskLevel": "HIGH"
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["action"] == "URGENT_ACTION"


def test_anomaly_detects_anomaly(client):
    resp = client.post("/ia/anomaly", json={
        "value": 50,
        "recentValues": [25, 26, 25, 27, 26, 25],
        "type": "TEMPERATURE"
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["anomaly"] is True


def test_anomaly_normal_reading(client):
    resp = client.post("/ia/anomaly", json={
        "value": 26,
        "recentValues": [25, 26, 25, 27, 26, 25],
        "type": "TEMPERATURE"
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["anomaly"] is False


def test_anomaly_insufficient_data(client):
    resp = client.post("/ia/anomaly", json={
        "value": 50,
        "recentValues": [25],
        "type": "TEMPERATURE"
    })
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["anomaly"] is False


def test_history_returns_readings_list(client):
    resp = client.get("/ia/history?type=TEMPERATURE&limit=10")
    assert resp.status_code == 200
    data = resp.get_json()
    assert "readings" in data
    assert "count" in data
    assert "type" in data
