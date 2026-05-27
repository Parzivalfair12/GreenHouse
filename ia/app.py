import logging
from flask import Flask, request, jsonify
from modelo import (
    predict_next, detect_anomaly, get_risk_level,
    predict_from_db, get_recommendation, fetch_recent_readings
)
import numpy as np

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

@app.route("/ia/health", methods=["GET"])
def health():
    return jsonify({"status": "UP", "service": "greenhouse-ia", "version": "2.0.0"})

@app.route("/ia/predict", methods=["POST"])
def predict():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body required"}), 400

    # Prefer real DB data over passed arrays
    temp_pred_db = predict_from_db("TEMPERATURE", 30)
    hum_pred_db = predict_from_db("HUMIDITY", 30)

    temperatures = data.get("temperature", [])
    humidities = data.get("humidity", [])

    # If DB has data, use it; otherwise fall back to passed arrays
    temp_pred = temp_pred_db if temp_pred_db is not None else (predict_next(temperatures, "temperature") if temperatures else None)
    hum_pred = hum_pred_db if hum_pred_db is not None else (predict_next(humidities, "humidity") if humidities else None)

    temp_current = temperatures[-1] if temperatures else None
    hum_current = humidities[-1] if humidities else None

    temp_anomaly = detect_anomaly(temp_pred, temperatures) if temp_pred and temperatures else False
    hum_anomaly = detect_anomaly(hum_pred, humidities) if hum_pred and humidities else False

    risk = get_risk_level(temp_pred or 25, hum_pred or 60, temp_current, hum_current)

    return jsonify({
        "predictedTemperature": temp_pred,
        "predictedHumidity": hum_pred,
        "riskLevel": risk,
        "anomalies": {
            "temperature": temp_anomaly,
            "humidity": hum_anomaly
        },
        "source": "database" if (temp_pred_db or hum_pred_db) else "input"
    })

@app.route("/ia/recommend", methods=["POST"])
def recommend():
    data = request.get_json() or {}
    temp_pred = data.get("predictedTemperature", 25)
    hum_pred = data.get("predictedHumidity", 60)
    risk = data.get("riskLevel", "LOW")

    recommendation = get_recommendation(temp_pred, hum_pred, risk)
    action = "NORMAL_OPERATION"
    if risk == "HIGH":
        action = "URGENT_ACTION"
    elif risk == "MEDIUM":
        action = "PREVENTIVE_ACTION"

    return jsonify({
        "action": action,
        "recommendation": recommendation,
        "riskLevel": risk,
        "predictedTemperature": temp_pred,
        "predictedHumidity": hum_pred
    })

@app.route("/ia/history", methods=["GET"])
def history():
    """Fetch real historical readings for visualization."""
    sensor_type = request.args.get("type", "TEMPERATURE")
    limit = int(request.args.get("limit", 50))
    rows = fetch_recent_readings(sensor_type, limit)
    return jsonify({
        "readings": rows,
        "count": len(rows),
        "type": sensor_type
    })

@app.route("/ia/anomaly", methods=["POST"])
def check_anomaly():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body required"}), 400

    value = data.get("value")
    values = data.get("recentValues", [])
    sensor_type = data.get("type", "unknown")

    if value is None or not values:
        return jsonify({"anomaly": False, "message": "Datos insuficientes"})

    is_anomaly = detect_anomaly(value, values)

    return jsonify({
        "anomaly": is_anomaly,
        "sensorType": sensor_type,
        "value": value,
        "message": f"Anomalia detectada en {sensor_type}: {value}" if is_anomaly else "Lectura normal"
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
